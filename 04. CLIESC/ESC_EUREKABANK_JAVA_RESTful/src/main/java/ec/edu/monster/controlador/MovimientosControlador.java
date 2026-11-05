package ec.edu.monster.controlador;

import ec.edu.monster.modelo.HistorialCuenta;
import ec.edu.monster.modelo.MovimientoServicio;
import ec.edu.monster.modelo.ServicioException;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.vista.MovimientosVista;

/**
 * Handles the "Consultar movimientos" screen: validates the account, asks the
 * server for the history and shows the account data with its movements.
 */
public class MovimientosControlador {

    private static final String CUENTA_VACIA = "Ingrese el número de cuenta";
    private static final String SESION_INACTIVA = "La sesión no está activa. Vuelva a iniciar sesión.";

    private final MovimientosVista vista;
    private final Sesion sesion;
    private final MovimientoServicio servicio;

    public MovimientosControlador(MovimientosVista vista, Sesion sesion, MovimientoServicio servicio) {
        this.vista = vista;
        this.sesion = sesion;
        this.servicio = servicio;
        vista.alConsultar(this::consultar);
    }

    // Runs off the EDT: the view read the account before calling this method.
    // A Cliente sends the account fixed by the panel (their own, from the session).
    private void consultar(String cuenta) {
        vista.limpiarErrores();
        String numero = cuenta == null ? "" : cuenta.trim();
        if (numero.isEmpty()) {
            vista.mostrarError(CUENTA_VACIA);
            return;
        }
        if (!sesion.estaActiva()) {
            vista.mostrarError(SESION_INACTIVA);
            return;
        }
        try {
            HistorialCuenta historial = servicio.consultarMovimientos(sesion.getToken(), numero);
            vista.mostrarHistorial(historial);
        } catch (ServicioException e) {
            // Unknown account, forbidden role, server down...: the message comes from the model
            vista.mostrarError(e.getMessage());
        }
    }
}
