package ec.edu.monster;

import ec.edu.monster.controlador.CatalogoControlador;
import ec.edu.monster.controlador.ConversionControlador;
import ec.edu.monster.controlador.EstadoControlador;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelo.ConversorServicio;
import ec.edu.monster.modelo.EstadoServidor;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.vista.LoginFrame;
import ec.edu.monster.vista.PrincipalFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Composition root: builds the model once and wires each window with its
 * controllers, handling navigation between login and the main window.
 */
public final class App {

    private static final String TITULO = "Conversión de Unidades Monster";
    private static final String SIN_CONEXION = "No se pudo conectar con el servidor";

    private final ConversorServicio servicio;
    private final EstadoServidor estado = new EstadoServidor();

    private App(ConversorServicio servicio) {
        this.servicio = servicio;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::iniciar);
    }

    private static void iniciar() {
        ConversorServicio servicio;
        try {
            // Building the HTTP client can fail (e.g. invalid configuration); fail gracefully.
            servicio = ConversorServicio.crear(new Sesion());
        } catch (RuntimeException e) {
            JOptionPane.showOptionDialog(null, SIN_CONEXION, TITULO,
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                    new Object[] {"Aceptar"}, "Aceptar");
            return;
        }
        new App(servicio).mostrarLogin();
    }

    private void mostrarLogin() {
        LoginFrame login = new LoginFrame(this::mostrarPrincipal);
        new LoginControlador(login, servicio);
        new EstadoControlador(login.getIndicadorEstado(), estado);
        login.setVisible(true);
    }

    private void mostrarPrincipal() {
        // The frame disposes itself after this callback runs.
        PrincipalFrame principal = new PrincipalFrame(() -> {
            try {
                servicio.cerrarSesion();
            } finally {
                mostrarLogin();
            }
        });
        new ConversionControlador(principal.getConversionPanel(), servicio);
        new CatalogoControlador(principal.getCatalogoPanel(), servicio);
        new EstadoControlador(principal.getIndicadorEstado(), estado);
        principal.setVisible(true);
    }
}
