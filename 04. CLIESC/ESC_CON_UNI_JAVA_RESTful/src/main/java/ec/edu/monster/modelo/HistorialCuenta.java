package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Answer of "Consultar movimientos": the account data and its movements, already formatted for
 * the table. Same rows the web client shows.
 */
public record HistorialCuenta(CuentaConsultada cuenta, List<MovimientoConsultado> movimientos) {

    public HistorialCuenta {
        movimientos = List.copyOf(movimientos);
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
