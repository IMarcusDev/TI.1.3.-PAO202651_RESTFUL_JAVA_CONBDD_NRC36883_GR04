package ec.edu.monster.vista;

import ec.edu.monster.modelo.Magnitudes;
import ec.edu.monster.modelo.NombresUnidades;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * "Consultar catálogo" screen: magnitudes on the left, units of the selected
 * one on the right. The list keeps server names; the table shows each symbol
 * with its Spanish name.
 */
public class CatalogoPanel extends JPanel implements CatalogoVista {

    private final JList<String> listaMagnitudes = new JList<>();
    private final DefaultTableModel modeloUnidades = new DefaultTableModel(new Object[] {"Nombre", "Símbolo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tablaUnidades = new JTable(modeloUnidades);
    private final JLabel etiquetaError = Estilos.mensajeError();

    private Runnable accionCambiarMagnitud = () -> { };
    // True while the list model is filled programmatically, so no user action is fired.
    private boolean actualizando;
    // Snapshot taken on the EDT so the controller can read it from a worker thread.
    private volatile String magnitudActual;

    public CatalogoPanel() {
        super(new BorderLayout(0, 16));
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(28, 34, 0, 34));

        add(Estilos.titulo("Consultar catálogo"), BorderLayout.NORTH);
        add(construirCuerpo(), BorderLayout.CENTER);
        add(etiquetaError, BorderLayout.SOUTH);

        listaMagnitudes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cambiarMagnitud();
            }
        });
    }

    private JPanel construirCuerpo() {
        listaMagnitudes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaMagnitudes.setFixedCellHeight(44);
        listaMagnitudes.setCellRenderer(new MagnitudRenderer());
        listaMagnitudes.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JScrollPane scrollMagnitudes = new JScrollPane(listaMagnitudes);
        scrollMagnitudes.setBorder(BorderFactory.createLineBorder(Estilos.BORDE));
        scrollMagnitudes.setPreferredSize(new Dimension(208, 0));

        tablaUnidades.setFont(Estilos.FUENTE);
        tablaUnidades.setForeground(Estilos.TEXTO);
        tablaUnidades.setRowHeight(38);
        tablaUnidades.setShowVerticalLines(false);
        tablaUnidades.setGridColor(Estilos.BORDE);
        tablaUnidades.setFocusable(false);
        tablaUnidades.setRowSelectionAllowed(false);
        tablaUnidades.setFillsViewportHeight(true);
        tablaUnidades.setDefaultRenderer(Object.class, celda(Estilos.FUENTE, Estilos.TEXTO, false));
        tablaUnidades.getTableHeader().setDefaultRenderer(
                celda(Estilos.FUENTE_ETIQUETA, Estilos.PRIMARIO, true));
        tablaUnidades.getTableHeader().setPreferredSize(new Dimension(0, 38));
        tablaUnidades.getTableHeader().setReorderingAllowed(false);
        tablaUnidades.getTableHeader().setResizingAllowed(false);
        JScrollPane scrollUnidades = new JScrollPane(tablaUnidades);
        scrollUnidades.setBorder(BorderFactory.createLineBorder(Estilos.BORDE));
        scrollUnidades.getViewport().setBackground(Estilos.BLANCO);

        JPanel cuerpo = new JPanel(new BorderLayout(20, 0));
        cuerpo.setOpaque(false);
        cuerpo.add(scrollMagnitudes, BorderLayout.WEST);
        cuerpo.add(scrollUnidades, BorderLayout.CENTER);
        return cuerpo;
    }

    private void cambiarMagnitud() {
        if (actualizando) {
            return;
        }
        magnitudActual = listaMagnitudes.getSelectedValue();
        Estilos.enSegundoPlano(accionCambiarMagnitud, this::mostrarError, listaMagnitudes);
    }

    @Override
    public void mostrarMagnitudes(List<String> magnitudes) {
        List<String> copia = List.copyOf(magnitudes);
        // Snapshot set synchronously: the controller reads it right after this call.
        magnitudActual = copia.isEmpty() ? null : copia.get(0);
        Estilos.enEdt(() -> {
            actualizando = true;
            listaMagnitudes.setListData(copia.toArray(String[]::new));
            if (!copia.isEmpty()) {
                listaMagnitudes.setSelectedIndex(0);
            }
            actualizando = false;
            if (accionCambiarMagnitud != null && !copia.isEmpty()) {
                Estilos.enSegundoPlano(accionCambiarMagnitud, this::mostrarError, listaMagnitudes);
            }
        });
    }

    @Override
    public String getMagnitud() {
        return magnitudActual;
    }

    @Override
    public void mostrarUnidades(List<String> unidades) {
        List<String> copia = List.copyOf(unidades);
        Estilos.enEdt(() -> {
            modeloUnidades.setRowCount(0);
            for (String unidad : copia) {
                modeloUnidades.addRow(new Object[] {NombresUnidades.nombre(unidad), unidad});
            }
        });
    }

    @Override
    public void mostrarError(String mensaje) {
        Estilos.enEdt(() -> etiquetaError.setText(mensaje));
    }

    @Override
    public void limpiarMensajes() {
        Estilos.enEdt(() -> etiquetaError.setText(" "));
    }

    @Override
    public void alCambiarMagnitud(Runnable accion) {
        this.accionCambiarMagnitud = accion;
    }

    private static DefaultTableCellRenderer celda(Font fuente, Color color, boolean encabezado) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
                    boolean foco, int fila, int columna) {
                super.getTableCellRendererComponent(tabla, valor, false, false, fila, columna);
                // The base renderer resets the font to the table's on every call.
                setFont(fuente);
                setBorder(encabezado
                        ? BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilos.BORDE),
                                BorderFactory.createEmptyBorder(0, 12, 0, 12))
                        : BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return this;
            }
        };
        renderer.setForeground(color);
        renderer.setBackground(encabezado ? Estilos.FONDO_RESULTADO : Estilos.BLANCO);
        return renderer;
    }

    /** Shows the display name; the selected item gets a rounded primary highlight. */
    private static final class MagnitudRenderer extends DefaultListCellRenderer {

        private boolean seleccionado;

        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                boolean seleccionado, boolean foco) {
            super.getListCellRendererComponent(lista, Magnitudes.nombreVisible((String) valor),
                    indice, false, false);
            this.seleccionado = seleccionado;
            setOpaque(false);
            setFont(seleccionado ? Estilos.FUENTE_NEGRITA : Estilos.FUENTE);
            setForeground(seleccionado ? Estilos.BLANCO : Estilos.TEXTO);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (seleccionado) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Estilos.PRIMARIO);
                g2.fillRoundRect(0, 2, getWidth(), getHeight() - 4, 6, 6);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
