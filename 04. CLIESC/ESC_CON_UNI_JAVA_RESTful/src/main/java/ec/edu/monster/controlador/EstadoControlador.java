package ec.edu.monster.controlador;

import ec.edu.monster.modelo.EstadoServidor;
import ec.edu.monster.vista.EstadoVista;

/**
 * Keeps the server indicator up to date: green while it answers, red when it does not.
 */
public class EstadoControlador {

    private final EstadoVista vista;
    private final EstadoServidor estado;

    public EstadoControlador(EstadoVista vista, EstadoServidor estado) {
        this.vista = vista;
        this.estado = estado;
        vista.alRevisarEstado(this::revisar);
    }

    private void revisar() {
        vista.mostrarEstado(estado.disponible());
    }
}
