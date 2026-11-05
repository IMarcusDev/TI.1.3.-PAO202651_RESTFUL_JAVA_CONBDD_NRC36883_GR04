package ec.edu.monster.vista;

import ec.edu.monster.modelo.MovimientoForm;

/**
 * Transfer screen. An Empleado types both accounts and the key of the origin; a
 * Cliente sees their own account fixed as the origin (the server ties the token
 * to it), types only the target and is not asked for a key.
 */
public class TransferenciaPanel extends OperacionPanel {

    private final boolean esCliente;

    public TransferenciaPanel(boolean esCliente, String cuentaCliente) {
        super("Transferencia",
                esCliente ? "Mover fondos desde su cuenta. La cuenta de destino debe ser diferente."
                        : "Mueva dinero de una cuenta a otra. Las cuentas de origen y destino deben ser diferentes.",
                definicion(esCliente, cuentaCliente));
        this.esCliente = esCliente;
    }

    private static Campo[] definicion(boolean esCliente, String cuentaCliente) {
        if (esCliente) {
            return new Campo[] {
                Campo.fijo("origen", "Cuenta de origen", cuentaCliente),
                Campo.editable("destino", "Cuenta de destino"),
                Campo.editable("importe", "Importe (USD)")
            };
        }
        return new Campo[] {
            Campo.editable("origen", "Cuenta de origen"),
            Campo.editable("destino", "Cuenta de destino"),
            Campo.editable("importe", "Importe (USD)"),
            Campo.clave("clave", "Clave de la cuenta de origen")
        };
    }

    @Override
    public MovimientoForm leer() {
        return esCliente
                ? MovimientoForm.transferencia(valor("origen"), valor("destino"), valor("importe"))
                : MovimientoForm.transferenciaEmpleado(valor("origen"), valor("destino"), valor("clave"),
                        valor("importe"));
    }
}
