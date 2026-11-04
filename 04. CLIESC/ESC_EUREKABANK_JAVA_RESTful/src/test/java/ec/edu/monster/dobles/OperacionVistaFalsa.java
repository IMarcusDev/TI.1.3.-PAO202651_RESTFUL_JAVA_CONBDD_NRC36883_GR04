package ec.edu.monster.dobles;

import ec.edu.monster.modelo.Comprobante;
import ec.edu.monster.modelo.MovimientoForm;
import ec.edu.monster.vista.OperacionVista;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Operation view double: records what the controller asks it to show.
 */
public class OperacionVistaFalsa implements OperacionVista {

    private Consumer<MovimientoForm> accionRevisar = form -> { };
    private boolean aceptar = true;
    private String pregunta;
    private Map<String, String> errores;
    private String error;
    private Comprobante comprobante;
    private boolean limpiada;
    private boolean erroresLimpiados;

    public void setAceptar(boolean aceptar) {
        this.aceptar = aceptar;
    }

    public void revisar(MovimientoForm form) {
        accionRevisar.accept(form);
    }

    @Override
    public void alRevisar(Consumer<MovimientoForm> accion) {
        this.accionRevisar = accion;
    }

    @Override
    public void limpiarErrores() {
        erroresLimpiados = true;
        errores = null;
        error = null;
    }

    @Override
    public void mostrarErrores(Map<String, String> errores) {
        this.errores = errores;
    }

    @Override
    public void mostrarError(String mensaje) {
        this.error = mensaje;
    }

    @Override
    public boolean confirmar(String mensaje) {
        this.pregunta = mensaje;
        return aceptar;
    }

    @Override
    public void mostrarComprobante(Comprobante comprobante) {
        this.comprobante = comprobante;
    }

    @Override
    public void limpiar() {
        limpiada = true;
    }

    public String getPregunta() {
        return pregunta;
    }

    public Map<String, String> getErrores() {
        return errores;
    }

    public String getError() {
        return error;
    }

    public Comprobante getComprobante() {
        return comprobante;
    }

    public boolean isLimpiada() {
        return limpiada;
    }

    public boolean isErroresLimpiados() {
        return erroresLimpiados;
    }
}
