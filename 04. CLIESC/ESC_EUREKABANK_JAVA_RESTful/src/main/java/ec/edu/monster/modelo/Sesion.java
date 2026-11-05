package ec.edu.monster.modelo;

/**
 * Login state of the desktop client: who is logged in and the token the
 * movement services need. Closed when the user logs out.
 */
public class Sesion {

    /** Must match the user type name returned by the server. */
    public static final String ROL_EMPLEADO = "Empleado";
    public static final String ROL_CLIENTE = "Cliente";

    private String usuario;
    private String nombre;
    private String tipoUsuario;
    private String token;

    public void iniciar(String usuario, DatosSesion datos) {
        this.usuario = usuario;
        this.nombre = datos.nombre();
        this.tipoUsuario = datos.tipoUsuario();
        this.token = datos.token();
    }

    public void cerrar() {
        usuario = null;
        nombre = null;
        tipoUsuario = null;
        token = null;
    }

    public boolean estaActiva() {
        return token != null && !token.isBlank();
    }

    /** True for a bank teller: works on any account and is the only one who deposits. */
    public boolean esEmpleado() {
        return ROL_EMPLEADO.equals(tipoUsuario);
    }

    /** True for an account holder: works only on the account used to log in. */
    public boolean esCliente() {
        return ROL_CLIENTE.equals(tipoUsuario);
    }

    /** Account a Cliente logged in with (their user name), or null for any other role. */
    public String getCuenta() {
        return esCliente() ? usuario : null;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getNombre() {
        return nombre == null || nombre.isBlank() ? usuario : nombre;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public String getToken() {
        return token;
    }
}
