package ec.edu.monster.modelo;

/**
 * What every screen does when the server rejects the token (ECUD4 FA1, ECUD6 FA2): close the
 * session, go back to the login and show a notice there.
 */
public final class Sesiones {

    public static final String AVISO_EXPIRADA = "Su sesión expiró o no es válida. Inicie sesión nuevamente.";

    // Message the server sends for an unknown or expired token
    public static final String TOKEN_INVALIDO = "Token inválido o sesión expirada";

    private Sesiones() {
    }

    public static boolean esSesionExpirada(String mensajeServidor) {
        return TOKEN_INVALIDO.equals(mensajeServidor);
    }
}
