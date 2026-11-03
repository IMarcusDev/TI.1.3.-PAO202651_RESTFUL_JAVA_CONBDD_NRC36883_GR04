package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Comprobante;
import ec.edu.monster.modelo.MovimientoForm;
import ec.edu.monster.modelo.ServicioException;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.vista.OperacionVista;
import java.util.function.Function;

/**
 * Handles one movement screen (deposit, withdrawal or transfer): validates the
 * form, asks for confirmation, calls the server and shows the receipt. The three
 * screens share this controller; the composition root tells it how to word the
 * question and which service call to make.
 */
public class MovimientoControlador {

    /** Runs the operation on the server and builds the receipt shown to the user. */
    @FunctionalInterface
    public interface Operacion {

        Comprobante ejecutar(String token, MovimientoForm form) throws ServicioException;
    }

    private static final String SESION_INACTIVA = "La sesión no está activa. Vuelva a iniciar sesión.";

    private final OperacionVista vista;
    private final Sesion sesion;
    private final Function<MovimientoForm, String> pregunta;
    private final Operacion operacion;

    public MovimientoControlador(OperacionVista vista, Sesion sesion,
            Function<MovimientoForm, String> pregunta, Operacion operacion) {
        this.vista = vista;
        this.sesion = sesion;
        this.pregunta = pregunta;
        this.operacion = operacion;
        vista.alRevisar(this::revisar);
    }

    // Runs off the EDT: the view read the form before calling this method.
    private void revisar(MovimientoForm form) {
        vista.limpiarErrores();
        if (!form.isValido()) {
            vista.mostrarErrores(form.getErrores());
            return;
        }
        if (!vista.confirmar(pregunta.apply(form))) {
            return;
        }
        if (!sesion.estaActiva()) {
            vista.mostrarError(SESION_INACTIVA);
            return;
        }
        try {
            Comprobante comprobante = operacion.ejecutar(sesion.getToken(), form);
            vista.mostrarComprobante(comprobante);
            vista.limpiar();
        } catch (ServicioException e) {
            // Token vencido, saldo insuficiente, servidor caído...: the form is kept
            vista.mostrarError(e.getMessage());
        }
    }
}
