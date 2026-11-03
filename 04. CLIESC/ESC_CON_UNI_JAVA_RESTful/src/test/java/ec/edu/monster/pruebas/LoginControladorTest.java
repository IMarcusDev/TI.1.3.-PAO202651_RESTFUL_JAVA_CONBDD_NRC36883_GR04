package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.dobles.LoginServicioFalso;
import ec.edu.monster.dobles.LoginVistaFalsa;
import ec.edu.monster.modelo.Sesion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginControladorTest {

    private final LoginVistaFalsa vista = new LoginVistaFalsa();
    private final LoginServicioFalso servicio = new LoginServicioFalso();
    private final Sesion sesion = new Sesion();

    @BeforeEach
    void conectar() {
        new LoginControlador(vista, servicio, sesion);
    }

    @Test
    void blankCredentialsDoNotCallTheService() {
        vista.escribir(" ", "");

        vista.ingresar();

        assertEquals("Ingrese usuario y contraseña", vista.getError());
        assertEquals(0, servicio.getLlamadas());
        assertFalse(vista.isMenuAbierto());
    }

    @Test
    void successfulLoginOpensTheMenuAndStoresTheSession() {
        vista.escribir(" admin ", "1234");

        vista.ingresar();

        assertEquals("admin", servicio.getUsuarioRecibido());
        assertEquals("1234", servicio.getClaveRecibida());
        assertTrue(vista.isMenuAbierto());
        assertTrue(sesion.estaActiva());
        assertTrue(sesion.esEmpleado());
        assertEquals("Administrador", sesion.getNombre());
        assertEquals(LoginServicioFalso.SESION.token(), sesion.getToken());
    }

    @Test
    void serverErrorIsShownAndTheMenuStaysClosed() {
        servicio.fallarCon("Contraseña incorrecta");
        vista.escribir("admin", "mala");

        vista.ingresar();

        assertEquals("Contraseña incorrecta", vista.getError());
        assertEquals(1, vista.getErroresMostrados());
        assertFalse(vista.isMenuAbierto());
        assertFalse(sesion.estaActiva());
    }
}
