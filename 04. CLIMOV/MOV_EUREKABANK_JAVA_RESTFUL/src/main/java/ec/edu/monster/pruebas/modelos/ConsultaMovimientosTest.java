package ec.edu.monster.pruebas.modelos;

import static org.junit.Assert.assertEquals;

import ec.edu.monster.modelos.data.ConsultaMovimientos;
import ec.edu.monster.modelos.data.Cuenta;
import ec.edu.monster.modelos.data.Movimiento;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class ConsultaMovimientosTest {

    private static final Cuenta CUENTA =
            new Cuenta("00100001", "Ana Torres", "USD", new BigDecimal("540"), "ACTIVO");

    private static Movimiento movimiento(String numero) {
        return new Movimiento(numero, "2026-11-04 21:20", "Depósito", "INGRESO", BigDecimal.TEN, "");
    }

    private static List<String> numeros(List<Movimiento> movimientos) {
        List<String> numeros = new ArrayList<>();
        for (Movimiento m : movimientos) {
            numeros.add(m.getNumero());
        }
        return numeros;
    }

    @Test
    public void recientes_elUltimoMovimientoVaPrimero() {
        ConsultaMovimientos consulta = new ConsultaMovimientos(CUENTA, Arrays.asList(
                movimiento("3"), movimiento("6"), movimiento("7"), movimiento("8"), movimiento("9")));

        assertEquals(Arrays.asList("9", "8", "7", "6", "3"), numeros(consulta.getMovimientosRecientes()));
        assertEquals(Arrays.asList("3", "6", "7", "8", "9"), numeros(consulta.getMovimientos()));
    }

    @Test
    public void recientes_noDependeDelOrdenDeLaRespuesta() {
        ConsultaMovimientos consulta = new ConsultaMovimientos(CUENTA, Arrays.asList(
                movimiento("10"), movimiento("2"), movimiento("9")));

        assertEquals(Arrays.asList("10", "9", "2"), numeros(consulta.getMovimientosRecientes()));
    }
}
