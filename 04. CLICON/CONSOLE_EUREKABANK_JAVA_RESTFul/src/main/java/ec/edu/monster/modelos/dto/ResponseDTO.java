package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.enums.EstadosEnum;
import java.math.BigDecimal;
import org.json.JSONObject;

//revisado y aprovado por Marcos Escobar
public class ResponseDTO {
  private EstadosEnum estado;
  private String mensaje;

  public ResponseDTO(EstadosEnum estado, String mensaje) {
    this.estado = estado;
    this.mensaje = mensaje;
  }

  public EstadosEnum getEstado() {
    return estado;
  }

  public void setEstado(EstadosEnum estado) {
    this.estado = estado;
  }

  public String getMensaje() {
    return mensaje;
  }

  public void setMensaje(String mensaje) {
    this.mensaje = mensaje;
  }

  /** ECUD4: el servidor responde «Token inválido o sesión expirada»; el cliente vuelve al inicio de sesión. */
  public boolean sesionExpirada() {
    return estado == EstadosEnum.ERROR && mensaje != null && mensaje.contains("Token");
  }

  public static ResponseDTO fromJSON(String json) {
    JSONObject obj = new JSONObject(json);
    return new ResponseDTO(leerEstado(obj), leerTexto(obj, "mensaje"));
  }

  /** El servidor Java envía el estado como texto ("OK") y el .NET como número del enum (0 = OK, 1 = ERROR). */
  protected static EstadosEnum leerEstado(JSONObject obj) {
    Object valor = obj.get("estado");

    if (valor instanceof Number numero)
      return EstadosEnum.values()[numero.intValue()];

    return EstadosEnum.valueOf(valor.toString().trim().toUpperCase());
  }

  public static String leerTexto(JSONObject obj, String nombre) {
    Object valor = obj == null ? null : obj.opt(nombre);
    return valor == null || JSONObject.NULL.equals(valor) ? "" : valor.toString().trim();
  }

  /** Los importes pueden llegar como número (120.5) o como texto ("120.50"). */
  public static BigDecimal leerDecimal(JSONObject obj, String nombre) {
    String texto = leerTexto(obj, nombre);
    try {
      return texto.isEmpty() ? null : new BigDecimal(texto);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static int leerEntero(JSONObject obj, String nombre) {
    try {
      return new BigDecimal(leerTexto(obj, nombre)).intValue();
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
