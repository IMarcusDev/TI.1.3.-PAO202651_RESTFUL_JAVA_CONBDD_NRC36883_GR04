package ec.edu.monster.vista;

/**
 * Contract between the server status indicator and its controller.
 */
public interface EstadoVista {

    void mostrarEstado(boolean disponible);

    void alRevisarEstado(Runnable accion);
}
