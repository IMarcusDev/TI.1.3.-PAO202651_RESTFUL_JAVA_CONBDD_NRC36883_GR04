package ec.edu.monster.modelos.dto;

import java.math.BigDecimal;

/**
 * Answer of a deposit, withdrawal or transfer. numeroMovimiento and nuevoSaldo
 * are only meaningful when estado is OK (nuevoSaldo may be null if the server omits it).
 */
public record MovimientoResultado(
    EstadosEnum estado, String mensaje, int numeroMovimiento, BigDecimal nuevoSaldo) {

  /** Non-OK answer (ERROR or SESION_EXPIRADA) carrying only the message to show. */
  public static MovimientoResultado fallo(EstadosEnum estado, String mensaje) {
    return new MovimientoResultado(estado, mensaje, 0, null);
  }

  public static MovimientoResultado error(String mensaje) {
    return fallo(EstadosEnum.ERROR, mensaje);
  }
}
