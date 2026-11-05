package ec.edu.monster.dobles;

import ec.edu.monster.modelo.HistorialCuenta;
import ec.edu.monster.vista.MovimientosVista;
import java.util.function.Consumer;

/**
 * Movement history view double: records what the controller asks it to show.
 */
public class MovimientosVistaFalsa implements MovimientosVista {

    private Consumer<String> accionConsultar = cuenta -> { };
    private String cuenta = "";
    private String error;
    private HistorialCuenta historial;
    private boolean erroresLimpiados;

    public void escribirCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public void consultar() {
        accionConsultar.accept(cuenta);
    }

    @Override
    public String getCuenta() {
        return cuenta;
    }

    @Override
    public void alConsultar(Consumer<String> accion) {
        this.accionConsultar = accion;
    }

    @Override
    public void limpiarErrores() {
        erroresLimpiados = true;
        error = null;
    }

    @Override
    public void mostrarError(String mensaje) {
        this.error = mensaje;
    }

    @Override
    public void mostrarHistorial(HistorialCuenta historial) {
        this.historial = historial;
    }

    public String getError() {
        return error;
    }

    public HistorialCuenta getHistorial() {
        return historial;
    }

    public boolean isErroresLimpiados() {
        return erroresLimpiados;
    }
}
