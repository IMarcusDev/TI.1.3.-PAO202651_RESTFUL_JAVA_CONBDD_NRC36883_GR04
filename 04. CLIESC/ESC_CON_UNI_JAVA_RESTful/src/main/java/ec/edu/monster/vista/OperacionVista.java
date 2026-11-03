package ec.edu.monster.vista;

import ec.edu.monster.modelo.Comprobante;
import ec.edu.monster.modelo.MovimientoForm;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Contract between an operation screen and its controller. The Swing panel
 * implements it; tests use a fake, so the controller never depends on Swing.
 */
public interface OperacionVista {

    /**
     * Registers the action to run when the user presses "Revisar". The form is
     * read on the EDT, before the action starts, so the controller never touches
     * Swing components.
     */
    void alRevisar(Consumer<MovimientoForm> accion);

    /** Removes the messages shown by a previous attempt. */
    void limpiarErrores();

    /** Shows one message under each field, keyed by field name. */
    void mostrarErrores(Map<String, String> errores);

    /** Shows a message that does not belong to a single field. */
    void mostrarError(String mensaje);

    /** Asks the user to confirm the operation; true only when they accept. */
    boolean confirmar(String mensaje);

    /** Shows the receipt of a movement accepted by the server. */
    void mostrarComprobante(Comprobante comprobante);

    /** Clears the form for a new operation. */
    void limpiar();
}
