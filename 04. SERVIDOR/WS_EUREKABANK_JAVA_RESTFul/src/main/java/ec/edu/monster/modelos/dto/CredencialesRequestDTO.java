package ec.edu.monster.modelos.dto;

/**
 * Cuerpo de Login/loginEmpleado ({usuario, clave}), Login/loginCliente ({cuenta, clave})
 * y Login/logout ({token}).
 *
 * @author Marcos Escobar
 */
//revisado y aprovado por Marcos Escobar
public class CredencialesRequestDTO {
  private String usuario;
  private String cuenta;
  private String clave;
  private String token;

  public CredencialesRequestDTO() {
  }

  public String getUsuario() {
    return usuario;
  }

  public void setUsuario(String usuario) {
    this.usuario = usuario;
  }

  public String getCuenta() {
    return cuenta;
  }

  public void setCuenta(String cuenta) {
    this.cuenta = cuenta;
  }

  public String getClave() {
    return clave;
  }

  public void setClave(String clave) {
    this.clave = clave;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
