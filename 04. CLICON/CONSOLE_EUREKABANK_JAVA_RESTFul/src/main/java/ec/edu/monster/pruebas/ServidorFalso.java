package ec.edu.monster.pruebas;

import ec.edu.monster.modelos.http.RespuestaHttp;
import ec.edu.monster.modelos.http.Transporte;
import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

/** Reemplaza la red: guarda la última petición y devuelve la respuesta preparada. */
//revisado y aprovado por Marcos Escobar
class ServidorFalso implements Transporte {
  String url = "";
  String cuerpo = "";
  Map<String, String> cabeceras = new HashMap<>();
  String tipoContenido = "";
  String respuesta;
  int codigo = 200;
  boolean sinRed;

  ServidorFalso(String respuesta) {
    this.respuesta = respuesta;
  }

  @Override
  public RespuestaHttp enviar(String url, String cuerpo, String tipoContenido, Map<String, String> cabeceras)
      throws IOException {
    this.url = url;
    this.cuerpo = cuerpo;
    this.tipoContenido = tipoContenido;
    this.cabeceras = cabeceras;
    if (sinRed)
      throw new ConnectException("sin red");
    return new RespuestaHttp(codigo, respuesta);
  }
}
