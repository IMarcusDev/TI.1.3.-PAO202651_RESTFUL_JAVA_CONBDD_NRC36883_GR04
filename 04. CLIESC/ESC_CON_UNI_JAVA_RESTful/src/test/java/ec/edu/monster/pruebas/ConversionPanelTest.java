package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import ec.edu.monster.vista.ConversionPanel;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Headless checks of the view threading contract: snapshots, programmatic fills
 * and the controls locked while a background action runs.
 */
class ConversionPanelTest {

    private static final String ERROR_INESPERADO = "Ocurrió un error inesperado";

    private ConversionPanel panel;
    private JComboBox<?> comboCategoria;
    private JComboBox<?> comboDesde;
    private JComboBox<?> comboHacia;
    private JTextField campoValor;
    private JButton botonConvertir;

    @BeforeEach
    void crearPanel() throws Exception {
        panel = enEdt(ConversionPanel::new);
        List<JComboBox<?>> combos = buscar(panel, JComboBox.class);
        comboCategoria = combos.get(0);
        comboDesde = combos.get(1);
        comboHacia = combos.get(2);
        campoValor = buscar(panel, JTextField.class).getFirst();
        botonConvertir = buscar(panel, JButton.class).stream()
                .filter(b -> "Convertir".equals(b.getText()))
                .findFirst().orElseThrow();
    }

    @Test
    void exposesTheFirstMagnitudeRightAfterShowingMagnitudes() throws Exception {
        enEdt(() -> panel.mostrarMagnitudes(List.of("Longitud", "Masa")));

        assertEquals("Longitud", panel.getMagnitud());
        assertEquals("Longitud", enEdt(comboCategoria::getSelectedItem));
    }

