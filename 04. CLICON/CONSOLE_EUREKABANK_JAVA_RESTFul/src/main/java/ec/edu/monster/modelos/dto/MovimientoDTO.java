package ec.edu.monster.modelos.dto;

import java.math.BigDecimal;

/** Un movimiento de la cuenta consultada. */
//revisado y aprovado por Marcos Escobar
public class MovimientoDTO {
  private int numeroMovimiento;
  private String fecha = "";
  private String tipoMovimiento = "";
  private String accion = "";
  private BigDecimal importe;
  private String cuentaReferencia = "";

  public MovimientoDTO(int numeroMovimiento, String fecha, String tipoMovimiento, String accion,
      BigDecimal importe, String cuentaReferencia) {
    this.numeroMovimiento = numeroMovimiento;
    this.fecha = fecha;
    this.tipoMovimiento = tipoMovimiento;
    this.accion = accion;
    this.importe = importe;
    this.cuentaReferencia = cuentaReferencia;
  }

  public int getNumeroMovimiento() {
    return numeroMovimiento;
  }

  public String getFecha() {
    return fecha;
  }

  public String getTipoMovimiento() {
    return tipoMovimiento;
  }

  public String getAccion() {
    return accion;
  }

  public BigDecimal getImporte() {
    return importe;
  }

  public String getCuentaReferencia() {
    return cuentaReferencia;
  }

  public boolean esIngreso() {
    return "INGRESO".equalsIgnoreCase(accion);
  }
}
