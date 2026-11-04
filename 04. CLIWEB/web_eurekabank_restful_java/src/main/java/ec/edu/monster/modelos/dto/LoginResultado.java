package ec.edu.monster.modelos.dto;

/**
 * Answer of api/Login/loginEmpleado. token, nombre and tipoUsuario
 * are only filled when estado is OK.
 */
public record LoginResultado(
    EstadosEnum estado, String mensaje, String token, String nombre, String tipoUsuario) {

  public static LoginResultado error(String mensaje) {
    return new LoginResultado(EstadosEnum.ERROR, mensaje, null, null, null);
  }
}
