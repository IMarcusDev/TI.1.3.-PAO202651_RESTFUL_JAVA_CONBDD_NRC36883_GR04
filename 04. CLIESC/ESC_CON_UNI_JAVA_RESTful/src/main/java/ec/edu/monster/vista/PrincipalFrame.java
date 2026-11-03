package ec.edu.monster.vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Main window shown after login. The "Archivo" menu switches between the
 * conversion and catalog screens and closes the session. Controllers are
 * wired by the composition root through the panel getters.
 */
public class PrincipalFrame extends JFrame {

    private static final String CONVERTIR = "convertir";
    private static final String CATALOGO = "catalogo";

    private final ConversionPanel conversionPanel = new ConversionPanel();
    private final CatalogoPanel catalogoPanel = new CatalogoPanel();
    private final CardLayout pantallas = new CardLayout();
    private final JPanel contenido = new JPanel(pantallas);
    private final IndicadorEstado indicadorEstado = new IndicadorEstado("Sesión activa · ");

    public PrincipalFrame(Runnable alCerrarSesion) {
        super(Estilos.TITULO_VENTANA);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(760, 520);
        setLocationRelativeTo(null);

        JMenu archivo = Estilos.menu("Archivo");
        archivo.add(Estilos.opcion("Convertir unidades", () -> pantallas.show(contenido, CONVERTIR)));
        archivo.add(Estilos.opcion("Consultar catálogo", () -> pantallas.show(contenido, CATALOGO)));
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
        setJMenuBar(Estilos.barraMenu(archivo, Estilos.menuAyuda(this)));

        contenido.setOpaque(false);
        contenido.add(conversionPanel, CONVERTIR);
        contenido.add(catalogoPanel, CATALOGO);

        indicadorEstado.setBorder(BorderFactory.createEmptyBorder(8, 34, 16, 34));

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilos.FONDO);
        raiz.add(contenido, BorderLayout.CENTER);
        raiz.add(indicadorEstado, BorderLayout.SOUTH);
        setContentPane(raiz);
    }

    public ConversionPanel getConversionPanel() {
        return conversionPanel;
    }

    public CatalogoPanel getCatalogoPanel() {
        return catalogoPanel;
    }

    public EstadoVista getIndicadorEstado() {
        return indicadorEstado;
    }
}
