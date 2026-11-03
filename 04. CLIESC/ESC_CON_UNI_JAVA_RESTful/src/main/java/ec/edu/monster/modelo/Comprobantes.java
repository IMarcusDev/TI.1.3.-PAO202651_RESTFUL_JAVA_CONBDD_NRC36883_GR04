package ec.edu.monster.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Builds the receipt shown after a movement, from the validated form and the
 * server answer. The date is the local date of the client.
 */
public final class Comprobantes {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Comprobantes() {
    }

    public static Comprobante deposito(MovimientoForm form, ResultadoMovimiento resultado) {
        return new Comprobante("Depósito", "Empleado " + form.getEmpleado(), form.getCuenta(),
                form.getImporteFormateado(), resultado.idMovimiento(), hoy(), resultado.mensaje());
    }

    public static Comprobante retiro(MovimientoForm form, ResultadoMovimiento resultado) {
        return new Comprobante("Retiro", form.getCuenta(), "",
                form.getImporteFormateado(), resultado.idMovimiento(), hoy(), resultado.mensaje());
    }

    public static Comprobante transferencia(MovimientoForm form, ResultadoMovimiento resultado) {
        return new Comprobante("Transferencia", form.getOrigen(), form.getDestino(),
                form.getImporteFormateado(), resultado.idMovimiento(), hoy(), resultado.mensaje());
    }

    private static String hoy() {
        return LocalDate.now().format(FECHA);
    }
}
