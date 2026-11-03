package ec.edu.monster.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import ec.edu.monster.modelo.Magnitudes;
import ec.edu.monster.modelo.NombresUnidades;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * "Convertir unidades" screen: magnitude and unit selectors, value input and
 * result area. Combo models hold server values; renderers show display names.
 */
public class ConversionPanel extends JPanel implements ConversionVista {

    private final JComboBox<String> comboCategoria = Estilos.combo(Magnitudes::nombreVisible);
    private final JComboBox<String> comboDesde = Estilos.combo(ConversionPanel::unidadVisible);
    private final JComboBox<String> comboHacia = Estilos.combo(ConversionPanel::unidadVisible);
    private final JTextField campoValor = Estilos.entrada(new JTextField());
    private final JButton botonConvertir = Estilos.botonPrimario("Convertir");
    private final JLabel etiquetaResultado = new JLabel(" ");
    private final JLabel etiquetaError = Estilos.mensajeError();

    private Runnable accionConvertir = () -> { };
    private Runnable accionCambiarMagnitud = () -> { };
    // True while models are filled programmatically, so no user action is fired.
    private boolean actualizando;
    // Snapshots taken on the EDT so the controller can read them from a worker thread.
    private volatile String magnitudActual;
    private volatile String valorActual;
    private volatile String origenActual;
    private volatile String destinoActual;

    public ConversionPanel() {
        super(new BorderLayout());
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(28, 34, 12, 34));
        add(construirContenido(), BorderLayout.NORTH);

        comboCategoria.addActionListener(e -> cambiarMagnitud());
        comboDesde.addActionListener(e -> origenActual = seleccion(comboDesde));
        comboHacia.addActionListener(e -> destinoActual = seleccion(comboHacia));
        botonConvertir.addActionListener(e -> convertir());
        campoValor.addActionListener(e -> convertir());
    }

    private JPanel construirContenido() {
        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        JLabel titulo = Estilos.titulo("Convertir unidades");
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JPanel selectores = new JPanel(new GridLayout(1, 3, 12, 0));
        selectores.setOpaque(false);
        selectores.add(campo("Categoría", comboCategoria));
        selectores.add(campo("Desde", comboDesde));
        selectores.add(campo("Hacia", comboHacia));

        JPanel filaValor = new JPanel(new GridBagLayout());
        filaValor.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.SOUTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        filaValor.add(campo("Valor", campoValor), c);
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 12, 0, 0);
        botonConvertir.setPreferredSize(new Dimension(134, 36));
        filaValor.add(botonConvertir, c);

        contenido.add(alinear(titulo));
        contenido.add(alinear(selectores));
        contenido.add(alinear(espacio(18)));
        contenido.add(alinear(filaValor));
        contenido.add(alinear(espacio(20)));
        contenido.add(alinear(construirResultado()));
        contenido.add(alinear(espacio(8)));
        contenido.add(alinear(etiquetaError));
        return contenido;
    }

    private JPanel construirResultado() {
        JPanel resultado = new JPanel(new BorderLayout(0, 8));
        resultado.setBackground(Estilos.FONDO_RESULTADO);
        resultado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, Estilos.PRIMARIO),
                BorderFactory.createEmptyBorder(20, 26, 22, 26)));
        JLabel encabezado = new JLabel("Resultado");
        encabezado.setFont(Estilos.FUENTE_PEQUENA);
        encabezado.setForeground(Estilos.PRIMARIO);
        resultado.add(encabezado, BorderLayout.NORTH);
        etiquetaResultado.setFont(Estilos.FUENTE_RESULTADO);
        etiquetaResultado.setForeground(Estilos.TEXTO);
        resultado.add(etiquetaResultado, BorderLayout.CENTER);
        return resultado;
    }

    private static JPanel campo(String texto, JComponent componente) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.add(Estilos.etiqueta(texto), BorderLayout.NORTH);
        panel.add(componente, BorderLayout.CENTER);
        return panel;
    }

    private static JComponent espacio(int alto) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, alto));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, alto));
        return panel;
    }

    private static JComponent alinear(JComponent componente) {
        componente.setAlignmentX(LEFT_ALIGNMENT);
        return componente;
    }

    private void cambiarMagnitud() {
        if (actualizando) {
            return;
        }
        magnitudActual = seleccion(comboCategoria);
        enSegundoPlano(accionCambiarMagnitud);
    }

    private void convertir() {
        if (!botonConvertir.isEnabled()) {
            return;
        }
        magnitudActual = seleccion(comboCategoria);
        valorActual = campoValor.getText();
        origenActual = seleccion(comboDesde);
        destinoActual = seleccion(comboHacia);
        enSegundoPlano(accionConvertir);
    }

    // Every input stays locked while a worker runs, so no second worker can
    // start and overwrite the snapshots the running one reads.
    private void enSegundoPlano(Runnable accion) {
        Estilos.enSegundoPlano(accion, this::mostrarError,
                comboCategoria, comboDesde, comboHacia, campoValor, botonConvertir);
    }

    private static String unidadVisible(String simbolo) {
        return Estilos.unidadVisible(simbolo, NombresUnidades.nombre(simbolo));
    }

    private static String seleccion(JComboBox<String> combo) {
        return (String) combo.getSelectedItem();
    }

    @Override
    public void mostrarMagnitudes(List<String> magnitudes) {
        List<String> copia = List.copyOf(magnitudes);
        // Snapshot set synchronously: the controller reads it right after this call.
        magnitudActual = copia.isEmpty() ? null : copia.get(0);
        Estilos.enEdt(() -> {
            actualizando = true;
            comboCategoria.setModel(new DefaultComboBoxModel<>(copia.toArray(String[]::new)));
            actualizando = false;
            if (accionCambiarMagnitud != null && !copia.isEmpty()) {
                enSegundoPlano(accionCambiarMagnitud);
            }
        });
    }

    @Override
    public void mostrarUnidades(List<String> unidades) {
        String[] items = unidades.toArray(String[]::new);
        origenActual = items.length > 0 ? items[0] : null;
        destinoActual = items.length > 1 ? items[1] : origenActual;
        Estilos.enEdt(() -> {
            actualizando = true;
            comboDesde.setModel(new DefaultComboBoxModel<>(items));
            DefaultComboBoxModel<String> modeloHacia = new DefaultComboBoxModel<>(items);
            if (items.length > 1) {
                modeloHacia.setSelectedItem(items[1]);
            }
            comboHacia.setModel(modeloHacia);
            actualizando = false;
        });
    }

    @Override
    public String getMagnitud() {
        return magnitudActual;
    }

    @Override
    public String getValor() {
        return valorActual;
    }

    @Override
    public String getUnidadOrigen() {
        return origenActual;
    }

    @Override
    public String getUnidadDestino() {
        return destinoActual;
    }

    @Override
    public void mostrarResultado(String texto) {
        Estilos.enEdt(() -> etiquetaResultado.setText(texto));
    }

    @Override
    public void mostrarError(String mensaje) {
        Estilos.enEdt(() -> etiquetaError.setText(mensaje));
    }

    @Override
    public void limpiarMensajes() {
        Estilos.enEdt(() -> {
            etiquetaResultado.setText(" ");
            etiquetaError.setText(" ");
        });
    }

    @Override
    public void alConvertir(Runnable accion) {
        this.accionConvertir = accion;
    }

    @Override
    public void alCambiarMagnitud(Runnable accion) {
        this.accionCambiarMagnitud = accion;
    }
}
