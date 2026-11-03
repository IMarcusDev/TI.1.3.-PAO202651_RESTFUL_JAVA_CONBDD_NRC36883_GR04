package ec.edu.monster.modelo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Asks the RESTful server whether it is up by requesting the API root. It asks on every
 * call, because the server may go down at any moment while the client is open.
 */
public class EstadoServidor {

    private static final Duration ESPERA = Duration.ofSeconds(2);

    private final URI api;
    private final HttpClient cliente = HttpClient.newBuilder().connectTimeout(ESPERA).build();

    public EstadoServidor(Configuracion configuracion) {
        this(URI.create(configuracion.getEndpoint(RutasApi.RAIZ)));
    }

    /** Points the availability check at the given address. */
    public EstadoServidor(URI api) {
        this.api = api;
    }

    /**
     * True when the server answers right now. The API root has no resource of its own, so any
     * answer below 500 (404 or 405 included) means the server is running.
     */
    public boolean disponible() {
        HttpRequest peticion = HttpRequest.newBuilder(api).timeout(ESPERA).GET().build();
        try {
            HttpResponse<Void> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.discarding());
            return respuesta.statusCode() < 500;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
