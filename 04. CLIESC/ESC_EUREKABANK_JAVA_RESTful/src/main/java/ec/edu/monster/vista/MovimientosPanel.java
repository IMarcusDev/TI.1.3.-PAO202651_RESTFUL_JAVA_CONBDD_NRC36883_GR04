package ec.edu.monster.vista;

import ec.edu.monster.modelo.HistorialCuenta;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * "Consultar movimientos" screen. An Empleado types any account; a Cliente only
 * sees their own (the server ties the token to it). The answer shows the account
 * data above a read-only table, or a message when the account has no movements.
 */
public class MovimientosPanel extends JPanel implements MovimientosVista {

    private static final String[] COLUMNAS = {"N.º", "Fecha", "Tipo", "Acción", "Importe", "Cuenta referencia"};

    private final boolean esCliente;
    private final String cuentaCliente;
    private final Runnable alVolver;

    private final JTextField campoCuenta = Estilos.entrada(new JTextField());
    private final JLabel errorCuenta = Estilos.mensajeError();
    private final JLabel mensajeError = Estilos.mensajeError();
    private final JLabel sinMovimientos = Estilos.texto("La cuenta no tiene movimientos");
    private final JButton botonConsultar = Estilos.botonPrimario("Consultar");
    private final JButton botonVolver = Estilos.botonSecundario("Volver");

    private final JLabel numero = Estilos.texto("");
    private final JLabel titular = Estilos.texto("");
    private final JLabel moneda = Estilos.texto("");
    private final JLabel saldo = Estilos.texto("");
    private final JLabel estado = Estilos.texto("");
    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JScrollPane desplazamiento = new JScrollPane(tabla);
    private final JPanel resultado = Estilos.panelTarjeta();

    private Consumer<String> accionConsultar = cuenta -> { };

    public MovimientosPanel(boolean esCliente, String cuentaCliente, Runnable alVolver) {
        super(new BorderLayout(0, 16));
        this.esCliente = esCliente;
        this.cuentaCliente = cuentaCliente == null ? "" : cuentaCliente;
        this.alVolver = alVolver;

        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        JPanel superior = new JPanel(new BorderLayout(0, 14));
        superior.setOpaque(false);
        superior.add(encabezado(), BorderLayout.NORTH);
        if (!esCliente) {
            superior.add(busqueda(), BorderLayout.CENTER);
        }
        add(superior, BorderLayout.NORTH);

        add(construirResultado(), BorderLayout.CENTER);
        add(pie(), BorderLayout.SOUTH);

        botonConsultar.addActionListener(e -> consultar());
        botonVolver.addActionListener(e -> alVolver.run());
        if (esCliente) {
            // A Cliente has no search form: their history is loaded when the screen opens
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    consultar();
                }
            });
        }
    }

    @Override
    public String getCuenta() {
        return esCliente ? cuentaCliente : campoCuenta.getText();
    }

    @Override
    public void alConsultar(Consumer<String> accion) {
        this.accionConsultar = accion;
    }

    @Override
    public void limpiarErrores() {
        Estilos.enEdt(() -> {
            errorCuenta.setText(" ");
            mensajeError.setText(" ");
        });
    }

    @Override
    public void mostrarError(String mensaje) {
        Estilos.enEdt(() -> {
            mensajeError.setText(mensaje == null || mensaje.isBlank() ? Estilos.ERROR_INESPERADO : mensaje);
            resultado.setVisible(false);
        });
    }

    @Override
    public void mostrarHistorial(HistorialCuenta historial) {
        Estilos.enEdt(() -> {
            HistorialCuenta.CuentaConsultada cuenta = historial.cuenta();
            numero.setText(cuenta.numero());
            titular.setText(cuenta.titular());
            moneda.setText(cuenta.moneda());
            saldo.setText(cuenta.saldoFormateado());
            estado.setText(cuenta.estado());

            modelo.setRowCount(0);
            historial.movimientos().forEach(m -> modelo.addRow(new Object[] {
                m.numero(), m.fecha(), m.tipo(), m.accion(), m.importeFormateado(), m.referencia()
            }));

            boolean vacio = historial.sinMovimientos();
            sinMovimientos.setVisible(vacio);
            desplazamiento.setVisible(!vacio);
            resultado.setVisible(true);
            revalidate();
            repaint();
        });
    }

    // Reads the field on the EDT, then calls the server off the EDT
    private void consultar() {
        String cuenta = getCuenta();
        limpiarErrores();
        Estilos.enSegundoPlano(() -> accionConsultar.accept(cuenta), this::mostrarError, botonConsultar);
    }

    private JPanel encabezado() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.add(Estilos.titulo("Consultar movimientos"), BorderLayout.NORTH);
        panel.add(Estilos.texto("Ver saldo e historial de una cuenta"), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel busqueda() {
        JPanel tarjeta = Estilos.panelTarjeta();
        tarjeta.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        c.gridy = 0;
        c.insets = new Insets(0, 0, 4, 0);
        tarjeta.add(Estilos.etiqueta("Número de cuenta"), c);

        c.gridy = 1;
        c.insets = new Insets(0, 0, 2, 0);
        tarjeta.add(campoCuenta, c);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 0);
        tarjeta.add(errorCuenta, c);
        return tarjeta;
    }

    private JPanel construirResultado() {
        resultado.setLayout(new BorderLayout(0, 12));
        resultado.add(datosCuenta(), BorderLayout.NORTH);

        JPanel movimientos = new JPanel(new BorderLayout(0, 8));
        movimientos.setOpaque(false);
        movimientos.add(Estilos.subtitulo("Movimientos"), BorderLayout.NORTH);
        movimientos.add(sinMovimientos, BorderLayout.CENTER);
        movimientos.add(desplazamiento, BorderLayout.SOUTH);
        resultado.add(movimientos, BorderLayout.CENTER);

        prepararTabla();
        sinMovimientos.setVisible(false);
        resultado.setVisible(false);
        return resultado;
    }

    private JPanel datosCuenta() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 0, 2, 24);

        int fila = 0;
        fila = filaDato(panel, c, fila, "Número", numero);
        fila = filaDato(panel, c, fila, "Titular", titular);
        fila = filaDato(panel, c, fila, "Moneda", moneda);
        fila = filaDato(panel, c, fila, "Saldo", saldo);
        filaDato(panel, c, fila, "Estado", estado);
        return panel;
    }

    private int filaDato(JPanel panel, GridBagConstraints c, int fila, String etiqueta, JLabel valor) {
        c.gridy = fila;
        c.gridx = 0;
        c.weightx = 0;
        panel.add(Estilos.etiqueta(etiqueta), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(valor, c);
        return fila + 1;
    }

    private void prepararTabla() {
        tabla.setFont(Estilos.FUENTE);
        tabla.setRowHeight(24);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setFont(Estilos.FUENTE_NEGRITA);
        // INGRESO is shown as a positive movement
        tabla.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object valor, boolean seleccion,
                    boolean foco, int fila, int columna) {
                super.getTableCellRendererComponent(table, valor, seleccion, foco, fila, columna);
                setForeground(historialIngreso(valor) ? Estilos.EXITO : Estilos.TEXTO);
                return this;
            }
        });
        desplazamiento.setPreferredSize(new Dimension(0, 240));
        desplazamiento.setBorder(BorderFactory.createLineBorder(Estilos.BORDE));
    }

    private static boolean historialIngreso(Object valor) {
        return "INGRESO".equals(valor);
    }

    private JPanel pie() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.add(mensajeError, BorderLayout.NORTH);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(botonVolver);
        botones.add(botonConsultar);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }
}
