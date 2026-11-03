package ec.edu.monster.modelo;

/**
 * Error reported by the REST service, or by the connection to it.
 */
public class ServicioException extends Exception {

    private static final String MENSAJE_POR_DEFECTO = "Error del servidor";

    public ServicioException(String mensaje) {
        super(mensaje);
    }

    /** Returns the server message, or a readable default when it came empty. */
    public static String mensaje(String delServidor) {
        return delServidor == null || delServidor.isBlank() ? MENSAJE_POR_DEFECTO : delServidor.trim();
    }
}
