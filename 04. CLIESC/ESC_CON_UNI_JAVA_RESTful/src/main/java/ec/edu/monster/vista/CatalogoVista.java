package ec.edu.monster.vista;

import java.util.List;

/**
 * Contract between the catalog screen and its controller.
 */
public interface CatalogoVista {

    /**
     * Shows the magnitudes and selects the first one. When the list is not
     * empty, the action registered with {@code alCambiarMagnitud} is notified
     * in the background, exactly like a user selection, so the initial units
     * load never runs on the EDT.
     */
    void mostrarMagnitudes(List<String> magnitudes);

    String getMagnitud();

    void mostrarUnidades(List<String> unidades);

    void mostrarError(String mensaje);

    /** Hides any previous error before loading a new catalog. */
    void limpiarMensajes();

    void alCambiarMagnitud(Runnable accion);
}
