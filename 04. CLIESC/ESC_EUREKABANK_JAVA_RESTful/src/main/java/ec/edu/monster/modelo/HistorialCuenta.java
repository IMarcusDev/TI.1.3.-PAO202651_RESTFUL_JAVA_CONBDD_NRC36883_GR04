package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Answer of "Consultar movimientos": the account data and its movements, already formatted for
 * the table. Same rows the web client shows.
 */
public record HistorialCuenta(CuentaConsultada cuenta, List<MovimientoConsultado> movimientos) {

    public HistorialCuenta {
        movimientos = recientesPrimero(movimientos);
    }

    /**
     * Newest first, which is the order every screen shows. The movement number is the server's
     * global ID, which grows with every record, so the largest one is the newest. It does not depend
     * on the response order; numbers that cannot be read go last, in reverse arrival order.
     */
    private static List<MovimientoConsultado> recientesPrimero(List<MovimientoConsultado> movimientos) {
        List<MovimientoConsultado> copia = new ArrayList<>(movimientos);
        Collections.reverse(copia);
        copia.sort(Comparator.comparingLong(HistorialCuenta::numeroDe).reversed());
        return List.copyOf(copia);
    }

    private static long numeroDe(MovimientoConsultado movimiento) {
        try {
            return Long.parseLong(movimiento.numero().trim());
        } catch (RuntimeException e) {
            return Long.MIN_VALUE;
        }
    }

    public boolean sinMovimientos() {
        return movimientos.isEmpty();
    }

    /** Account data shown above the table. */
    public record CuentaConsultada(String numero, String titular, String moneda, BigDecimal saldo,
            String estado) {

        public String saldoFormateado() {
            return Formatos.dinero(saldo);
        }
    }

    /** One movement row: number and date already formatted, amount as a BigDecimal. */
    public record MovimientoConsultado(String numero, String fecha, String tipo, String accion,
            BigDecimal importe, String referencia) {

        public String importeFormateado() {
            return Formatos.dinero(importe);
        }

        public boolean esIngreso() {
            return "INGRESO".equals(accion);
        }
    }
}
