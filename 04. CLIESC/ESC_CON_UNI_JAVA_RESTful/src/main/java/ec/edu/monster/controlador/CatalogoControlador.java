package ec.edu.monster.controlador;

import ec.edu.monster.modelo.ConversorServicio;
import ec.edu.monster.modelo.Magnitudes;
import ec.edu.monster.modelo.ServicioException;
import ec.edu.monster.vista.CatalogoVista;
import java.util.List;

/**
 * Handles the catalog screen: lists the units available for the selected
 * magnitude.
 */
public class CatalogoControlador {

    private final CatalogoVista vista;
    private final ConversorServicio servicio;

    public CatalogoControlador(CatalogoVista vista, ConversorServicio servicio) {
        this.vista = vista;
        this.servicio = servicio;
        vista.alCambiarMagnitud(this::cargarUnidades);
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
            // Never leave the previous magnitude's units on screen.
            vista.mostrarUnidades(List.of());
            vista.mostrarError(e.getMessage());
        }
    }
}
