package ec.edu.monster.vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.Border;

/**
 * Shared palette, fonts and small widget factories for the Swing views.
 */
final class Estilos {

    static final String TITULO_VENTANA = "EurekaBank";
    static final String ERROR_INESPERADO = "Ocurrió un error inesperado";

    static final Color PRIMARIO = new Color(0x1F4E79);
    static final Color FONDO = new Color(0xF4F6F8);
    static final Color TEXTO = new Color(0x1F2937);
    static final Color BORDE = new Color(0xD1D5DB);
    static final Color BORDE_ENTRADA = new Color(0x6B7280);
    static final Color FONDO_RESULTADO = new Color(0xE8F0F8);
    static final Color EXITO = new Color(0x15803D);
    static final Color ERROR = new Color(0xB91C1C);
    static final Color APAGADO = new Color(0x9CA3AF);
    static final Color BLANCO = Color.WHITE;

    static final Font FUENTE = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
    static final Font FUENTE_NEGRITA = FUENTE.deriveFont(Font.BOLD);
    static final Font FUENTE_ETIQUETA = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    static final Font FUENTE_PEQUENA = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    static final Font FUENTE_TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 26);
    static final Font FUENTE_SUBTITULO = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    static final Font FUENTE_RESULTADO = new Font(Font.SANS_SERIF, Font.BOLD, 24);

    private static final Logger LOG = Logger.getLogger(Estilos.class.getName());

    private Estilos() {
    }

    static JLabel titulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_TITULO);
        etiqueta.setForeground(PRIMARIO);
        return etiqueta;
    }

    static JLabel subtitulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_SUBTITULO);
        etiqueta.setForeground(PRIMARIO);
        return etiqueta;
    }

    static JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_ETIQUETA);
        etiqueta.setForeground(TEXTO);
        return etiqueta;
    }

    static JLabel texto(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE);
        etiqueta.setForeground(TEXTO);
        return etiqueta;
    }

    static JLabel mensajeError() {
        JLabel etiqueta = new JLabel(" ");
        etiqueta.setFont(FUENTE);
        etiqueta.setForeground(ERROR);
        return etiqueta;
    }

    static JButton botonPrimario(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(FUENTE_NEGRITA);
        boton.setForeground(BLANCO);
        boton.setBackground(PRIMARIO);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    static JButton botonSecundario(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(FUENTE_NEGRITA);
        boton.setForeground(PRIMARIO);
        boton.setBackground(BLANCO);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(true);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_ENTRADA),
                BorderFactory.createEmptyBorder(7, 15, 7, 15)));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    /** Flat, left aligned button used by the sidebar navigation. */
    static JButton botonNavegacion(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(FUENTE);
        boton.setForeground(TEXTO);
        boton.setBackground(BLANCO);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    static JPanel panelTarjeta() {
        JPanel panel = new JPanel();
        panel.setBackground(BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        return panel;
    }

    static Border bordeEntrada() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_ENTRADA),
                BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    static <T extends JComponent> T entrada(T componente) {
        componente.setFont(FUENTE);
        componente.setForeground(TEXTO);
        componente.setBackground(BLANCO);
        componente.setBorder(bordeEntrada());
        componente.setPreferredSize(new Dimension(220, 36));
        return componente;
    }

    /** Small round icon used by the server status line. */
    static Icon punto(Color color) {
        return new Punto(color, 10);
    }

    static JMenuBar barraMenu(JMenu... menus) {
        JMenuBar barra = new JMenuBar();
        barra.setBackground(BLANCO);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        for (JMenu menu : menus) {
            barra.add(menu);
        }
        return barra;
    }

    static JMenu menu(String texto) {
        JMenu menu = new JMenu(texto);
        menu.setFont(FUENTE);
        menu.setForeground(TEXTO);
        return menu;
    }

    static JMenuItem opcion(String texto, Runnable accion) {
        JMenuItem opcion = new JMenuItem(texto);
        opcion.setFont(FUENTE);
        opcion.addActionListener(e -> accion.run());
        return opcion;
    }

    static JMenu menuAyuda(Component padre) {
        JMenu ayuda = menu("Ayuda");
        ayuda.add(opcion("Acerca de", () -> JOptionPane.showOptionDialog(padre, TITULO_VENTANA,
                "Acerca de", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                new Object[] {"Aceptar"}, "Aceptar")));
        return ayuda;
    }

    /** Runs the UI update on the EDT, immediately when already there. */
    static void enEdt(Runnable accion) {
        if (SwingUtilities.isEventDispatchThread()) {
            accion.run();
        } else {
            SwingUtilities.invokeLater(accion);
        }
    }

    /**
     * Runs a controller action off the EDT while the given controls stay
     * disabled. Unexpected failures are logged and reported through {@code alFallar}.
     */
    static void enSegundoPlano(Runnable accion, Consumer<String> alFallar, JComponent... controles) {
        for (JComponent control : controles) {
            control.setEnabled(false);
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                accion.run();
                return null;
            }

            @Override
            protected void done() {
                for (JComponent control : controles) {
                    control.setEnabled(true);
                }
                try {
                    get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    LOG.log(Level.SEVERE, "Background action failed", e.getCause());
                    alFallar.accept(ERROR_INESPERADO);
                }
            }
        }.execute();
    }

    private record Punto(Color color, int tamano) implements Icon {

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, tamano, tamano);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return tamano;
        }

        @Override
        public int getIconHeight() {
            return tamano;
        }
    }
}
