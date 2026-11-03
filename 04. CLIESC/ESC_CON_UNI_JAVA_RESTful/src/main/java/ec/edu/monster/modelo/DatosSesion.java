package ec.edu.monster.modelo;

/**
 * Data returned by the server when the login succeeds.
 */
public record DatosSesion(String nombre, String tipoUsuario, String token) {
}