    @Test
    void keepsShowingTheOriginalMagnitudesWhenTheCallerMutatesTheList() throws Exception {
        List<String> magnitudes = new ArrayList<>(List.of("Longitud", "Masa"));

        // Hold the EDT so the model update is still queued while the list changes.
        CountDownLatch edtRetenido = new CountDownLatch(1);
        CountDownLatch soltarEdt = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            edtRetenido.countDown();
            esperar(soltarEdt);
        });
        esperar(edtRetenido);
        panel.mostrarMagnitudes(magnitudes);
        magnitudes.set(0, "Cambiada");
        magnitudes.add("Extra");
        soltarEdt.countDown();

        assertEquals("Longitud", panel.getMagnitud());
        assertEquals(List.of("Longitud", "Masa"), enEdt(() -> elementos(comboCategoria)));
    }

    @Test
    void showingMagnitudesNotifiesTheFirstOnceOffTheEdtAndUnitFillsDoNot() throws Exception {
        AtomicInteger disparos = new AtomicInteger();
        AtomicBoolean enHiloEdt = new AtomicBoolean();
        panel.alCambiarMagnitud(() -> {
            enHiloEdt.set(SwingUtilities.isEventDispatchThread());
            disparos.incrementAndGet();
        });

        enEdt(() -> {
            panel.mostrarMagnitudes(List.of("Longitud", "Masa"));
            panel.mostrarUnidades(List.of("m", "km", "cm"));
        });
        esperarHasta(this::todosHabilitados);

        assertEquals(1, disparos.get());
        assertFalse(enHiloEdt.get(), "the initial load must run off the EDT");
        assertEquals("Longitud", panel.getMagnitud());
    }

    @Test
    void preselectsTheFirstTwoUnitsAsOriginAndDestination() throws Exception {
        enEdt(() -> panel.mostrarUnidades(List.of("m", "km", "cm")));

        assertEquals("m", panel.getUnidadOrigen());
        assertEquals("km", panel.getUnidadDestino());
        assertEquals("m", enEdt(comboDesde::getSelectedItem));
        assertEquals("km", enEdt(comboHacia::getSelectedItem));
    }

    @Test
    void locksEveryInputWhileAConversionRunsAndUnlocksAfterwards() throws Exception {
        enEdt(() -> panel.mostrarMagnitudes(List.of("Longitud", "Masa")));
        // Let the initial magnitude notification finish before acting.
        esperarHasta(this::todosHabilitados);
        CountDownLatch iniciada = new CountDownLatch(1);
        CountDownLatch soltar = new CountDownLatch(1);
        panel.alConvertir(() -> {
            iniciada.countDown();
            esperar(soltar);
        });

        enEdt(() -> botonConvertir.doClick());
        esperar(iniciada);

        assertTodosDeshabilitados();
        soltar.countDown();
        esperarHasta(this::todosHabilitados);
    }

    @Test
    void locksEveryInputWhileUnitsOfANewMagnitudeLoad() throws Exception {
        enEdt(() -> panel.mostrarMagnitudes(List.of("Longitud", "Masa")));
        // Let the initial magnitude notification finish before acting.
        esperarHasta(this::todosHabilitados);
        CountDownLatch iniciada = new CountDownLatch(1);
        CountDownLatch soltar = new CountDownLatch(1);
        panel.alCambiarMagnitud(() -> {
            iniciada.countDown();
            esperar(soltar);
        });

        enEdt(() -> comboCategoria.setSelectedIndex(1));
        esperar(iniciada);

        assertEquals("Masa", panel.getMagnitud());
        assertTodosDeshabilitados();
        soltar.countDown();
        esperarHasta(this::todosHabilitados);
    }

    @Test
    void reportsAGenericErrorAndUnlocksWhenTheActionFails() throws Exception {
        panel.alConvertir(() -> {
            throw new IllegalStateException("boom");
        });

        enEdt(() -> botonConvertir.doClick());

        esperarHasta(() -> todosHabilitados() && !buscarTexto(panel, ERROR_INESPERADO).isEmpty());
    }

    private void assertTodosDeshabilitados() throws Exception {
        enEdt(() -> {
            assertFalse(comboCategoria.isEnabled(), "category combo");
            assertFalse(comboDesde.isEnabled(), "origin combo");
            assertFalse(comboHacia.isEnabled(), "destination combo");
            assertFalse(campoValor.isEnabled(), "value field");
            assertFalse(botonConvertir.isEnabled(), "convert button");
        });
    }

    private boolean todosHabilitados() {
        return comboCategoria.isEnabled() && comboDesde.isEnabled() && comboHacia.isEnabled()
                && campoValor.isEnabled() && botonConvertir.isEnabled();
    }

    private static List<Object> elementos(JComboBox<?> combo) {
        List<Object> elementos = new ArrayList<>();
        for (int i = 0; i < combo.getItemCount(); i++) {
            elementos.add(combo.getItemAt(i));
        }
        return elementos;
    }

    // ---- Swing test helpers ------------------------------------------------

    static <T extends Component> List<T> buscar(Container raiz, Class<? super T> tipo) {
        List<T> encontrados = new ArrayList<>();
        for (Component hijo : raiz.getComponents()) {
            if (tipo.isInstance(hijo)) {
                @SuppressWarnings("unchecked")
                T coincidencia = (T) hijo;
                encontrados.add(coincidencia);
            }
            if (hijo instanceof Container contenedor) {
                encontrados.addAll(buscar(contenedor, tipo));
            }
        }
        return encontrados;
    }

    static List<JLabel> buscarTexto(Container raiz, String texto) {
        List<JLabel> etiquetas = buscar(raiz, JLabel.class);
        return etiquetas.stream().filter(e -> texto.equals(e.getText())).toList();
    }

    static <T> T enEdt(Supplier<T> accion) throws Exception {
        AtomicReference<T> resultado = new AtomicReference<>();
        enEdt(() -> resultado.set(accion.get()));
        return resultado.get();
    }

    static void enEdt(Runnable accion) throws Exception {
        try {
            SwingUtilities.invokeAndWait(accion);
        } catch (InvocationTargetException e) {
            // Surface assertion failures raised on the EDT as-is.
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            throw e;
        }
    }

    static void esperar(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                fail("timed out waiting for latch");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail(e);
        }
    }

    /** Re-checks the condition on the EDT until it holds; SwingWorker.done() is posted asynchronously. */
    static void esperarHasta(BooleanSupplier condicion) throws Exception {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!enEdt(condicion::getAsBoolean)) {
            if (System.nanoTime() > limite) {
                fail("condition not met within 5 seconds");
            }
            Thread.sleep(5);
        }
    }
}
