package ec.edu.monster.controlador;

import ec.edu.monster.modelo.DatosSesion;
import ec.edu.monster.modelo.LoginServicio;
import ec.edu.monster.modelo.ServicioException;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.vista.LoginVista;

/**
 * Handles the login screen: validates the input, asks the model to log in
 * and tells the view what to show next.
 */
public class LoginControlador {

    private static final String CREDENCIALES_VACIAS = "Ingrese usuario y contraseña";

    private final LoginVista vista;
    private final LoginServicio servicio;
    private final Sesion sesion;

    public LoginControlador(LoginVista vista, LoginServicio servicio, Sesion sesion) {
        this.vista = vista;
        this.servicio = servicio;
        this.sesion = sesion;
        vista.alIngresar(this::ingresar);
    }

    private void ingresar() {
        String usuario = vista.getUsuario();
        String clave = vista.getClave();
        if (estaVacio(usuario) || estaVacio(clave)) {
            vista.mostrarError(CREDENCIALES_VACIAS);
            return;
        }
        try {
            DatosSesion datos = servicio.iniciarSesion(usuario.trim(), clave);
            sesion.iniciar(usuario.trim(), datos);
            vista.abrirMenu();
        } catch (ServicioException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    private static boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
