package ec.edu.monster.controlador;

import ec.edu.monster.modelo.ConversorServicio;
import ec.edu.monster.modelo.ServicioException;
import ec.edu.monster.vista.LoginVista;

/**
 * Handles the login screen: validates the input, asks the model to log in
 * and tells the view what to show next.
 */
public class LoginControlador {

    private static final String CREDENCIALES_VACIAS = "Ingrese usuario y contraseña";

    private final LoginVista vista;
    private final ConversorServicio servicio;

    public LoginControlador(LoginVista vista, ConversorServicio servicio) {
        this.vista = vista;
        this.servicio = servicio;
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
            servicio.iniciarSesion(usuario.trim(), clave);
            vista.abrirMenu();
        } catch (ServicioException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    private static boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
