package ec.edu.monster.vista;

/**
 * Server status indicator (green or red dot). The window runs the registered
 * check when it opens and then every few seconds.
 */
public interface EstadoVista {

    void mostrarEstado(boolean disponible);

    void alRevisarEstado(Runnable accion);
}
