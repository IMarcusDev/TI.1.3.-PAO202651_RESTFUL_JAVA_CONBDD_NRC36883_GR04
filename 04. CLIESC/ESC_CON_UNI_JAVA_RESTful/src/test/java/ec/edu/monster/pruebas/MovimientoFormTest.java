package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelo.Comprobante;
import ec.edu.monster.modelo.Comprobantes;
import ec.edu.monster.modelo.MovimientoForm;
import ec.edu.monster.modelo.ResultadoMovimiento;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Plain unit tests of the withdrawal/transfer form validation and the receipt. */
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
    void depositRequiresEmployeeAndTargetAccount() {
        MovimientoForm form = MovimientoForm.deposito(" ", null, "10");

        assertFalse(form.isValido());
        assertEquals("Ingrese el código de empleado", form.getErrores().get("empleado"));
        assertEquals("Ingrese la cuenta de destino", form.getErrores().get("cuenta"));
    }

    @Test
    void depositTrimsIdsAndLimitsThemTo30Characters() {
        MovimientoForm form = MovimientoForm.deposito(" 0001 ", " 0004 ", "100");
        assertTrue(form.isValido(), () -> form.getErrores().toString());
        assertEquals("0001", form.getEmpleado());
        assertEquals("0004", form.getCuenta());

        MovimientoForm largo = MovimientoForm.deposito("1".repeat(31), "2".repeat(31), "10");
        assertTrue(largo.getErrores().containsKey("empleado"));
        assertTrue(largo.getErrores().containsKey("cuenta"));
        assertTrue(MovimientoForm.deposito("1".repeat(30), "2".repeat(30), "10").isValido());
    }

    @Test
    void amountIsFormattedUsStyleWithTwoDecimals() {
        assertEquals("$ 1,250.00", MovimientoForm.formatearImporte(new BigDecimal("1250")));
        assertEquals("$ 0.50", MovimientoForm.formatearImporte(new BigDecimal("0.5")));
        assertEquals("$ 150.00", MovimientoForm.transferencia("1", "2", "150").getImporteFormateado());
    }

    @Test
    void depositReceiptShowsEmployeeAndTargetAccount() {
        MovimientoForm form = MovimientoForm.deposito("0001", "0004", "100");
        Comprobante comprobante = Comprobantes.deposito(form, new ResultadoMovimiento(101, "Depósito registrado"));

        assertEquals("Depósito", comprobante.operacion());
        assertEquals("Empleado 0001", comprobante.cuentaOrigen());
        assertEquals("0004", comprobante.cuentaDestino());
        assertEquals("$ 100.00", comprobante.importe());
        assertEquals(101, comprobante.idMovimiento());
        assertEquals("Depósito registrado", comprobante.mensaje());
        assertFalse(comprobante.fecha().isBlank());
    }

    @Test
    void withdrawalReceiptHasNoTargetAccount() {
        MovimientoForm form = MovimientoForm.retiro("0004", "50");
        Comprobante comprobante = Comprobantes.retiro(form, new ResultadoMovimiento(102, "Retiro registrado"));

        assertEquals("Retiro", comprobante.operacion());
        assertEquals("0004", comprobante.cuentaOrigen());
        assertEquals("", comprobante.cuentaDestino());
    }

    @Test
    void transferReceiptShowsBothAccounts() {
        MovimientoForm form = MovimientoForm.transferencia("0001", "0002", "25.5");
        Comprobante comprobante = Comprobantes.transferencia(form,
                new ResultadoMovimiento(103, "Transferencia registrada"));

        assertEquals("Transferencia", comprobante.operacion());
        assertEquals("0001", comprobante.cuentaOrigen());
        assertEquals("0002", comprobante.cuentaDestino());
        assertEquals("$ 25.50", comprobante.importe());
    }
}
