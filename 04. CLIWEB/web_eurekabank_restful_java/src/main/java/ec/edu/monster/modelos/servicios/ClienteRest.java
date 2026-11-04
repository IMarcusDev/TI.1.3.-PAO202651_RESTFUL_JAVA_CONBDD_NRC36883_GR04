package ec.edu.monster.modelos.servicios;

import ec.edu.monster.modelos.dto.EstadosEnum;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Sends JSON to one REST endpoint and turns every outcome into a {@link Respuesta},
 * so the services never throw and the controllers only look at estado/mensaje.
 */
final class ClienteRest {
  static final String SESION_EXPIRADA = "Su sesión expiró o no es válida. Inicie sesión nuevamente.";

  private static final Duration ESPERA_CONEXION = Duration.ofSeconds(5);
  private static final String SIN_CONEXION = "No se pudo conectar con el servidor: ";
  private static final String SIN_RESPUESTA = "El servidor no devolvió respuesta";
  private static final String CON_ERROR = "El servidor respondió con un error (HTTP %d)";
  private static final String ERROR_SERVIDOR = "Error del servidor";
  private static final String ESTADO_OK = "OK";
  private static final int NO_AUTORIZADO = 401;

  /** Result of one call; json is only set when estado is OK. */
  record Respuesta(EstadosEnum estado, String mensaje, JSONObject json) {
    boolean ok() {
      return estado == EstadosEnum.OK;
    }
  }

  // HttpClient is thread-safe and expensive to build: one per service
  private final HttpClient http = HttpClient.newBuilder().connectTimeout(ESPERA_CONEXION).build();
  private final Duration esperaRespuesta;
  private final String sinRespuestaATiempo;

  /**
   * @param sinRespuestaATiempo message for a request that was sent but not answered in time;
   *                            the server may have processed it, so a movement must not say
   *                            "could not connect"
   */
  ClienteRest(Duration esperaRespuesta, String sinRespuestaATiempo) {
    this.esperaRespuesta = esperaRespuesta;
    this.sinRespuestaATiempo = sinRespuestaATiempo;
  }

  /** Message for a request that never reached the server. */
  static String sinConexion(String endpoint) {
    return SIN_CONEXION + endpoint;
  }

  /** POSTs {@code cuerpo} to {@code endpoint}; the server reads the session token from the body. */
  Respuesta post(String endpoint, JSONObject cuerpo) {
    HttpRequest peticion = HttpRequest.newBuilder(URI.create(endpoint))
        .timeout(esperaRespuesta)
        .header("Content-Type", "application/json; charset=UTF-8")
        .header("Accept", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(cuerpo.toString(), StandardCharsets.UTF_8))
        .build();

    HttpResponse<String> respuesta;
    try {
      respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    } catch (HttpConnectTimeoutException e) {
      // Never connected: the request did not leave this computer
      return error(sinConexion(endpoint));
    } catch (HttpTimeoutException e) {
      // Sent but not answered: the server may have processed it
      return error(sinRespuestaATiempo);
    } catch (IOException e) {
      // Server down, wrong host/port in config.properties, network error...
      return error(sinConexion(endpoint));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return error(sinConexion(endpoint));
    }
    // Only requests carrying a token can have an expired session; a login 401 keeps the server message
    return interpretar(respuesta.statusCode(), respuesta.body(), cuerpo.has("token"));
  }

  // The JSON "estado" decides; the HTTP status only matters when there is no usable JSON
  private static Respuesta interpretar(int estadoHttp, String texto, boolean conSesion) {
    if (conSesion && estadoHttp == NO_AUTORIZADO) {
      // Unknown or expired token: one fixed message, so every screen handles it the same way
      return new Respuesta(EstadosEnum.SESION_EXPIRADA, SESION_EXPIRADA, null);
    }
    boolean exito = estadoHttp >= 200 && estadoHttp < 300;
    if (texto == null || texto.isBlank()) {
      return error(exito ? SIN_RESPUESTA : String.format(CON_ERROR, estadoHttp));
    }
    JSONObject json;
    try {
      json = new JSONObject(texto);
    } catch (JSONException e) {
      return error(String.format(CON_ERROR, estadoHttp));
    }
    String mensaje = json.optString("mensaje", "");
    if (!exito || !ESTADO_OK.equals(json.optString("estado"))) {
      return error(mensaje.isBlank() ? ERROR_SERVIDOR : mensaje);
    }
    return new Respuesta(EstadosEnum.OK, mensaje, json);
  }

  private static Respuesta error(String mensaje) {
    return new Respuesta(EstadosEnum.ERROR, mensaje, null);
  }
}
