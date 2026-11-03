package ec.edu.monster.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * Login window. Navigation to the main window is delegated to the
 * {@code alAbrirMenu} callback supplied by the composition root.
 */
public class LoginFrame extends JFrame implements LoginVista {

    private final Runnable alAbrirMenu;
    private final JTextField campoUsuario = Estilos.entrada(new JTextField());
    private final JPasswordField campoClave = Estilos.entrada(new JPasswordField());
    private final JButton botonIngresar = Estilos.botonPrimario("Ingresar");
    private final IndicadorEstado indicadorEstado = new IndicadorEstado("");

    private Runnable accionIngresar = () -> { };
    // Snapshots taken on the EDT so the controller can read them from a worker thread.
    private volatile String usuarioActual;
    private volatile String claveActual;

    public LoginFrame(Runnable alAbrirMenu) {
        super(Estilos.TITULO_VENTANA);
        this.alAbrirMenu = alAbrirMenu;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(640, 480);
        setLocationRelativeTo(null);
        setContentPane(construirContenido());
        JMenu archivo = Estilos.menu("Archivo");
        archivo.add(Estilos.opcion("Salir",
                () -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING))));
        setJMenuBar(Estilos.barraMenu(archivo, Estilos.menuAyuda(this)));
        getRootPane().setDefaultButton(botonIngresar);
        botonIngresar.addActionListener(e -> ingresar());
    }

    private JPanel construirContenido() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        c.insets = new Insets(0, 0, 22, 0);
        formulario.add(Estilos.titulo("Iniciar sesión"), c);
        c.insets = new Insets(0, 0, 8, 0);
        formulario.add(Estilos.etiqueta("Usuario"), c);
        c.insets = new Insets(0, 0, 22, 0);
        formulario.add(campoUsuario, c);
        c.insets = new Insets(0, 0, 8, 0);
        formulario.add(Estilos.etiqueta("Contraseña"), c);
        c.insets = new Insets(0, 0, 22, 0);
        formulario.add(campoClave, c);

        JPanel filaBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        filaBoton.setOpaque(false);
        botonIngresar.setPreferredSize(new Dimension(126, 40));
        filaBoton.add(botonIngresar);
        c.insets = new Insets(0, 0, 0, 0);
        formulario.add(filaBoton, c);

        indicadorEstado.setBorder(BorderFactory.createEmptyBorder(0, 50, 18, 50));

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilos.FONDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(36, 50, 0, 50));
        raiz.add(formulario, BorderLayout.NORTH);
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(Estilos.FONDO);
        contenedor.add(raiz, BorderLayout.CENTER);
        contenedor.add(indicadorEstado, BorderLayout.SOUTH);
        return contenedor;
    }

    private void ingresar() {
        usuarioActual = campoUsuario.getText();
        claveActual = new String(campoClave.getPassword());
        Estilos.enSegundoPlano(accionIngresar, this::mostrarError, botonIngresar);
    }

    @Override
    public String getUsuario() {
        return usuarioActual;
    }

    @Override
    public String getClave() {
        return claveActual;
    }

    @Override
    public void mostrarError(String mensaje) {
        Estilos.enEdt(() -> JOptionPane.showOptionDialog(this, mensaje, Estilos.TITULO_VENTANA,
                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                new Object[] {"Aceptar"}, "Aceptar"));
    }

    @Override
    public void abrirMenu() {
        Estilos.enEdt(() -> {
            claveActual = null;
            alAbrirMenu.run();
            dispose();
        });
    }

    @Override
    public void alIngresar(Runnable accion) {
        this.accionIngresar = accion;
    }

    public EstadoVista getIndicadorEstado() {
        return indicadorEstado;
    }
}
