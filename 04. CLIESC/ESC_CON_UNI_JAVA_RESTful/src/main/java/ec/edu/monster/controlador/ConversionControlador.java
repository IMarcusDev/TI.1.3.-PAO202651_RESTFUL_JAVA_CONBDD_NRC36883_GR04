package ec.edu.monster.controlador;

import ec.edu.monster.modelo.ConversorServicio;
import ec.edu.monster.modelo.FormatoResultado;
import ec.edu.monster.modelo.Magnitudes;
import ec.edu.monster.modelo.ServicioException;
import ec.edu.monster.vista.ConversionVista;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Handles the conversion screen: loads the units of the selected magnitude,
 * validates the input and shows the formatted result.
 */
public class ConversionControlador {

    private static final String VALOR_INVALIDO = "Ingrese un valor numérico válido";
    private static final String SIN_UNIDADES = "Seleccione la unidad de origen y de destino";
    private static final String SIN_MAGNITUD = "Seleccione una magnitud";
    // Optional sign, digits and at most one decimal separator ("." or ","). No exponents or suffixes.
    private static final Pattern NUMERO = Pattern.compile("[+-]?(\\d+([.,]\\d+)?|[.,]\\d+)");

    private final ConversionVista vista;
    private final ConversorServicio servicio;

    public ConversionControlador(ConversionVista vista, ConversorServicio servicio) {
        this.vista = vista;
        this.servicio = servicio;
        vista.alCambiarMagnitud(this::cargarUnidades);
        vista.alConvertir(this::convertir);
        // The view notifies the first magnitude in the background, loading its units.
        vista.mostrarMagnitudes(Magnitudes.TODAS);
    }

    private void cargarUnidades() {
        vista.limpiarMensajes();
        String magnitud = vista.getMagnitud();
        if (magnitud == null) {
            vista.mostrarUnidades(List.of());
            return;
        }
        try {
            vista.mostrarUnidades(servicio.catalogo(magnitud));
        } catch (ServicioException e) {
            // Never leave the previous magnitude's units selectable.
            vista.mostrarUnidades(List.of());
            vista.mostrarError(e.getMessage());
        }
    }

    private void convertir() {
        vista.limpiarMensajes();
        Double valor = leerValor(vista.getValor());
        if (valor == null) {
            vista.mostrarError(VALOR_INVALIDO);
            return;
        }
        String magnitud = vista.getMagnitud();
        if (magnitud == null) {
            vista.mostrarError(SIN_MAGNITUD);
            return;
        }
        String desde = vista.getUnidadOrigen();
        String hacia = vista.getUnidadDestino();
        if (desde == null || hacia == null) {
            vista.mostrarError(SIN_UNIDADES);
            return;
        }
        try {
            double resultado = servicio.convertir(magnitud, valor, desde, hacia);
            vista.mostrarResultado(FormatoResultado.formatear(valor, desde, resultado, hacia));
        } catch (ServicioException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    // Accepts plain decimals such as "2.5" or "2,5"; returns null for anything else.
    private static Double leerValor(String texto) {
        if (texto == null || !NUMERO.matcher(texto.trim()).matches()) {
            return null;
        }
        double valor = Double.parseDouble(texto.trim().replace(',', '.'));
        return Double.isFinite(valor) ? valor : null;
    }
}
