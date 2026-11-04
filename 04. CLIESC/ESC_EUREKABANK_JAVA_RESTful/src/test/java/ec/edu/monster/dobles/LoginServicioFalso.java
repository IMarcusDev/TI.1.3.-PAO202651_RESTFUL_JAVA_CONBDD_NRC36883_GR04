package ec.edu.monster.dobles;

import ec.edu.monster.modelo.DatosSesion;
import ec.edu.monster.modelo.LoginServicio;
import ec.edu.monster.modelo.ServicioException;

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

    public void fallarCon(String mensaje) {
        this.error = new ServicioException(mensaje);
    }

    @Override
    public DatosSesion loginEmpleado(String usuario, String clave) throws ServicioException {
        return iniciar(usuario, clave);
    }

    @Override
    public DatosSesion loginCliente(String cuenta, String clave) throws ServicioException {
        return iniciar(cuenta, clave);
    }

    @Override
    public void logout(String token) throws ServicioException {
        tokenCerrado = token;
        if (error != null) {
            throw error;
        }
    }

    private DatosSesion iniciar(String usuario, String clave) throws ServicioException {
        llamadas++;
        usuarioRecibido = usuario;
        claveRecibida = clave;
        if (error != null) {
            throw error;
        }
        return SESION;
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
