package ec.edu.monster.modelos.data;

import java.util.Collections;
import java.util.List;

public class ConsultaMovimientos {
    private final Cuenta cuenta;
    private final List<Movimiento> movimientos;

    public ConsultaMovimientos(Cuenta cuenta, List<Movimiento> movimientos) {
        this.cuenta = cuenta;
        this.movimientos = Collections.unmodifiableList(movimientos);
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public List<Movimiento> getMovimientos() {
        return movimientos;
    }
}
