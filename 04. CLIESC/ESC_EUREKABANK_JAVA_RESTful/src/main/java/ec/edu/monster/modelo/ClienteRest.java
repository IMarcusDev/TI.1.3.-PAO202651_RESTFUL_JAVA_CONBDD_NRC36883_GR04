package ec.edu.monster.modelo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Sends JSON to one REST endpoint and returns the answer only when the server accepted it
 * ({@code "estado":"OK"}). Every other outcome becomes a {@link ServicioException} with the same
 * messages the SOAP client shows.
 */
final class ClienteRest {

    private static final Duration ESPERA_CONEXION = Duration.ofSeconds(5);
    /** Default time to wait for an answer once the request was sent (ECUD: 15 s). */
    static final Duration ESPERA_RESPUESTA = Duration.ofSeconds(15);
    private static final String SIN_RESPUESTA = "El servidor no devolvió respuesta";
    private static final String CON_ERROR = "El servidor respondió con un error (HTTP %d)";
    private static final String ESTADO_OK = "OK";
    private static final int NO_AUTORIZADO = 401;

    // HttpClient is thread-safe and expensive to build: one per service
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(ESPERA_CONEXION).build();
    private final Duration esperaRespuesta;
    private final String sinRespuestaATiempo;
    private final boolean conSesion;

    /**
     * @param sinRespuestaATiempo message for a request that was sent but not answered in time;
     *                            the server may have processed it, so it must not say "could not connect"
     * @param conSesion           true when the requests carry a session token: then any HTTP 401
     *                            means the session expired; a login sends no token, so its 401 keeps
     *                            the server message
     */
    ClienteRest(Duration esperaRespuesta, String sinRespuestaATiempo, boolean conSesion) {
        this.esperaRespuesta = esperaRespuesta;
        this.sinRespuestaATiempo = sinRespuestaATiempo;
        this.conSesion = conSesion;
    }

    /** POSTs {@code cuerpo} to {@code endpoint}; the server reads the session token from the body. */
    JSONObject post(String endpoint, JSONObject cuerpo) throws ServicioException {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(esperaRespuesta)
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo.toString(), StandardCharsets.UTF_8));

        HttpResponse<String> respuesta;
        try {
            respuesta = http.send(peticion.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (HttpConnectTimeoutException e) {
            // Never connected: the request did not leave this computer
            throw ServicioException.sinConexion();
        } catch (HttpTimeoutException e) {
            // Sent but not answered: the server may have processed it
            throw new ServicioException(sinRespuestaATiempo);
        } catch (IOException e) {
            // Server down, wrong host/port in config.properties, network error...
            throw ServicioException.sinConexion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ServicioException.sinConexion();
        }
        return interpretar(respuesta.statusCode(), respuesta.body());
    }

    // The JSON "estado" decides; the HTTP status only matters when there is no usable JSON
    private JSONObject interpretar(int estadoHttp, String texto) throws ServicioException {
        if (conSesion && estadoHttp == NO_AUTORIZADO) {
            // Unknown or expired token: one fixed message, so every screen detects it the same way
            throw new ServicioException(Sesiones.TOKEN_INVALIDO);
        }
        boolean exito = estadoHttp >= 200 && estadoHttp < 300;
        if (texto == null || texto.isBlank()) {
            throw new ServicioException(exito ? SIN_RESPUESTA : String.format(CON_ERROR, estadoHttp));
        }
        JSONObject json;
        try {
            json = new JSONObject(texto);
        } catch (JSONException e) {
            throw new ServicioException(String.format(CON_ERROR, estadoHttp));
        }
        if (!exito || !ESTADO_OK.equals(json.optString("estado"))) {
            throw new ServicioException(ServicioException.mensaje(json.optString("mensaje", null)));
        }
        return json;
    }
}
