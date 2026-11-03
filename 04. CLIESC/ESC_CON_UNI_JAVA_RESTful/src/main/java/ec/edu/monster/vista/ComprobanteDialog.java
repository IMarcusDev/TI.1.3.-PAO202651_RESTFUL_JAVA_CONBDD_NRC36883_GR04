package ec.edu.monster.vista;

import ec.edu.monster.modelo.Comprobante;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Modal receipt shown after the server accepts a deposit, withdrawal or transfer.
 */
public class ComprobanteDialog extends JDialog {

    public ComprobanteDialog(Window propietario, Comprobante comprobante) {
        super(propietario, "Comprobante de operación", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout(0, 18));
        raiz.setBackground(Estilos.FONDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        JLabel titulo = Estilos.titulo("Operación exitosa");
        raiz.add(titulo, BorderLayout.NORTH);
        raiz.add(detalle(comprobante), BorderLayout.CENTER);

        JButton aceptar = Estilos.botonPrimario("Aceptar");
        aceptar.addActionListener(e -> dispose());
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pie.setOpaque(false);
        pie.add(aceptar);
        raiz.add(pie, BorderLayout.SOUTH);

        setContentPane(raiz);
        getRootPane().setDefaultButton(aceptar);
        pack();
        setLocationRelativeTo(propietario);
    }

    private JPanel detalle(Comprobante comprobante) {
        JPanel tarjeta = Estilos.panelTarjeta();
        tarjeta.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 0, 4, 24);

        int fila = 0;
        fila = agregar(tarjeta, c, fila, "Operación", comprobante.operacion());
        fila = agregar(tarjeta, c, fila, "Cuenta de origen", comprobante.cuentaOrigen());
        if (comprobante.cuentaDestino() != null && !comprobante.cuentaDestino().isBlank()) {
            fila = agregar(tarjeta, c, fila, "Cuenta de destino", comprobante.cuentaDestino());
        }
        fila = agregar(tarjeta, c, fila, "Importe", comprobante.importe());
        fila = agregar(tarjeta, c, fila, "N.º de movimiento", String.valueOf(comprobante.idMovimiento()));
        fila = agregar(tarjeta, c, fila, "Fecha", comprobante.fecha());
        agregar(tarjeta, c, fila, "Mensaje", comprobante.mensaje());
        return tarjeta;
    }

    private int agregar(JPanel panel, GridBagConstraints c, int fila, String etiqueta, String valor) {
        c.gridy = fila++;
        c.gridx = 0;
        c.weightx = 0;
        panel.add(Estilos.etiqueta(etiqueta), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(Estilos.texto(valor == null ? "" : valor), c);
        return fila;
    }
}
