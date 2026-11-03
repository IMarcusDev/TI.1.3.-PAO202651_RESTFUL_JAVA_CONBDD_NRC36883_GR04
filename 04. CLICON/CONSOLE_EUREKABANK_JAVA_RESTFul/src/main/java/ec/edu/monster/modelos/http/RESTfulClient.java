package ec.edu.monster.modelos.http;

import java.io.IOException;
import java.util.Map;
import org.json.JSONObject;

/** POST con cuerpo JSON; el token viaja dentro del cuerpo. */
//revisado y aprovado por Marcos Escobar
public class RESTfulClient {
  public String post(String endpoint, JSONObject body) throws IOException, InterruptedException {
    RespuestaHttp respuesta = ConfigHTTP.getTransporte().enviar(
      ConfigHTTP.getBaseUrl() + endpoint,
      body.toString(),
      "application/json; charset=utf-8",
      Map.of("Accept", "application/json")
    );

    return respuesta.cuerpo();
  }
}
