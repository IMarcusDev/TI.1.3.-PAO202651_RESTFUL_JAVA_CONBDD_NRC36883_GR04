package ec.edu.monster.vista;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Home screen: greeting and one card per operation. The three movements are only
 * available to an "Empleado", exactly like the AuthFilter of the web client; the
 * other roles see them disabled with "Próximamente".
 */
public class MenuPanel extends JPanel {

    public MenuPanel(String nombre, boolean empleado, Runnable alDeposito, Runnable alRetiro,
            Runnable alTransferencia) {
        super(new BorderLayout(0, 22));
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(28, 30, 24, 30));

        JPanel encabezado = new JPanel(new BorderLayout(0, 6));
        encabezado.setOpaque(false);
        encabezado.add(Estilos.titulo("Bienvenido, " + nombre), BorderLayout.NORTH);
        encabezado.add(Estilos.texto("Seleccione una operación para continuar."), BorderLayout.SOUTH);
        add(encabezado, BorderLayout.NORTH);

        JPanel tarjetas = new JPanel(new GridLayout(2, 2, 16, 16));
        tarjetas.setOpaque(false);
        tarjetas.add(tarjeta("Consultar movimientos", "Disponible próximamente", false, () -> { }));
        tarjetas.add(tarjeta("Depósito", "Registre un depósito en la cuenta de un cliente", empleado, alDeposito));
        tarjetas.add(tarjeta("Retiro", "Registre un retiro de la cuenta de un cliente", empleado, alRetiro));
        tarjetas.add(tarjeta("Transferencia", "Mueva dinero de una cuenta a otra", empleado, alTransferencia));
        add(tarjetas, BorderLayout.CENTER);
    }

    private JButton tarjeta(String titulo, String descripcion, boolean habilitada, Runnable accion) {
        String detalle = habilitada ? descripcion : "Próximamente";
        JButton boton = new JButton("<html><div style='width:180px'><b>" + titulo
                + "</b><br><br>" + detalle + "</div></html>");
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setVerticalAlignment(SwingConstants.TOP);
        boton.setBackground(Estilos.BLANCO);
        boton.setForeground(habilitada ? Estilos.TEXTO : Estilos.APAGADO);
        boton.setFont(Estilos.FUENTE);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        boton.setEnabled(habilitada);
        boton.setCursor(Cursor.getPredefinedCursor(
                habilitada ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        boton.addActionListener(e -> accion.run());
        return boton;
    }
}
