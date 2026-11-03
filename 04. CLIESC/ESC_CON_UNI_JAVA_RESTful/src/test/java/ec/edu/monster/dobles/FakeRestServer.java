package ec.edu.monster.dobles;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ec.edu.monster.modelo.Configuracion;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal HTTP server standing in for the RESTful server, so tests exercise the real
 * Java -> JSON -> HTTP -> JSON -> Java round trip without the real server or the database.
 * It answers each path with a canned response and remembers the last request it received.
 */
public final class FakeRestServer implements AutoCloseable {

    /** Request as the server saw it. */
    public record Peticion(String metodo, String ruta, String autorizacion, String contentType, String cuerpo) {
    }

    private record Respuesta(int estado, String cuerpo, long demoraMs) {
    }

    private final HttpServer servidor;
    private final String contexto;
    private final Map<String, Respuesta> respuestas = new ConcurrentHashMap<>();
    private volatile Peticion ultima;

    private FakeRestServer(Configuracion configuracion) throws IOException {
        URI base = URI.create(configuracion.getUrlBase());
        this.contexto = base.getPath();
        this.servidor = HttpServer.create(new InetSocketAddress(base.getHost(), base.getPort()), 0);
        servidor.createContext("/", this::atender);
        servidor.start();
    }

    /** Starts a fake listening at the host and port the configuration points to. */
    public static FakeRestServer iniciar(Configuracion configuracion) throws IOException {
        return new FakeRestServer(configuracion);
    }

    /** Answers {@code ruta} (relative to the context, e.g. "api/login") with this status and JSON. */
    public FakeRestServer responder(String ruta, int estado, String json) {
        respuestas.put(contexto + "/" + ruta, new Respuesta(estado, json, 0));
        return this;
    }

    /** Like {@link #responder}, but answers 200 only after {@code demoraMs}: a slow server. */
    public FakeRestServer responderTarde(String ruta, long demoraMs, String json) {
        respuestas.put(contexto + "/" + ruta, new Respuesta(200, json, demoraMs));
        return this;
    }

    public Peticion ultimaPeticion() {
        return ultima;
    }

    private void atender(HttpExchange intercambio) throws IOException {
        String cuerpo = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String ruta = intercambio.getRequestURI().getPath();
        ultima = new Peticion(intercambio.getRequestMethod(), ruta,
                intercambio.getRequestHeaders().getFirst("Authorization"),
                intercambio.getRequestHeaders().getFirst("Content-Type"), cuerpo);

        Respuesta respuesta = respuestas.getOrDefault(ruta, new Respuesta(404, "", 0));
        if (respuesta.demoraMs() > 0) {
            try {
                Thread.sleep(respuesta.demoraMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        byte[] bytes = respuesta.cuerpo().getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        intercambio.sendResponseHeaders(respuesta.estado(), bytes.length == 0 ? -1 : bytes.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(bytes);
        }
    }

    @Override
    public void close() {
        servidor.stop(0);
    }
}
