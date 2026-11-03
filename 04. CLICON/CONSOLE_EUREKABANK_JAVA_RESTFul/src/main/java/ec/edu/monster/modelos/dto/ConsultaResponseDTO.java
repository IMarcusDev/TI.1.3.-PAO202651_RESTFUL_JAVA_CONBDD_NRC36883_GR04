package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.enums.EstadosEnum;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** Respuesta de consultarMovimientos: los datos de la cuenta y la lista de sus movimientos. */
//revisado y aprovado por Marcos Escobar
public class ConsultaResponseDTO extends ResponseDTO {
  private String numeroCuenta = "";
  private String titular = "";
  private String moneda = "";
  private BigDecimal saldo;
  private String estadoCuenta = "";
  private final List<MovimientoDTO> movimientos = new ArrayList<>();

  public ConsultaResponseDTO(EstadosEnum estado, String mensaje) {
    super(estado, mensaje);
  }

  public String getNumeroCuenta() {
    return numeroCuenta;
  }

  public String getTitular() {
    return titular;
  }

  public String getMoneda() {
    return moneda;
  }

  public BigDecimal getSaldo() {
    return saldo;
  }

  public String getEstadoCuenta() {
    return estadoCuenta;
  }

  public List<MovimientoDTO> getMovimientos() {
    return movimientos;
  }

  public static ConsultaResponseDTO fromJSON(String json) {
    JSONObject obj = new JSONObject(json);
    ConsultaResponseDTO dto = new ConsultaResponseDTO(leerEstado(obj), leerTexto(obj, "mensaje"));

    JSONObject cuenta = obj.optJSONObject("cuenta");
    if (cuenta != null) {
      dto.numeroCuenta = leerTexto(cuenta, "numeroCuenta");
      dto.titular = leerTexto(cuenta, "titular");
      dto.moneda = leerTexto(cuenta, "moneda");
      dto.saldo = leerDecimal(cuenta, "saldo");
      dto.estadoCuenta = leerTexto(cuenta, "estadoCuenta");
    }

    JSONArray lista = obj.optJSONArray("movimientos");
    if (lista != null) {
      for (int i = 0; i < lista.length(); i++) {
        JSONObject m = lista.getJSONObject(i);
        dto.movimientos.add(new MovimientoDTO(
          leerEntero(m, "numeroMovimiento"),
          leerTexto(m, "fecha"),
          leerTexto(m, "tipoMovimiento"),
          leerTexto(m, "accion"),
          leerDecimal(m, "importe"),
          leerTexto(m, "cuentaReferencia")
        ));
      }
    }

    return dto;
  }
}
