package ec.edu.monster.modelo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Asks the RESTful server whether it is up by requesting its health resource,
 * which answers without a session token. It asks on every call, because the
 * server may go down at any moment while the client is open.
 */
public class EstadoServidor {

    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    private final URI recurso;
    private final HttpClient cliente = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    public EstadoServidor() {
        this(URI.create(Configuracion.url() + "/jakartaee11"));
    }

    /** Points the availability check at the given health resource address. */
    public EstadoServidor(URI recurso) {
        this.recurso = recurso;
    }

    /** True when the server answers its health resource right now. */
    public boolean disponible() {
        HttpRequest peticion = HttpRequest.newBuilder(recurso).timeout(TIMEOUT).GET().build();
        try {
            HttpResponse<Void> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.discarding());
            return respuesta.statusCode() == 200;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
