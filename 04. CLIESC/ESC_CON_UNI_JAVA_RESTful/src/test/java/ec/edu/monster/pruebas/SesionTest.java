package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelo.DatosSesion;
import ec.edu.monster.modelo.Sesion;
import org.junit.jupiter.api.Test;

class SesionTest {

    private static final DatosSesion ADMIN = new DatosSesion("Administrador", "Empleado", "token-1");

    @Test
    void startsAndClosesTheSession() {
        Sesion sesion = new Sesion();

        assertFalse(sesion.estaActiva());
        sesion.iniciar("admin", ADMIN);

        assertTrue(sesion.estaActiva());
        assertEquals("admin", sesion.getUsuario());
        assertEquals("Administrador", sesion.getNombre());
        assertEquals("Empleado", sesion.getTipoUsuario());
        assertEquals("token-1", sesion.getToken());

        sesion.cerrar();

        assertFalse(sesion.estaActiva());
        assertNull(sesion.getToken());
        assertNull(sesion.getTipoUsuario());
    }

    @Test
    void onlyTheEmployeeRoleCanOperate() {
        Sesion empleado = new Sesion();
        empleado.iniciar("admin", ADMIN);
        assertTrue(empleado.esEmpleado());

        Sesion cliente = new Sesion();
        cliente.iniciar("cliente", new DatosSesion("Cliente", "Cliente", "token-2"));
        assertFalse(cliente.esEmpleado());
    }

    @Test
    void blankTokenMeansTheSessionIsNotActive() {
        Sesion sesion = new Sesion();
        sesion.iniciar("admin", new DatosSesion("Administrador", "Empleado", " "));

        assertFalse(sesion.estaActiva());
    }

    @Test
    void emptyNameFallsBackToTheUser() {
        Sesion sesion = new Sesion();
        sesion.iniciar("admin", new DatosSesion("", "Empleado", "token-1"));

        assertEquals("admin", sesion.getNombre());
    }
}
