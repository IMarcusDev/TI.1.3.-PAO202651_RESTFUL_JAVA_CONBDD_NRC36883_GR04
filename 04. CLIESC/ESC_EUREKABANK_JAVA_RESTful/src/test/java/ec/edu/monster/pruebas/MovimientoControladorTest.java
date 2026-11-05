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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MovimientoControladorTest {

    private final Sesion sesion = new Sesion();
    private final OperacionVistaFalsa vista = new OperacionVistaFalsa();
    private final MovimientoServicioFalso servicio = new MovimientoServicioFalso();

    @BeforeEach
    void conectar() {
        sesion.iniciar("cajero", new DatosSesion("Carla Andrade", "Empleado", DatosSesion.EMPLEADO, "token-1"));
    }

    @Nested
    class Deposito {

        @BeforeEach
        void conectarPantalla() {
            new MovimientoControlador(vista, sesion,
                    form -> "¿Depositar " + form.getImporteFormateado() + " en la cuenta " + form.getCuenta() + "?",
                    (token, form) -> Comprobantes.deposito(form,
                            servicio.registrarDeposito(token, form.getCuenta(), form.getImporte())));
        }

        @Test
        void invalidFormShowsTheErrorsAndDoesNotCallTheService() {
            vista.revisar(MovimientoForm.deposito("", ""));

            assertEquals("Ingrese la cuenta de destino", vista.getErrores().get("cuenta"));
            assertEquals("Ingrese el importe", vista.getErrores().get("importe"));
            assertEquals(0, servicio.getLlamadas());
            assertNull(vista.getComprobante());
            assertTrue(vista.isErroresLimpiados());
        }

        @Test
        void confirmationIsAskedBeforeCallingTheService() {
            vista.setAceptar(false);

            vista.revisar(MovimientoForm.deposito("0002", "100"));

            assertEquals("¿Depositar $ 100.00 en la cuenta 0002?", vista.getPregunta());
            assertEquals(0, servicio.getLlamadas());
            assertNull(vista.getComprobante());
        }

        @Test
        void successShowsTheReceiptAndClearsTheForm() {
            vista.revisar(MovimientoForm.deposito("0002", "100"));

            assertNotNull(vista.getComprobante());
            assertEquals("Depósito", vista.getComprobante().operacion());
            assertEquals("", vista.getComprobante().cuentaOrigen());
            assertEquals("0002", vista.getComprobante().cuentaDestino());
            assertEquals(777, vista.getComprobante().idMovimiento());
            assertEquals("token-1", servicio.getTokenRecibido());
            // registrarDeposito sends only the account: the server takes the employee from the token
            assertEquals("0002", servicio.getOrigenRecibido());
            assertNull(servicio.getDestinoRecibido());
            assertNull(servicio.getClaveRecibida());
            assertTrue(vista.isLimpiada());
        }

        @Test
        void serverErrorKeepsTheFormAndShowsTheMessage() {
            servicio.fallarCon("Token inválido");

            vista.revisar(MovimientoForm.deposito("0002", "100"));

            assertEquals("Token inválido", vista.getError());
            assertNull(vista.getComprobante());
            assertFalse(vista.isLimpiada());
        }

        @Test
        void inactiveSessionDoesNotCallTheService() {
            sesion.cerrar();

            vista.revisar(MovimientoForm.deposito("0002", "100"));

            assertEquals("La sesión no está activa. Vuelva a iniciar sesión.", vista.getError());
            assertEquals(0, servicio.getLlamadas());
        }
    }

    @Nested
    class Retiro {

        @BeforeEach
        void conectarPantalla() {
            new MovimientoControlador(vista, sesion,
                    form -> "¿Retirar " + form.getImporteFormateado() + " de la cuenta " + form.getCuenta() + "?",
                    (token, form) -> Comprobantes.retiro(form,
                            servicio.registrarRetiro(token, form.getCuenta(), form.getImporte(), form.getClave())));
        }

        @Test
        void employeeKeyIsSentToTheServer() {
            vista.revisar(MovimientoForm.retiroEmpleado("0002", "s3creta", "50"));

            assertNull(servicio.getDestinoRecibido());
            assertEquals("0002", servicio.getOrigenRecibido());
            assertEquals("s3creta", servicio.getClaveRecibida());
            assertEquals("0002", vista.getComprobante().cuentaOrigen());
            assertTrue(vista.isLimpiada());
        }

        @Test
        void blankEmployeeKeyIsRejectedBeforeConfirmation() {
            vista.revisar(MovimientoForm.retiroEmpleado("0002", "  ", "50"));

            assertEquals("Ingrese la clave de la cuenta", vista.getErrores().get("clave"));
            assertEquals(0, servicio.getLlamadas());
            assertNull(vista.getPregunta());
            assertNull(vista.getComprobante());
        }

        @Test
        void clienteSendsAnEmptyKeyForTheirOwnAccount() {
            vista.revisar(MovimientoForm.retiro("00100001", "50"));

            assertNull(servicio.getDestinoRecibido());
            assertEquals("00100001", servicio.getOrigenRecibido());
            assertEquals("", servicio.getClaveRecibida());
            assertTrue(vista.isLimpiada());
        }
    }

    @Nested
    class Transferencia {

        @BeforeEach
        void conectarPantalla() {
            new MovimientoControlador(vista, sesion,
                    form -> "¿Transferir " + form.getImporteFormateado() + " de la cuenta " + form.getOrigen()
                            + " a la cuenta " + form.getDestino() + "?",
                    (token, form) -> Comprobantes.transferencia(form,
                            servicio.registrarTransferencia(token, form.getOrigen(), form.getDestino(),
                                    form.getImporte(), form.getClave())));
        }

        @Test
        void employeeKeyIsSentWithBothAccounts() {
            vista.revisar(MovimientoForm.transferenciaEmpleado("0001", "0002", "s3creta", "25"));

            assertEquals(1, servicio.getLlamadas());
            assertEquals("0001", servicio.getOrigenRecibido());
            assertEquals("0002", servicio.getDestinoRecibido());
            assertEquals("s3creta", servicio.getClaveRecibida());
            assertTrue(vista.isLimpiada());
        }

        @Test
        void blankEmployeeKeyIsRejectedBeforeConfirmation() {
            vista.revisar(MovimientoForm.transferenciaEmpleado("0001", "0002", "", "25"));

            assertEquals("Ingrese la clave de la cuenta", vista.getErrores().get("clave"));
            assertEquals(0, servicio.getLlamadas());
            assertNull(vista.getPregunta());
        }

        @Test
        void clienteSendsAnEmptyKeyFromTheirOwnAccount() {
            vista.revisar(MovimientoForm.transferencia("00100001", "0002", "25"));

            assertEquals(1, servicio.getLlamadas());
            assertEquals("00100001", servicio.getOrigenRecibido());
            assertEquals("0002", servicio.getDestinoRecibido());
            assertEquals("", servicio.getClaveRecibida());
        }
    }
}
