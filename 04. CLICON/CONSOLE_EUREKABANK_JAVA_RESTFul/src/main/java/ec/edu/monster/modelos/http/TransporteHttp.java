package ec.edu.monster.modelos.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Transporte real con java.net.http; deja de esperar a los 15 segundos (RNF-007). */
//revisado y aprovado por Marcos Escobar
public class TransporteHttp implements Transporte {
  private final HttpClient http = HttpClient.newBuilder()
    .version(HttpClient.Version.HTTP_1_1)
    .connectTimeout(ConfigHTTP.TIMEOUT)
    .build();

  @Override
  public RespuestaHttp enviar(String url, String cuerpo, String tipoContenido, Map<String, String> cabeceras)
      throws IOException, InterruptedException {
    HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(url))
      .timeout(ConfigHTTP.TIMEOUT)
      .header("Content-Type", tipoContenido)
      .POST(HttpRequest.BodyPublishers.ofString(cuerpo, StandardCharsets.UTF_8));

    cabeceras.forEach(peticion::header);

    HttpResponse<String> respuesta = http.send(
      peticion.build(),
      HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
    );

    return new RespuestaHttp(respuesta.statusCode(), respuesta.body());
  }
}
