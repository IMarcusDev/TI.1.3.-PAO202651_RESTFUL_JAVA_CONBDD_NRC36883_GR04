package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.enums.EstadosEnum;
import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta de consultarMovimientos: los datos de la cuenta y sus movimientos.
 *
 * @author Marcos Escobar
 */
//revisado y aprovado por Marcos Escobar
public class ConsultaMovimientosResponseDTO extends ResponseDTO {
  private CuentaDTO cuenta;
  private List<MovimientoDTO> movimientos = new ArrayList<>();

  public ConsultaMovimientosResponseDTO(EstadosEnum estado, String mensaje) {
    super(estado, mensaje);
  }

  public CuentaDTO getCuenta() {
    return cuenta;
  }

  public void setCuenta(CuentaDTO cuenta) {
    this.cuenta = cuenta;
  }

  public List<MovimientoDTO> getMovimientos() {
    return movimientos;
  }

  public void setMovimientos(List<MovimientoDTO> movimientos) {
    this.movimientos = movimientos;
  }
}
