package ec.edu.monster.vista;

import ec.edu.monster.modelo.MovimientoForm;

/**
 * Withdrawal screen: source account and amount.
 */
public class RetiroPanel extends OperacionPanel {

    public RetiroPanel() {
        super("Retiro",
                "Registre un retiro de la cuenta de un cliente.",
                new String[][] {
                    {"cuenta", "Cuenta de origen"},
                    {"importe", "Importe (USD)"}
                });
    }

    @Override
    public MovimientoForm leer() {
        return MovimientoForm.retiro(valor("cuenta"), valor("importe"));
    }
}
