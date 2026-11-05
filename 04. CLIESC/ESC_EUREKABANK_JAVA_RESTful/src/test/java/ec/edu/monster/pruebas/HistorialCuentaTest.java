package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ec.edu.monster.modelo.HistorialCuenta;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The table shows the movements newest first: the larger movement number is the newer one. */
class HistorialCuentaTest {

    private static final HistorialCuenta.CuentaConsultada CUENTA =
            new HistorialCuenta.CuentaConsultada("00100001", "Ana Torres", "USD", null, "ACTIVO");

    private static HistorialCuenta.MovimientoConsultado movimiento(String numero) {
        return new HistorialCuenta.MovimientoConsultado(numero, "20/10/2026 10:00", "Depósito", "INGRESO",
                BigDecimal.TEN, "-");
    }

    private static List<String> numeros(HistorialCuenta historial) {
        return historial.movimientos().stream().map(HistorialCuenta.MovimientoConsultado::numero).toList();
    }

    @Test
    void movementsAreShownNewestFirst() {
        HistorialCuenta historial = new HistorialCuenta(CUENTA, List.of(
                movimiento("000003"), movimiento("000006"), movimiento("000007"), movimiento("000009")));

        assertEquals(List.of("000009", "000007", "000006", "000003"), numeros(historial));
    }

    @Test
    void newestFirstDoesNotDependOnTheServerOrder() {
        HistorialCuenta historial = new HistorialCuenta(CUENTA, List.of(
                movimiento("000010"), movimiento("000002"), movimiento("000009")));

        assertEquals(List.of("000010", "000009", "000002"), numeros(historial));
    }

    @Test
    void unreadableNumbersGoLastInReverseArrivalOrder() {
        HistorialCuenta historial = new HistorialCuenta(CUENTA, List.of(
                movimiento("x"), movimiento("000002"), movimiento(null), movimiento("y"), movimiento("000005")));

        assertEquals(Arrays.asList("000005", "000002", "y", null, "x"), numeros(historial));
    }
}
