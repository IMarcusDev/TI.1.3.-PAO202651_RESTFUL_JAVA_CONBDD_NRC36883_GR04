package ec.edu.monster.modelo;

/**
 * Data returned by the server when the login succeeds. The profile tells which kind of user
 * logged in ({@link #EMPLEADO} or {@link #CLIENTE}) and therefore which screens and operations
 * are allowed.
 */
public record DatosSesion(String nombre, String tipoUsuario, String perfil, String token) {

    /** Profile values the server sends (same as its {@code Sesion} constants). */
    public static final String EMPLEADO = "EMPLEADO";
    public static final String CLIENTE = "CLIENTE";
}
