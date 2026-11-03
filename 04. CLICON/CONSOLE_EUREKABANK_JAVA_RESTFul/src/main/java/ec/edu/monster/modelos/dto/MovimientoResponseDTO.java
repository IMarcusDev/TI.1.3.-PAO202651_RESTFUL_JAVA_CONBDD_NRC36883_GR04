package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.enums.EstadosEnum;
import java.math.BigDecimal;
import org.json.JSONObject;

/** Resultado de una operación; el comprobante (RNF-010) muestra el número de movimiento y el nuevo saldo. */
//revisado y aprovado por Marcos Escobar
public class MovimientoResponseDTO extends ResponseDTO {
  private int idMovimiento;
  private int numeroMovimiento;
  private BigDecimal nuevoSaldo;

  public MovimientoResponseDTO(EstadosEnum estado, String mensaje, int idMovimiento) {
    super(estado, mensaje);
    this.idMovimiento = idMovimiento;
  }

  public int getIdMovimiento() {
    return idMovimiento;
  }

  public int getNumeroMovimiento() {
    return numeroMovimiento;
  }

  public BigDecimal getNuevoSaldo() {
    return nuevoSaldo;
  }

  public static MovimientoResponseDTO fromJSON(String json) {
    JSONObject obj = new JSONObject(json);

    MovimientoResponseDTO dto = new MovimientoResponseDTO(
      leerEstado(obj), leerTexto(obj, "mensaje"), leerEntero(obj, "idMovimiento"));
    dto.numeroMovimiento = leerEntero(obj, "numeroMovimiento");
    dto.nuevoSaldo = leerDecimal(obj, "nuevoSaldo");
    return dto;
  }
}
