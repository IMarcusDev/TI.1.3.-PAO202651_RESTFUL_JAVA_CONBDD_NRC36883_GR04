package ec.edu.monster.pruebas.controlador;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import ec.edu.monster.controlador.ValidadorDatos;
import java.math.BigDecimal;
import org.junit.Test;

public class ValidadorDatosTest {

    @Test
    public void cuenta_exigeOchoDigitos() {
        assertNull(ValidadorDatos.errorCuenta("00100001"));
        assertEquals("Ingrese el número de cuenta", ValidadorDatos.errorCuenta(""));
        assertEquals("El número de cuenta debe tener 8 dígitos", ValidadorDatos.errorCuenta("1234567"));
        assertEquals("El número de cuenta debe tener 8 dígitos", ValidadorDatos.errorCuenta("1234567a"));
    }

    @Test
    public void claveDeCuenta_exigeSeisDigitos() {
        assertNull(ValidadorDatos.errorClaveCuenta("123456"));
        assertEquals("Ingrese la clave", ValidadorDatos.errorClaveCuenta(""));
        assertEquals("La clave de la cuenta debe tener 6 dígitos", ValidadorDatos.errorClaveCuenta("12345"));
    }

    @Test
    public void importe_aceptaPuntoOComaYHastaDosDecimales() {
        assertEquals(new BigDecimal("150.50"), ValidadorDatos.convertirImporte("150,5"));
        assertEquals(new BigDecimal("150.25"), ValidadorDatos.convertirImporte("150.25"));
        assertEquals(new BigDecimal("10.00"), ValidadorDatos.convertirImporte("10"));
        assertNotNull(ValidadorDatos.convertirImporte("9999999999.99"));
    }

    @Test
    public void importe_rechazaCeroMasDeDosDecimalesYMasDeDiezEnteros() {
        assertNull(ValidadorDatos.convertirImporte("0"));
        assertNull(ValidadorDatos.convertirImporte("0,00"));
        assertNull(ValidadorDatos.convertirImporte("1.234"));
        assertNull(ValidadorDatos.convertirImporte("12345678901"));
        assertNull(ValidadorDatos.convertirImporte("-5"));
        assertNull(ValidadorDatos.convertirImporte("abc"));
        assertEquals("Ingrese el importe", ValidadorDatos.errorImporte(""));
        assertEquals("El importe debe ser mayor que cero y tener como máximo dos decimales",
                ValidadorDatos.errorImporte("0"));
    }
}
