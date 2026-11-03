package ec.edu.monster.modelos.http;

import java.io.IOException;
import java.util.Map;

/** Envía una petición POST. Las pruebas de pruebas/ lo reemplazan por un servidor simulado. */
//revisado y aprovado por Marcos Escobar
public interface Transporte {
  RespuestaHttp enviar(String url, String cuerpo, String tipoContenido, Map<String, String> cabeceras)
      throws IOException, InterruptedException;
}
