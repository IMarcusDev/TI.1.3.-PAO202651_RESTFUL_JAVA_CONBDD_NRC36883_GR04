package ec.edu.monster.vista;

import ec.edu.monster.modelo.MovimientoForm;

/**
 * Transfer screen: source account, target account (must differ) and amount.
 */
public class TransferenciaPanel extends OperacionPanel {

    public TransferenciaPanel() {
        super("Transferencia",
                "Mueva dinero de una cuenta a otra. Las cuentas de origen y destino deben ser diferentes.",
                new String[][] {
                    {"origen", "Cuenta de origen"},
                    {"destino", "Cuenta de destino"},
                    {"importe", "Importe (USD)"}
                });
    }

    @Override
    public MovimientoForm leer() {
        return MovimientoForm.transferencia(valor("origen"), valor("destino"), valor("importe"));
    }
}
