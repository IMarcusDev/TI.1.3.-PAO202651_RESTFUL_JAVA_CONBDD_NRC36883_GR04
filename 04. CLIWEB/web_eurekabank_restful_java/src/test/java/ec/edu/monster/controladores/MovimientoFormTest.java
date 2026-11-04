package ec.edu.monster.controladores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Plain unit tests of the deposit/withdrawal/transfer form validation. */
class MovimientoFormTest {
  private static final String IMPORTE_INVALIDO =
      "El importe debe ser mayor que cero y tener como máximo dos decimales";

  @ParameterizedTest
  @CsvSource({
      "150, 150",
      "150.5, 150.5",
      "'150,75', 150.75",
      "' 0.01 ', 0.01",
      "9999999999.99, 9999999999.99"
  })
  void validAmountsAreParsedWithDotOrComma(String texto, String esperado) {
    MovimientoForm form = MovimientoForm.retiro("00100001", texto);

    assertTrue(form.isValido(), () -> form.getErrores().toString());
    assertEquals(0, new BigDecimal(esperado).compareTo(form.getImporte()));
  }

  @ParameterizedTest
  @ValueSource(strings = {"NaN", "abc", "0", "0.00", "1.234", "-5", "Infinity", "1e3",
      "1,5.0", "12345678901", "1.", ".5"})
  void invalidAmountsAreRejected(String texto) {
    MovimientoForm form = MovimientoForm.retiro("00100001", texto);

    assertFalse(form.isValido());
    assertEquals(IMPORTE_INVALIDO, form.getErrores().get("importe"));
    assertNull(form.getImporte());
  }

  @Test
  void missingFieldsHaveRequiredMessages() {
    MovimientoForm form = MovimientoForm.retiro("  ", null);

    assertEquals("Ingrese el número de cuenta", form.getErrores().get("cuenta"));
    assertEquals("Ingrese el importe", form.getErrores().get("importe"));
  }

  @Test
  void accountIsTrimmedAndLimitedTo30Characters() {
    assertEquals("ABC-01", MovimientoForm.retiro("  ABC-01 ", "10").getCuenta());

    MovimientoForm largo = MovimientoForm.retiro("1".repeat(31), "10");
    assertFalse(largo.isValido());
    assertTrue(largo.getErrores().containsKey("cuenta"));
    assertTrue(MovimientoForm.retiro("1".repeat(30), "10").isValido());
  }

  @Test
  void transferRequiresBothAccounts() {
    MovimientoForm form = MovimientoForm.transferencia("", " ", "10");

    assertEquals("Ingrese la cuenta de origen", form.getErrores().get("origen"));
    assertEquals("Ingrese la cuenta de destino", form.getErrores().get("destino"));
  }

  @Test
  void transferRejectsSameAccountAfterTrim() {
    MovimientoForm form = MovimientoForm.transferencia("00100001", " 00100001 ", "10");

    assertFalse(form.isValido());
    assertEquals("La cuenta de origen y la de destino deben ser diferentes",
        form.getErrores().get("destino"));
  }

  @Test
  void depositOnlyRequiresTargetAccountAndAmount() {
    MovimientoForm form = MovimientoForm.deposito(" ", null);

    assertFalse(form.isValido());
    // The server takes the employee from the token: no employee field to validate
    assertEquals(Map.of("cuenta", "Ingrese la cuenta de destino", "importe", "Ingrese el importe"),
        form.getErrores());
  }

  @Test
  void depositTrimsAccountAndLimitsItTo30Characters() {
    MovimientoForm form = MovimientoForm.deposito(" 0004 ", "100");
    assertTrue(form.isValido(), () -> form.getErrores().toString());
    assertEquals("0004", form.getCuenta());
    assertEquals(new BigDecimal("100"), form.getImporte());

    MovimientoForm largo = MovimientoForm.deposito("2".repeat(31), "10");
    assertEquals(Map.of("cuenta", "La cuenta debe tener como máximo 30 caracteres"),
        largo.getErrores());
    assertTrue(MovimientoForm.deposito("2".repeat(30), "10").isValido());
  }

  @Test
  void amountIsFormattedUsStyleWithTwoDecimals() {
    assertEquals("$ 1,250.00", MovimientoForm.formatearImporte(new BigDecimal("1250")));
    assertEquals("$ 0.50", MovimientoForm.formatearImporte(new BigDecimal("0.5")));
    assertEquals("$ 150.00", MovimientoForm.transferencia("1", "2", "150").getImporteFormateado());
  }
}
