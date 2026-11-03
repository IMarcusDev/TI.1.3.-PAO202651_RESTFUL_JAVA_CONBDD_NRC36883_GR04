package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelo.Sesion;
import org.junit.jupiter.api.Test;

class SesionTest {
     //Sesión empieza como inactiva antes de ingresar las credenciales.
    @Test
    void startsInactive() {
        Sesion sesion = new Sesion();

        assertFalse(sesion.estaActiva());
        assertNull(sesion.getToken());
    }
    // Mantener sesion y tokens despues de iniciar sesión
    @Test
    void keepsUserAndTokenAfterStart() {
        Sesion sesion = new Sesion();

        sesion.iniciar("user", "abc123");

        assertTrue(sesion.estaActiva());
        assertEquals("user", sesion.getUsuario());
        assertEquals("abc123", sesion.getToken());
    }
    
     //Limpiar estado y cierra sesión 
    @Test
    void clearsStateOnClose() {
        Sesion sesion = new Sesion();
        sesion.iniciar("user", "abc123");

        sesion.cerrar();

        assertFalse(sesion.estaActiva());
        assertNull(sesion.getUsuario());
        assertNull(sesion.getToken());
    }
}
