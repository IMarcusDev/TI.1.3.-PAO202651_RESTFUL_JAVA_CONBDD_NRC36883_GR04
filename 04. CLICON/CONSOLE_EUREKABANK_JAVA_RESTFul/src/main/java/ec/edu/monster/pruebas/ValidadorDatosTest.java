package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.vista.utils.Formato;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

//revisado y aprovado por Marcos Escobar
class ValidadorDatosTest {
  @Test
  void cuentaVaciaCortaOConLetras() {
    assertEquals(ValidadorDatos.INGRESE_CUENTA, ValidadorDatos.errorCuenta(""));
    assertEquals(ValidadorDatos.CUENTA_INVALIDA, ValidadorDatos.errorCuenta("0010001"));
    assertEquals(ValidadorDatos.CUENTA_INVALIDA, ValidadorDatos.errorCuenta("0010000A"));
    assertNull(ValidadorDatos.errorCuenta("00100001"));
  }

  @Test
  void claveDeCuentaDeSeisDigitos() {
    assertEquals(ValidadorDatos.INGRESE_CLAVE, ValidadorDatos.errorClaveCuenta(""));
    assertEquals(ValidadorDatos.CLAVE_INVALIDA, ValidadorDatos.errorClaveCuenta("12345"));
    assertNull(ValidadorDatos.errorClaveCuenta("123456"));
  }

  @Test
  void importeConComaOPuntoYDosDecimales() {
    assertEquals(new BigDecimal("10.50"), ValidadorDatos.convertirImporte("10,5"));
    assertEquals(new BigDecimal("120.50"), ValidadorDatos.convertirImporte("120.50"));
    assertNull(ValidadorDatos.convertirImporte("10.555"));
    assertNull(ValidadorDatos.convertirImporte("0"));
    assertNull(ValidadorDatos.convertirImporte("-5"));
    assertEquals(ValidadorDatos.IMPORTE_INVALIDO, ValidadorDatos.errorImporte("abc"));
  }

  @Test
  void formatoDelErsParaImportesYFechas() {
    assertEquals("1.234,50 USD", Formato.importe(new BigDecimal("1234.5"), "USD"));
    assertEquals("03/11/2026 16:07", Formato.fecha("2026-11-03T16:07:05"));
    assertEquals("31/10/2026 16:40", Formato.fecha("31/10/2026 16:40"));
  }
}
