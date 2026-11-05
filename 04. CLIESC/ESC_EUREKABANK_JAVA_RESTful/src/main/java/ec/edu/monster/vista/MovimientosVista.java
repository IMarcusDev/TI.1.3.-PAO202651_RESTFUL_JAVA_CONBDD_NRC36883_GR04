package ec.edu.monster.vista;

import ec.edu.monster.modelo.HistorialCuenta;
import java.util.function.Consumer;

/**
 * Contract between the "Consultar movimientos" screen and its controller. The
 * Swing panel implements it; tests use a fake, so the controller never depends
 * on Swing.
 */
public interface MovimientosVista {

    /** Account typed by the user, or the one fixed for a Cliente. */
    String getCuenta();

    /**
     * Registers the action to run when the user asks to consult. The account is
     * read on the EDT, before the action starts, so the controller never touches
     * Swing components.
     */
    void alConsultar(Consumer<String> accion);

    /** Removes the messages shown by a previous attempt. */
    void limpiarErrores();

    /** Shows a message that does not belong to a field. */
    void mostrarError(String mensaje);

    /** Shows the account data and its movements (an empty list is a valid answer). */
    void mostrarHistorial(HistorialCuenta historial);
}
