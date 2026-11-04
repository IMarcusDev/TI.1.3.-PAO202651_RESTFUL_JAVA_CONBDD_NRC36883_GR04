package ec.edu.monster.vista;

import ec.edu.monster.modelo.Sesion;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Main window shown after login: header with the user, sidebar navigation and the
 * home / deposit / withdrawal / transfer screens. Controllers are wired by the
 * composition root through the panel getters.
 */
public class PrincipalFrame extends JFrame {

    private static final String INICIO = "inicio";
    private static final String DEPOSITO = "deposito";
    private static final String RETIRO = "retiro";
    private static final String TRANSFERENCIA = "transferencia";

    private final CardLayout pantallas = new CardLayout();
    private final JPanel contenido = new JPanel(pantallas);
    private final DepositoPanel depositoPanel = new DepositoPanel();
    private final RetiroPanel retiroPanel = new RetiroPanel();
    private final TransferenciaPanel transferenciaPanel = new TransferenciaPanel();
    private final IndicadorEstado indicadorEstado = new IndicadorEstado("Sesión activa · ");
    private final MenuPanel menuPanel;

    public PrincipalFrame(String nombre, String tipoUsuario, Runnable alCerrarSesion) {
        super(Estilos.TITULO_VENTANA);
        boolean empleado = Sesion.ROL_EMPLEADO.equals(tipoUsuario);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(940, 640);
        setLocationRelativeTo(null);

        menuPanel = new MenuPanel(nombre, empleado,
                () -> seleccionar(DEPOSITO), () -> seleccionar(RETIRO), () -> seleccionar(TRANSFERENCIA));

        contenido.setOpaque(false);
        contenido.add(menuPanel, INICIO);
        contenido.add(depositoPanel, DEPOSITO);
        contenido.add(retiroPanel, RETIRO);
        contenido.add(transferenciaPanel, TRANSFERENCIA);

        depositoPanel.setAlVolver(() -> seleccionar(INICIO));
        retiroPanel.setAlVolver(() -> seleccionar(INICIO));
        transferenciaPanel.setAlVolver(() -> seleccionar(INICIO));

        setJMenuBar(construirMenu(empleado, alCerrarSesion));

        indicadorEstado.setBorder(BorderFactory.createEmptyBorder(8, 30, 14, 30));

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilos.FONDO);
        raiz.add(construirEncabezado(nombre, tipoUsuario, alCerrarSesion), BorderLayout.NORTH);
        raiz.add(construirNavegacion(empleado), BorderLayout.WEST);
        raiz.add(contenido, BorderLayout.CENTER);
        raiz.add(indicadorEstado, BorderLayout.SOUTH);
        setContentPane(raiz);
    }

    public DepositoPanel getDepositoPanel() {
        return depositoPanel;
    }

    public RetiroPanel getRetiroPanel() {
        return retiroPanel;
    }

    public TransferenciaPanel getTransferenciaPanel() {
        return transferenciaPanel;
    }

    public EstadoVista getIndicadorEstado() {
        return indicadorEstado;
    }

    private void seleccionar(String pantalla) {
        pantallas.show(contenido, pantalla);
    }

    private JMenuBar construirMenu(boolean empleado, Runnable alCerrarSesion) {
        JMenu archivo = Estilos.menu("Archivo");
        archivo.add(Estilos.opcion("Inicio", () -> seleccionar(INICIO)));
        if (empleado) {
            archivo.add(Estilos.opcion("Depósito", () -> seleccionar(DEPOSITO)));
            archivo.add(Estilos.opcion("Retiro", () -> seleccionar(RETIRO)));
            archivo.add(Estilos.opcion("Transferencia", () -> seleccionar(TRANSFERENCIA)));
        }
        archivo.addSeparator();
        archivo.add(Estilos.opcion("Cerrar sesión", () -> {
            try {
                alCerrarSesion.run();
            } finally {
                dispose();
            }
        }));
        archivo.add(Estilos.opcion("Salir",
                () -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING))));
        return Estilos.barraMenu(archivo, Estilos.menuAyuda(this));
    }

    private JPanel construirEncabezado(String nombre, String tipoUsuario, Runnable alCerrarSesion) {
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izquierda.setOpaque(false);

        JLabel marca = new JLabel("EB");
        marca.setOpaque(true);
        marca.setBackground(Estilos.PRIMARIO);
        marca.setForeground(Estilos.BLANCO);
        marca.setFont(Estilos.FUENTE_NEGRITA);
        marca.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        izquierda.add(marca);
        izquierda.add(Estilos.subtitulo("EurekaBank"));

        JLabel servicio = new JLabel("RESTful / Java");
        servicio.setFont(Estilos.FUENTE_PEQUENA);
        servicio.setForeground(Estilos.PRIMARIO);
        servicio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.PRIMARIO),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        izquierda.add(servicio);

        JPanel usuario = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        usuario.setOpaque(false);
        JLabel nombreUsuario = new JLabel(nombre);
        nombreUsuario.setFont(Estilos.FUENTE_NEGRITA);
        nombreUsuario.setForeground(Estilos.TEXTO);
        usuario.add(nombreUsuario);
        JLabel rol = new JLabel(tipoUsuario == null ? "" : tipoUsuario);
        rol.setFont(Estilos.FUENTE_PEQUENA);
        rol.setForeground(Estilos.APAGADO);
        usuario.add(rol);

        JButton cerrar = Estilos.botonSecundario("Cerrar sesión");
        cerrar.addActionListener(e -> {
            try {
                alCerrarSesion.run();
            } finally {
                dispose();
            }
        });
        usuario.add(cerrar);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Estilos.BLANCO);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilos.BORDE),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));
        encabezado.add(izquierda, BorderLayout.WEST);
        encabezado.add(usuario, BorderLayout.EAST);
        return encabezado;
    }

    private JPanel construirNavegacion(boolean empleado) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Estilos.BLANCO);
        panel.setPreferredSize(new Dimension(210, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Estilos.BORDE),
                BorderFactory.createEmptyBorder(14, 10, 14, 10)));

        JLabel titulo = new JLabel("MENÚ");
        titulo.setFont(Estilos.FUENTE_PEQUENA);
        titulo.setForeground(Estilos.APAGADO);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 14, 8, 0));
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titulo);

        panel.add(navegacion("Inicio", () -> seleccionar(INICIO)));
        panel.add(proximamente("Consultar movimientos"));
        if (empleado) {
            panel.add(navegacion("Depósito", () -> seleccionar(DEPOSITO)));
            panel.add(navegacion("Retiro", () -> seleccionar(RETIRO)));
            panel.add(navegacion("Transferencia", () -> seleccionar(TRANSFERENCIA)));
        } else {
            panel.add(proximamente("Depósito"));
            panel.add(proximamente("Retiro"));
            panel.add(proximamente("Transferencia"));
        }
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JButton navegacion(String texto, Runnable accion) {
        JButton boton = Estilos.botonNavegacion(texto);
        boton.setAlignmentX(LEFT_ALIGNMENT);
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    private JLabel proximamente(String texto) {
        JLabel etiqueta = new JLabel(texto + " · Próximamente");
        etiqueta.setFont(Estilos.FUENTE);
        etiqueta.setForeground(Estilos.APAGADO);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        etiqueta.setAlignmentX(LEFT_ALIGNMENT);
        etiqueta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return etiqueta;
    }
}
