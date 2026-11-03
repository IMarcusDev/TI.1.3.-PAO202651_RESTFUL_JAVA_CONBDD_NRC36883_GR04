package ec.edu.monster;

import ec.edu.monster.controlador.EstadoControlador;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.controlador.MovimientoControlador;
import ec.edu.monster.modelo.Comprobantes;
import ec.edu.monster.modelo.Configuracion;
import ec.edu.monster.modelo.EstadoServidor;
import ec.edu.monster.modelo.LoginServicio;
import ec.edu.monster.modelo.LoginServicioRest;
import ec.edu.monster.modelo.MovimientoServicio;
import ec.edu.monster.modelo.MovimientoServicioRest;
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

    private static final String TITULO = "EurekaBank";
    private static final String SIN_CONEXION = "No se pudo conectar con el servidor";

    private final Configuracion configuracion;
    private final Sesion sesion = new Sesion();
    private final EstadoServidor estado;

    private App(Configuracion configuracion) {
        this.configuracion = configuracion;
        this.estado = new EstadoServidor(configuracion);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::iniciar);
    }

    private static void iniciar() {
        Configuracion configuracion;
        try {
            configuracion = Configuracion.cargar();
        } catch (RuntimeException e) {
            error(e.getMessage());
            return;
        }
        new App(configuracion).mostrarLogin();
    }

    private void mostrarLogin() {
        LoginServicio servicio;
        try {
            // A malformed address in config.properties fails here; report it instead of crashing.
            servicio = new LoginServicioRest(configuracion);
        } catch (RuntimeException | LinkageError e) {
            error(SIN_CONEXION);
            return;
        }
        LoginFrame login = new LoginFrame(this::mostrarPrincipal);
        new LoginControlador(login, servicio, sesion);
        new EstadoControlador(login.getIndicadorEstado(), estado);
        login.setVisible(true);
    }

    private void mostrarPrincipal() {
        MovimientoServicio servicio;
        PrincipalFrame principal;
        try {
            servicio = new MovimientoServicioRest(configuracion);
            principal = new PrincipalFrame(sesion.getNombre(), sesion.getTipoUsuario(), this::cerrarSesion);
        } catch (RuntimeException | LinkageError e) {
            error(SIN_CONEXION);
            return;
        }
        conectar(principal, servicio);
        principal.setVisible(true);
    }

    private void conectar(PrincipalFrame principal, MovimientoServicio servicio) {
        new MovimientoControlador(principal.getDepositoPanel(), sesion,
                form -> "¿Depositar " + form.getImporteFormateado() + " en la cuenta " + form.getCuenta()
                        + " del empleado " + form.getEmpleado() + "?",
                (token, form) -> Comprobantes.deposito(form,
                        servicio.deposito(token, form.getEmpleado(), form.getCuenta(), form.getImporte())));

        new MovimientoControlador(principal.getRetiroPanel(), sesion,
                form -> "¿Retirar " + form.getImporteFormateado() + " de la cuenta " + form.getCuenta() + "?",
                (token, form) -> Comprobantes.retiro(form,
                        servicio.retiro(token, form.getCuenta(), "", form.getImporte())));

        new MovimientoControlador(principal.getTransferenciaPanel(), sesion,
                form -> "¿Transferir " + form.getImporteFormateado() + " de la cuenta " + form.getOrigen()
                        + " a la cuenta " + form.getDestino() + "?",
                (token, form) -> Comprobantes.transferencia(form,
                        servicio.transferencia(token, form.getOrigen(), form.getDestino(), form.getImporte())));
    }

    private void cerrarSesion() {
        sesion.cerrar();
        mostrarLogin();
    }

    private static void error(String mensaje) {
        JOptionPane.showOptionDialog(null, mensaje, TITULO, JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE, null, new Object[] {"Aceptar"}, "Aceptar");
    }
}
