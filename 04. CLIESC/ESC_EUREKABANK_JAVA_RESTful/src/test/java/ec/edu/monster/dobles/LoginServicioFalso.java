package ec.edu.monster.dobles;

import ec.edu.monster.modelo.DatosSesion;
import ec.edu.monster.modelo.LoginServicio;
import ec.edu.monster.modelo.ServicioException;
import ec.edu.monster.modelo.Sesion;

/**
 * Login service double: returns a fixed session or the configured error.
 */
public class LoginServicioFalso implements LoginServicio {

    public static final DatosSesion SESION =
            new DatosSesion("Administrador", "Empleado", DatosSesion.EMPLEADO, "token-de-prueba");

    private ServicioException error;
    private String usuarioRecibido;
    private String claveRecibida;
    private String tokenCerrado;
    private int llamadas;
    private String rolRecibido;
    private DatosSesion respuesta = SESION;

    /** Session returned by the next logins (e.g. a Cliente session). */
    public void responderCon(DatosSesion datos) {
        this.respuesta = datos;
    }

    public void fallarCon(String mensaje) {
        this.error = new ServicioException(mensaje);
    }

    @Override
    public DatosSesion loginEmpleado(String usuario, String clave) throws ServicioException {
        return iniciar(Sesion.ROL_EMPLEADO, usuario, clave);
    }

    @Override
    public DatosSesion loginCliente(String cuenta, String clave) throws ServicioException {
        return iniciar(Sesion.ROL_CLIENTE, cuenta, clave);
    }

    @Override
    public void logout(String token) throws ServicioException {
        tokenCerrado = token;
        if (error != null) {
            throw error;
        }
    }

    private DatosSesion iniciar(String rol, String usuario, String clave) throws ServicioException {
        llamadas++;
        rolRecibido = rol;
        usuarioRecibido = usuario;
        claveRecibida = clave;
        if (error != null) {
            throw error;
        }
        return respuesta;
    }

    /** "Empleado" or "Cliente": which login was called last. */
    public String getRolRecibido() {
        return rolRecibido;
    }

    public String getUsuarioRecibido() {
        return usuarioRecibido;
    }

    public String getClaveRecibida() {
        return claveRecibida;
    }

    public String getTokenCerrado() {
        return tokenCerrado;
    }

    public int getLlamadas() {
        return llamadas;
    }
}
