package ec.edu.monster.modelo;

/**
 * Error reported by the REST service, or by the connection to it.
 */
public class ServicioException extends Exception {

    /** Shown when the server cannot be reached (ECUD). */
    public static final String SIN_CONEXION =
            "No se pudo conectar con el servidor. Verifique la red e intente nuevamente.";

    private static final String MENSAJE_POR_DEFECTO = "Error del servidor";

    public ServicioException(String mensaje) {
        super(mensaje);
    }

    /** Returns the server message, or a readable default when it came empty. */
    public static String mensaje(String delServidor) {
        return delServidor == null || delServidor.isBlank() ? MENSAJE_POR_DEFECTO : delServidor.trim();
    }

    /** Connection error, the same text for a dead server, a wrong host or a network failure. */
    public static ServicioException sinConexion() {
        return new ServicioException(SIN_CONEXION);
    }
}
