package ec.edu.monster.vista;

import ec.edu.monster.modelo.Comprobante;
import ec.edu.monster.modelo.MovimientoForm;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Base panel for the deposit, withdrawal and transfer screens: title, the input
 * fields with their error line, the "Revisar" / "Limpiar" buttons and every UI
 * reaction the controller asks for. Each screen only declares its fields and
 * builds its form model.
 */
public abstract class OperacionPanel extends JPanel implements OperacionVista {

    private static final Logger LOG = Logger.getLogger(OperacionPanel.class.getName());

    private final Map<String, JTextField> campos = new LinkedHashMap<>();
    private final Map<String, JLabel> etiquetasError = new LinkedHashMap<>();
    private final Map<String, String> valoresFijos = new LinkedHashMap<>();
    private final JLabel mensajeGeneral = Estilos.mensajeError();
    private final JButton botonRevisar = Estilos.botonPrimario("Revisar");
    private final JButton botonLimpiar = Estilos.botonSecundario("Limpiar");
    private final JButton botonVolver = Estilos.botonSecundario("Volver");

    private Consumer<MovimientoForm> accionRevisar = form -> { };
    private Runnable alVolver = () -> { };

    /**
     * One input of an operation screen.
     *
     * @param nombre     form field name ({@code cuenta}, {@code importe}...)
     * @param etiqueta   label shown above the input
     * @param valorFijo  value of a read-only input (e.g. the account of a Cliente), or null
     * @param clave      true for a password input (the account key of an Empleado)
     */
    protected record Campo(String nombre, String etiqueta, String valorFijo, boolean clave) {

        static Campo editable(String nombre, String etiqueta) {
            return new Campo(nombre, etiqueta, null, false);
        }

        static Campo fijo(String nombre, String etiqueta, String valor) {
            return new Campo(nombre, etiqueta, valor, false);
        }

        static Campo clave(String nombre, String etiqueta) {
            return new Campo(nombre, etiqueta, null, true);
        }
    }

    /**
     * @param definicion the fields of the screen, in display order
     */
    protected OperacionPanel(String titulo, String descripcion, Campo... definicion) {
        super(new BorderLayout());
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));
        add(encabezado(titulo, descripcion), BorderLayout.NORTH);
        add(formulario(definicion), BorderLayout.CENTER);
        add(pie(), BorderLayout.SOUTH);

        botonRevisar.addActionListener(e -> revisar());
        botonLimpiar.addActionListener(e -> limpiar());
        botonVolver.addActionListener(e -> alVolver.run());
    }

    /** Reads the fields on the EDT and returns the form model to validate. */
    public abstract MovimientoForm leer();

    public final void setAlVolver(Runnable alVolver) {
        this.alVolver = alVolver;
    }

    @Override
    public final void alRevisar(Consumer<MovimientoForm> accion) {
        this.accionRevisar = accion;
    }

    @Override
    public final void limpiarErrores() {
        Estilos.enEdt(() -> {
            etiquetasError.values().forEach(etiqueta -> etiqueta.setText(" "));
            mensajeGeneral.setText(" ");
        });
    }

    @Override
    public final void mostrarErrores(Map<String, String> errores) {
        Estilos.enEdt(() -> {
            etiquetasError.values().forEach(etiqueta -> etiqueta.setText(" "));
            mensajeGeneral.setText(" ");
            errores.forEach((campo, mensaje) -> {
                JLabel etiqueta = etiquetasError.get(campo);
                if (etiqueta != null) {
                    etiqueta.setText(mensaje);
                } else {
                    mensajeGeneral.setText(mensaje);
                }
            });
        });
    }

    @Override
    public final void mostrarError(String mensaje) {
        Estilos.enEdt(() -> {
            etiquetasError.values().forEach(etiqueta -> etiqueta.setText(" "));
            mensajeGeneral.setText(mensaje == null || mensaje.isBlank() ? Estilos.ERROR_INESPERADO : mensaje);
        });
    }

    @Override
    public final boolean confirmar(String mensaje) {
        final boolean[] aceptado = {false};
        try {
            SwingUtilities.invokeAndWait(() -> aceptado[0] = JOptionPane.showConfirmDialog(this, mensaje,
                    "Confirmar operación", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            LOG.log(Level.SEVERE, "Confirmation dialog failed", e.getCause());
        }
        return aceptado[0];
    }

    @Override
    public final void mostrarComprobante(Comprobante comprobante) {
        Estilos.enEdt(() -> new ComprobanteDialog(SwingUtilities.getWindowAncestor(this), comprobante)
                .setVisible(true));
    }

    @Override
    public final void limpiar() {
        Estilos.enEdt(() -> {
            campos.forEach((nombre, campo) -> campo.setText(valoresFijos.getOrDefault(nombre, "")));
            etiquetasError.values().forEach(etiqueta -> etiqueta.setText(" "));
            mensajeGeneral.setText(" ");
            if (!campos.isEmpty()) {
                campos.values().iterator().next().requestFocusInWindow();
            }
        });
    }

    /** Current text of one field, trimmed by {@code MovimientoForm}. */
    protected final String valor(String nombre) {
        JTextField campo = campos.get(nombre);
        return campo == null ? "" : campo.getText();
    }

    private void revisar() {
        // Read the Swing fields on the EDT, then call the server off the EDT
        MovimientoForm form = leer();
        limpiarErrores();
        Estilos.enSegundoPlano(() -> accionRevisar.accept(form), this::mostrarError,
                botonRevisar, botonLimpiar);
    }

    private JPanel encabezado(String titulo, String descripcion) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        panel.add(Estilos.titulo(titulo), BorderLayout.NORTH);
        panel.add(Estilos.texto(descripcion), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel formulario(Campo[] definicion) {
        JPanel tarjeta = Estilos.panelTarjeta();
        tarjeta.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int fila = 0;
        for (Campo campo : definicion) {
            c.gridy = fila++;
            c.insets = new Insets(0, 0, 4, 0);
            tarjeta.add(Estilos.etiqueta(campo.etiqueta()), c);

            JTextField entrada = campo.clave()
                    ? Estilos.entrada(new JPasswordField())
                    : Estilos.entrada(new JTextField());
            if (campo.valorFijo() != null) {
                // Own account of a Cliente: shown, never edited
                entrada.setText(campo.valorFijo());
                entrada.setEditable(false);
                valoresFijos.put(campo.nombre(), campo.valorFijo());
            }
            c.gridy = fila++;
            c.insets = new Insets(0, 0, 2, 0);
            tarjeta.add(entrada, c);

            JLabel error = Estilos.mensajeError();
            c.gridy = fila++;
            c.insets = new Insets(0, 0, 12, 0);
            tarjeta.add(error, c);

            campos.put(campo.nombre(), entrada);
            etiquetasError.put(campo.nombre(), error);
        }

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.add(tarjeta, BorderLayout.NORTH);
        return contenedor;
    }

    private JPanel pie() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        panel.add(mensajeGeneral, BorderLayout.NORTH);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(botonVolver);
        botones.add(botonLimpiar);
        botones.add(botonRevisar);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }
}
