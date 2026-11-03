package ec.edu.monster.modelos.data;

/**
 * Dueño de un token: el perfil (EMPLEADO o CLIENTE) y el código del empleado
 * o el número de la cuenta con el que se inició sesión.
 *
 * @author Marcos Escobar
 */
//revisado y aprovado por Marcos Escobar
public record Sesion(String perfil, String codigo) {
  public static final String EMPLEADO = "EMPLEADO";
  public static final String CLIENTE = "CLIENTE";

  public boolean esCliente() {
    return CLIENTE.equals(perfil);
  }
}
