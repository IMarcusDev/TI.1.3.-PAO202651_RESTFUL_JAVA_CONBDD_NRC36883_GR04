package ec.edu.monster.modelos.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * movimientos.jsp reads these objects through EL, which (Jakarta EE 10) only sees
 * JavaBean getters, not record accessors. Every property the view uses must exist.
 * The movements are kept newest first, which is the order the table shows.
 */
class HistorialCuentaTest {

  private static Set<String> propiedades(Class<?> tipo) throws IntrospectionException {
    return Arrays.stream(Introspector.getBeanInfo(tipo).getPropertyDescriptors())
        .filter(p -> p.getReadMethod() != null)
        .map(PropertyDescriptor::getName)
        .collect(Collectors.toSet());
  }

  @Test
  void historialExposesThePropertiesUsedByTheView() throws IntrospectionException {
    Set<String> propiedades = propiedades(HistorialCuenta.class);
    assertTrue(propiedades.containsAll(Set.of("cuenta", "movimientos")), propiedades.toString());
  }

  @Test
  void cuentaExposesThePropertiesUsedByTheView() throws IntrospectionException {
    Set<String> propiedades = propiedades(HistorialCuenta.CuentaConsultada.class);
    assertTrue(propiedades.containsAll(
        Set.of("numero", "titular", "moneda", "saldoFormateado", "estado")), propiedades.toString());
  }

  @Test
  void movimientoExposesThePropertiesUsedByTheView() throws IntrospectionException {
    Set<String> propiedades = propiedades(HistorialCuenta.MovimientoConsultado.class);
    assertTrue(propiedades.containsAll(Set.of(
        "numero", "fecha", "tipo", "accion", "ingreso", "importeFormateado", "referencia")),
        propiedades.toString());
  }

  private static HistorialCuenta.MovimientoConsultado movimiento(String numero) {
    return new HistorialCuenta.MovimientoConsultado(
        numero, "20/10/2026 10:00", "Depósito", "INGRESO", BigDecimal.TEN, "-");
  }

  private static List<String> numeros(List<HistorialCuenta.MovimientoConsultado> movimientos) {
    return movimientos.stream().map(HistorialCuenta.MovimientoConsultado::numero).toList();
  }

  private static HistorialCuenta historial(HistorialCuenta.MovimientoConsultado... movimientos) {
    return new HistorialCuenta(EstadosEnum.OK, "Consulta de movimientos exitosa", null, List.of(movimientos));
  }

  @Test
  void movementsAreShownNewestFirst() {
    HistorialCuenta h = historial(
        movimiento("000003"), movimiento("000006"), movimiento("000007"), movimiento("000009"));

    assertEquals(List.of("000009", "000007", "000006", "000003"), numeros(h.getMovimientos()));
    assertEquals(h.getMovimientos(), h.movimientos());
  }

  @Test
  void newestFirstDoesNotDependOnTheServerOrder() {
    HistorialCuenta h = historial(movimiento("000010"), movimiento("000002"), movimiento("000009"));

    assertEquals(List.of("000010", "000009", "000002"), numeros(h.getMovimientos()));
  }

  @Test
  void unreadableNumbersGoLastInReverseArrivalOrder() {
    HistorialCuenta h = historial(
        movimiento("x"), movimiento("000002"), movimiento(null), movimiento("y"), movimiento("000005"));

    assertEquals(Arrays.asList("000005", "000002", "y", null, "x"), numeros(h.getMovimientos()));
  }
}
