package ec.edu.monster.modelos.dto;

import java.math.BigDecimal;

/**
 * Datos de la cuenta que devuelve consultarMovimientos.
 *
 * @author Marcos Escobar
 */
//revisado y aprovado por Marcos Escobar
public class CuentaDTO {
  // La base de datos no guarda la moneda: EurekaBank trabaja solo en dólares.
  public static final String MONEDA = "USD";

  private String numeroCuenta;
  private String titular;
  private String moneda;
  private BigDecimal saldo;
  private String estadoCuenta;

  public CuentaDTO() {
  }

  public CuentaDTO(String numeroCuenta, String titular, double saldo, String estadoCuenta) {
    this.numeroCuenta = numeroCuenta;
    this.titular = titular;
    this.moneda = MONEDA;
    this.saldo = MovimientoDTO.monto(saldo);
    this.estadoCuenta = estadoCuenta;
  }

  public String getNumeroCuenta() {
    return numeroCuenta;
  }

  public void setNumeroCuenta(String numeroCuenta) {
    this.numeroCuenta = numeroCuenta;
  }

  public String getTitular() {
    return titular;
  }

  public void setTitular(String titular) {
    this.titular = titular;
  }

  public String getMoneda() {
    return moneda;
  }

  public void setMoneda(String moneda) {
    this.moneda = moneda;
  }

  public BigDecimal getSaldo() {
    return saldo;
  }

  public void setSaldo(BigDecimal saldo) {
    this.saldo = saldo;
  }

  public String getEstadoCuenta() {
    return estadoCuenta;
  }

  public void setEstadoCuenta(String estadoCuenta) {
    this.estadoCuenta = estadoCuenta;
  }
}
