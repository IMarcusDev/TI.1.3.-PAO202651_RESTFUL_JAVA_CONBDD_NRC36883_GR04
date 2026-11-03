package ec.edu.monster.pruebas;

import static ec.edu.monster.pruebas.ConversionPanelTest.buscar;
import static ec.edu.monster.pruebas.ConversionPanelTest.enEdt;
import static ec.edu.monster.pruebas.ConversionPanelTest.esperarHasta;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ec.edu.monster.vista.IndicadorEstado;
import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;

/**
 * Headless checks of the server status line shown at the bottom of each window.
 */
class IndicadorEstadoTest {

    private static final Color VERDE = new Color(0x15803D);
    private static final Color ROJO = new Color(0xB91C1C);

    @Test
    void turnsRedWhenTheServerGoesDownAndGreenWhenItComesBack() throws Exception {
        IndicadorEstado indicador = enEdt(() -> new IndicadorEstado("Sesión activa · "));
        JLabel etiqueta = enEdt(() -> buscar(indicador, JLabel.class).<JLabel>getFirst());
        assertEquals("Sesión activa · Servicio disponible", enEdt(etiqueta::getText));

        indicador.mostrarEstado(false);
        esperarHasta(() -> "Sesión activa · Servicio no disponible".equals(etiqueta.getText()));
        assertEquals(ROJO, enEdt(etiqueta::getForeground));

        indicador.mostrarEstado(true);
        esperarHasta(() -> "Sesión activa · Servicio disponible".equals(etiqueta.getText()));
        assertEquals(VERDE, enEdt(etiqueta::getForeground));
    }

    @Test
    void checksRightAwayThenPeriodicallyUntilStopped() throws Exception {
        IndicadorEstado indicador = enEdt(() -> new IndicadorEstado("", 20));
        AtomicInteger revisiones = new AtomicInteger();
        indicador.alRevisarEstado(revisiones::incrementAndGet);

        enEdt(indicador::iniciar);
        esperarHasta(() -> revisiones.get() >= 3);
        enEdt(indicador::detener);
        Thread.sleep(50);
        int alDetener = revisiones.get();
        Thread.sleep(100);

        assertEquals(alDetener, revisiones.get());
    }
}
