package ec.edu.monster.vista;

import java.awt.Color;
import java.awt.FlowLayout;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * "Dot + text" line showing whether the server is reachable. While the panel is
 * on screen it runs the registered check right away and then every few seconds,
 * off the EDT, so a slow or dead server never freezes the window.
 */
public class IndicadorEstado extends JPanel implements EstadoVista {

    private static final int INTERVALO_MS = 5_000;
    private static final String DISPONIBLE = "Servicio disponible";
    private static final String NO_DISPONIBLE = "Servicio no disponible";
    private static final Logger LOG = Logger.getLogger(IndicadorEstado.class.getName());

    private final String prefijo;
    private final JLabel etiqueta = new JLabel();
    private final Timer temporizador;
    private final AtomicBoolean revisando = new AtomicBoolean();
    private volatile Runnable accionRevisar = () -> { };

    /** {@code prefijo} goes before the status text, e.g. "Sesión activa · ". */
    public IndicadorEstado(String prefijo) {
        this(prefijo, INTERVALO_MS);
    }

    public IndicadorEstado(String prefijo, int intervaloMs) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.prefijo = prefijo;
        setOpaque(false);
        etiqueta.setIconTextGap(8);
        etiqueta.setFont(Estilos.FUENTE_PEQUENA);
        pintar(true);
        add(etiqueta);
        temporizador = new Timer(intervaloMs, e -> revisar());
        temporizador.setInitialDelay(0);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        iniciar();
    }

    @Override
    public void removeNotify() {
        detener();
        super.removeNotify();
    }

    /** Starts checking: once now, then periodically. */
    public void iniciar() {
        temporizador.start();
    }

    public void detener() {
        temporizador.stop();
    }

    @Override
    public void mostrarEstado(boolean disponible) {
        Estilos.enEdt(() -> pintar(disponible));
    }

    @Override
    public void alRevisarEstado(Runnable accion) {
        this.accionRevisar = accion;
    }

    // Skips a tick while the previous check is still waiting for the server.
    private void revisar() {
        if (!revisando.compareAndSet(false, true)) {
            return;
        }
        CompletableFuture.runAsync(accionRevisar).whenComplete((resultado, error) -> {
            revisando.set(false);
            if (error != null) {
                LOG.log(Level.WARNING, "Server status check failed", error);
            }
        });
    }

    private void pintar(boolean disponible) {
        Color color = disponible ? Estilos.EXITO : Estilos.ERROR;
        etiqueta.setText(prefijo + (disponible ? DISPONIBLE : NO_DISPONIBLE));
        etiqueta.setForeground(color);
        etiqueta.setIcon(Estilos.punto(color));
    }
}
