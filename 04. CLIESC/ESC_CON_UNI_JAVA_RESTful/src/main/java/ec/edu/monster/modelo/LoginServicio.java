package ec.edu.monster.modelo;

/**
 * Client-side access to the RESTful login service. Controllers depend on this
 * interface, never on the generated stubs, so they can be tested with a double.
 */
public interface LoginServicio {

    /**
     * Logs in on the server.
     *
     * @throws ServicioException when the server rejects the credentials or cannot be reached
     */
    DatosSesion iniciarSesion(String usuario, String clave) throws ServicioException;
}
