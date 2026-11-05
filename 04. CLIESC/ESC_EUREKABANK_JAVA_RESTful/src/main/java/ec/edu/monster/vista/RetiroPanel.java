package ec.edu.monster.vista;

import ec.edu.monster.modelo.MovimientoForm;

/**
 * Withdrawal screen. An Empleado types any account and its key; a Cliente sees
 * their own account fixed (the server ties the token to it) and is not asked for
 * a key.
 */
public class RetiroPanel extends OperacionPanel {

    private final boolean esCliente;

    public RetiroPanel(boolean esCliente, String cuentaCliente) {
        super("Retiro",
                esCliente ? "Registrar una salida de su cuenta."
                        : "Registre un retiro de la cuenta de un cliente.",
                definicion(esCliente, cuentaCliente));
        this.esCliente = esCliente;
    }

    private static Campo[] definicion(boolean esCliente, String cuentaCliente) {
        if (esCliente) {
            return new Campo[] {
                Campo.fijo("cuenta", "Cuenta de origen", cuentaCliente),
                Campo.editable("importe", "Importe (USD)")
            };
        }
        return new Campo[] {
            Campo.editable("cuenta", "Cuenta de origen"),
            Campo.editable("importe", "Importe (USD)"),
            Campo.clave("clave", "Clave de la cuenta")
        };
    }

    @Override
    public MovimientoForm leer() {
        return esCliente
                ? MovimientoForm.retiro(valor("cuenta"), valor("importe"))
                : MovimientoForm.retiroEmpleado(valor("cuenta"), valor("clave"), valor("importe"));
    }
}
