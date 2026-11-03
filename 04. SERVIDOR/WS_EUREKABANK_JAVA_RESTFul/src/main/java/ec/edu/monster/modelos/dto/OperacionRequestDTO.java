package ec.edu.monster.modelos.dto;

import java.math.BigDecimal;

/**
 * Cuerpo de las operaciones del ERS en el recurso Movimiento:
 * consultarMovimientos {token, cuenta}, registrarDeposito {token, cuenta, importe},
 * registrarRetiro {token, cuenta, importe, clave} y
 * registrarTransferencia {token, cuentaOrigen, cuentaDestino, importe, clave}.
 *
 * @author Marcos Escobar
 */
//revisado y aprovado por Marcos Escobar
public class OperacionRequestDTO {
  private String token;
  private String cuenta;
  private String cuentaOrigen;
  private String cuentaDestino;
  private BigDecimal importe;
  private String clave;

  public OperacionRequestDTO() {
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getCuenta() {
    return cuenta;
  }

  public void setCuenta(String cuenta) {
    this.cuenta = cuenta;
  }

  public String getCuentaOrigen() {
    return cuentaOrigen;
  }

  public void setCuentaOrigen(String cuentaOrigen) {
    this.cuentaOrigen = cuentaOrigen;
  }

  public String getCuentaDestino() {
    return cuentaDestino;
  }

  public void setCuentaDestino(String cuentaDestino) {
    this.cuentaDestino = cuentaDestino;
  }

  public BigDecimal getImporte() {
    return importe;
  }

  public void setImporte(BigDecimal importe) {
    this.importe = importe;
  }

  public String getClave() {
    return clave;
  }

  public void setClave(String clave) {
    this.clave = clave;
  }
}
