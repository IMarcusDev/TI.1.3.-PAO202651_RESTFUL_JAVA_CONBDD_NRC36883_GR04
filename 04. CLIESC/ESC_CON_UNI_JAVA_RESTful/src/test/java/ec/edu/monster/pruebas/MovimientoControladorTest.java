package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.controlador.MovimientoControlador;
import ec.edu.monster.dobles.MovimientoServicioFalso;
import ec.edu.monster.dobles.OperacionVistaFalsa;
import ec.edu.monster.modelo.Comprobantes;
import ec.edu.monster.modelo.DatosSesion;
import ec.edu.monster.modelo.MovimientoForm;
import ec.edu.monster.modelo.Sesion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MovimientoControladorTest {

    private final Sesion sesion = new Sesion();
    private final OperacionVistaFalsa vista = new OperacionVistaFalsa();
    private final MovimientoServicioFalso servicio = new MovimientoServicioFalso();

    @BeforeEach
    void conectar() {
        sesion.iniciar("admin", new DatosSesion("Administrador", "Empleado", "token-1"));
        new MovimientoControlador(vista, sesion,
                form -> "¿Depositar " + form.getImporteFormateado() + " en la cuenta " + form.getCuenta() + "?",
                (token, form) -> Comprobantes.deposito(form,
                        servicio.deposito(token, form.getEmpleado(), form.getCuenta(), form.getImporte())));
    }

    @Test
    void invalidFormShowsTheErrorsAndDoesNotCallTheService() {
        vista.revisar(MovimientoForm.deposito("", "", ""));

        assertEquals("Ingrese el código de empleado", vista.getErrores().get("empleado"));
        assertEquals("Ingrese la cuenta de destino", vista.getErrores().get("cuenta"));
        assertEquals("Ingrese el importe", vista.getErrores().get("importe"));
        assertEquals(0, servicio.getLlamadas());
        assertNull(vista.getComprobante());
        assertTrue(vista.isErroresLimpiados());
    }

    @Test
    void confirmationIsAskedBeforeCallingTheService() {
        vista.setAceptar(false);

        vista.revisar(MovimientoForm.deposito("0001", "0002", "100"));

        assertEquals("¿Depositar $ 100.00 en la cuenta 0002?", vista.getPregunta());
        assertEquals(0, servicio.getLlamadas());
        assertNull(vista.getComprobante());
    }

    @Test
    void successShowsTheReceiptAndClearsTheForm() {
        vista.revisar(MovimientoForm.deposito("0001", "0002", "100"));

        assertNotNull(vista.getComprobante());
        assertEquals("Depósito", vista.getComprobante().operacion());
        assertEquals("Empleado 0001", vista.getComprobante().cuentaOrigen());
        assertEquals("0002", vista.getComprobante().cuentaDestino());
        assertEquals(777, vista.getComprobante().idMovimiento());
        assertEquals("token-1", servicio.getTokenRecibido());
        assertEquals("0001", servicio.getOrigenRecibido());
        assertEquals("0002", servicio.getDestinoRecibido());
        assertTrue(vista.isLimpiada());
    }

    @Test
    void serverErrorKeepsTheFormAndShowsTheMessage() {
        servicio.fallarCon("Token inválido");

        vista.revisar(MovimientoForm.deposito("0001", "0002", "100"));

        assertEquals("Token inválido", vista.getError());
        assertNull(vista.getComprobante());
        assertFalse(vista.isLimpiada());
    }

    @Test
    void inactiveSessionDoesNotCallTheService() {
        sesion.cerrar();

        vista.revisar(MovimientoForm.deposito("0001", "0002", "100"));

        assertEquals("La sesión no está activa. Vuelva a iniciar sesión.", vista.getError());
        assertEquals(0, servicio.getLlamadas());
    }
}
