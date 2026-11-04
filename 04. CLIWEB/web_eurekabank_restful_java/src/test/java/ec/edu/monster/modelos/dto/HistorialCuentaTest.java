package ec.edu.monster.modelos.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * movimientos.jsp reads these objects through EL, which (Jakarta EE 10) only sees
 * JavaBean getters, not record accessors. Every property the view uses must exist.
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
}
