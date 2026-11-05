package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.controlador.MovimientosControlador;
import ec.edu.monster.dobles.MovimientosVistaFalsa;
import ec.edu.monster.dobles.MovimientoServicioFalso;
import ec.edu.monster.modelo.DatosSesion;
import ec.edu.monster.modelo.HistorialCuenta;
import ec.edu.monster.modelo.Sesion;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MovimientosControladorTest {

    private final Sesion sesion = new Sesion();
    private final MovimientosVistaFalsa vista = new MovimientosVistaFalsa();
    private final MovimientoServicioFalso servicio = new MovimientoServicioFalso();

    @BeforeEach
    void conectar() {
        sesion.iniciar("admin", new DatosSesion("Administrador", "Empleado", DatosSesion.EMPLEADO, "token-1"));
        new MovimientosControlador(vista, sesion, servicio);
    }

    @Test
    void blankAccountShowsTheErrorAndDoesNotCallTheService() {
        vista.escribirCuenta("  ");

        vista.consultar();

        assertEquals("Ingrese el número de cuenta", vista.getError());
        assertEquals(0, servicio.getLlamadas());
        assertNull(vista.getHistorial());
        assertTrue(vista.isErroresLimpiados());
    }

    @Test
    void employeeQueriesAnyAccountAndShowsTheHistory() {
        vista.escribirCuenta(" 00100001 ");

        vista.consultar();

        assertEquals(1, servicio.getLlamadas());
        assertEquals("token-1", servicio.getTokenRecibido());
        assertEquals("00100001", servicio.getOrigenRecibido());
        assertEquals("00100001", vista.getHistorial().cuenta().numero());
        assertNull(vista.getError());
    }

    @Test
    void clienteQueriesTheAccountFixedByTheView() {
        sesion.iniciar("00100001", new DatosSesion("Ana Torres", "Cliente", DatosSesion.CLIENTE, "token-cliente"));
        vista.escribirCuenta("00100001");

        vista.consultar();

        assertEquals("token-cliente", servicio.getTokenRecibido());
        assertEquals("00100001", servicio.getOrigenRecibido());
    }

    @Test
    void accountWithoutMovementsIsShownAsAValidAnswer() {
        servicio.responderCon(new HistorialCuenta(
                new HistorialCuenta.CuentaConsultada("00100004", "Luis", "USD", BigDecimal.ZERO, "ACTIVO"),
                List.of()));
        vista.escribirCuenta("00100004");

        vista.consultar();

        assertTrue(vista.getHistorial().sinMovimientos());
        assertEquals("Luis", vista.getHistorial().cuenta().titular());
        assertNull(vista.getError());
    }

    @Test
    void serverErrorIsShownAsIs() {
        servicio.fallarCon("La cuenta no existe");
        vista.escribirCuenta("99999999");

        vista.consultar();

        assertEquals("La cuenta no existe", vista.getError());
        assertNull(vista.getHistorial());
    }

    @Test
    void inactiveSessionDoesNotCallTheService() {
        sesion.cerrar();
        vista.escribirCuenta("00100001");

        vista.consultar();

        assertEquals("La sesión no está activa. Vuelva a iniciar sesión.", vista.getError());
        assertEquals(0, servicio.getLlamadas());
    }
}
