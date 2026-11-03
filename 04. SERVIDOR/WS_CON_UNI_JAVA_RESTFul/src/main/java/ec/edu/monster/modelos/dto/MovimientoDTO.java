package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.data.Movimiento;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

/**
 * Un movimiento visto desde una cuenta, como lo devuelve consultarMovimientos.
 *
 * @author Marcos Escobar
 */
//revisado y aprovado por Marcos Escobar
public class MovimientoDTO {
  private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  private int numeroMovimiento;
  private String fecha;
  private String tipoMovimiento;
  private String accion;
  private BigDecimal importe;
  private String cuentaReferencia;

  public MovimientoDTO() {
  }

  /**
   * La base guarda una transferencia en una sola fila (origen → destino). Para la
   * cuenta de origen es una SALIDA y para la de destino un INGRESO; la cuenta de
   * referencia es la otra cuenta. Un depósito es INGRESO y un retiro, SALIDA.
   */
  public static MovimientoDTO desde(Movimiento m, String cuenta) {
    MovimientoDTO dto = new MovimientoDTO();
    dto.numeroMovimiento = Integer.parseInt(m.getId());
    dto.fecha = m.getFecha() == null ? "" : m.getFecha().format(FORMATO_FECHA);
    dto.importe = monto(m.getValor());

    switch (m.getTipo()) {
      case "DEPOSITO" -> {
        dto.tipoMovimiento = "Depósito";
        dto.accion = "INGRESO";
      }
      case "RETIRO" -> {
        dto.tipoMovimiento = "Retiro";
        dto.accion = "SALIDA";
      }
      default -> {
        boolean salida = cuenta.equals(m.getCuentaOrigen());
        dto.tipoMovimiento = "Transferencia";
        dto.accion = salida ? "SALIDA" : "INGRESO";
        dto.cuentaReferencia = salida ? m.getCuentaDestino() : m.getCuentaOrigen();
      }
    }
    return dto;
  }

  /** Importe con dos decimales, para no enviar valores como 1.0E7. */
  public static BigDecimal monto(double valor) {
    return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
  }

  public int getNumeroMovimiento() {
    return numeroMovimiento;
  }

  public void setNumeroMovimiento(int numeroMovimiento) {
    this.numeroMovimiento = numeroMovimiento;
  }

  public String getFecha() {
    return fecha;
  }

  public void setFecha(String fecha) {
    this.fecha = fecha;
  }

  public String getTipoMovimiento() {
    return tipoMovimiento;
  }

  public void setTipoMovimiento(String tipoMovimiento) {
    this.tipoMovimiento = tipoMovimiento;
  }

  public String getAccion() {
    return accion;
  }

  public void setAccion(String accion) {
    this.accion = accion;
  }

  public BigDecimal getImporte() {
    return importe;
  }

  public void setImporte(BigDecimal importe) {
    this.importe = importe;
  }

  public String getCuentaReferencia() {
    return cuentaReferencia;
  }

  public void setCuentaReferencia(String cuentaReferencia) {
    this.cuentaReferencia = cuentaReferencia;
  }
}
