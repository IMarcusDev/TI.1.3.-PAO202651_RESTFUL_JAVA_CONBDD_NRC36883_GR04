package ec.edu.monster.modelos.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConsultaMovimientos {
    private final Cuenta cuenta;
    private final List<Movimiento> movimientos;
    private final List<Movimiento> recientes;

    public ConsultaMovimientos(Cuenta cuenta, List<Movimiento> movimientos) {
        this.cuenta = cuenta;
        this.movimientos = Collections.unmodifiableList(movimientos);
        this.recientes = Collections.unmodifiableList(ordenarRecientes(movimientos));
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    /** Movimientos en el orden en que los envía el servidor. */
    public List<Movimiento> getMovimientos() {
        return movimientos;
    }

    /** Movimientos del más reciente al más antiguo: es el orden en que se muestran. */
    public List<Movimiento> getMovimientosRecientes() {
        return recientes;
    }

    /**
     * El número de movimiento es el ID global del servidor, que crece con cada registro, así que el
     * mayor es el más reciente. No depende del orden de la respuesta; los números que no se pueden
     * leer quedan al final, en el orden inverso al de llegada.
     */
    private static List<Movimiento> ordenarRecientes(List<Movimiento> movimientos) {
        List<Movimiento> copia = new ArrayList<>(movimientos);
        Collections.reverse(copia);
        copia.sort(Comparator.comparingLong(ConsultaMovimientos::numero).reversed());
        return copia;
    }

    private static long numero(Movimiento movimiento) {
        try {
            return Long.parseLong(movimiento.getNumero().trim());
        } catch (RuntimeException e) {
            return Long.MIN_VALUE;
        }
    }
}
