package ec.edu.monster.modelo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
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
    private static final Duration ESPERA_RESPUESTA = Duration.ofSeconds(15);
    private static final String SIN_CONEXION = "No se pudo conectar con el servidor: ";
    private static final String SIN_RESPUESTA = "El servidor no devolvió respuesta";
    private static final String CON_ERROR = "El servidor respondió con un error (HTTP %d)";
    private static final String ESTADO_OK = "OK";

    // HttpClient is thread-safe and expensive to build: one per service
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(ESPERA_CONEXION).build();

    /**
     * POSTs {@code cuerpo} to {@code endpoint}, adding the raw token in {@code Authorization}
     * when it is not null.
     */
    JSONObject post(String endpoint, String token, JSONObject cuerpo) throws ServicioException {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(ESPERA_RESPUESTA)
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo.toString(), StandardCharsets.UTF_8));
        if (token != null) {
            peticion.header("Authorization", token);
        }

        HttpResponse<String> respuesta;
        try {
            respuesta = http.send(peticion.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            // Server down, wrong host/port in config.properties, network error...
            throw new ServicioException(SIN_CONEXION + endpoint);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServicioException(SIN_CONEXION + endpoint);
        }
        return interpretar(respuesta.statusCode(), respuesta.body());
    }

    // The JSON "estado" decides; the HTTP status only matters when there is no usable JSON
    private static JSONObject interpretar(int estadoHttp, String texto) throws ServicioException {
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
