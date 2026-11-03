package ec.edu.monster.vista;

import ec.edu.monster.modelo.MovimientoForm;

/**
 * Deposit screen: employee code, target account and amount.
 */
public class DepositoPanel extends OperacionPanel {

    public DepositoPanel() {
        super("Depósito",
                "Registre un depósito en la cuenta de un cliente. El código de empleado se envía como cuenta de origen.",
                new String[][] {
                    {"empleado", "Código de empleado"},
                    {"cuenta", "Cuenta de destino"},
                    {"importe", "Importe (USD)"}
                });
    }

    @Override
    public MovimientoForm leer() {
        return MovimientoForm.deposito(valor("empleado"), valor("cuenta"), valor("importe"));
    }
}
