package ec.edu.monster.modelo;

/**
 * Client-side access to the RESTful login service. Controllers depend on this interface, never on
 * the HTTP details, so they can be tested with a double.
 */
public interface LoginServicio {

    /**
     * Employee login: user name and password.
     *
     * @throws ServicioException when the server rejects the credentials or cannot be reached
     */
    DatosSesion loginEmpleado(String usuario, String clave) throws ServicioException;

    /**
     * Client login: account number and account key.
     *
     * @throws ServicioException when the server rejects the credentials or cannot be reached
     */
    DatosSesion loginCliente(String cuenta, String clave) throws ServicioException;

    /**
     * Removes the token on the server.
     *
     * @throws ServicioException when the server cannot be reached; the local session is closed anyway
     */
    void logout(String token) throws ServicioException;
}
