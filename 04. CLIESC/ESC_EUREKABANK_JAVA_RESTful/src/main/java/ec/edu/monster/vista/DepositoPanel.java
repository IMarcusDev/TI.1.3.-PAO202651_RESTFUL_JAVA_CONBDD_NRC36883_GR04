package ec.edu.monster.vista;

import ec.edu.monster.modelo.MovimientoForm;

/**
 * Deposit screen (employee only): target account and amount. The ERS server takes
 * the employee from the token, so there is no employee field and no key.
 */
public class DepositoPanel extends OperacionPanel {

    public DepositoPanel() {
        super("Depósito",
                "Registre un depósito en la cuenta de un cliente.",
                new Campo[] {
                    Campo.editable("cuenta", "Cuenta de destino"),
                    Campo.editable("importe", "Importe (USD)")
                });
    }

    @Override
    public MovimientoForm leer() {
        return MovimientoForm.deposito(valor("cuenta"), valor("importe"));
    }
}
