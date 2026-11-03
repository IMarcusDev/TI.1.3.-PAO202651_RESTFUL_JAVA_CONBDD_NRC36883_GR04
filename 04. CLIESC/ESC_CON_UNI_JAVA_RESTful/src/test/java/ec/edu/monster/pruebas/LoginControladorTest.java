package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelo.ConversorServicio;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.vista.LoginVista;
import ec.edu.monster.modelo.dto.CatalogResponseDTO;
import ec.edu.monster.modelo.http.ConversorApi;
import ec.edu.monster.modelo.dto.ConversorResponseDTO;
import ec.edu.monster.modelo.enums.EstadosEnum;
import ec.edu.monster.modelo.dto.LoginResponseDTO;
import ec.edu.monster.modelo.http.LoginApi;
import org.junit.jupiter.api.Test;

class LoginControladorTest {

    private final Sesion sesion = new Sesion();
    private final VistaLoginFalsa vista = new VistaLoginFalsa();
    private int llamadasAlServidor;

    @Test
    void showsErrorAndSkipsServerWhenCredentialsAreBlank() {
        new LoginControlador(vista, servicio((usuario, clave) -> loginOk()));
        vista.usuario = "  ";
        vista.clave = "";

        vista.presionarIngresar();

        assertEquals("Ingrese usuario y contraseña", vista.error);
        assertFalse(vista.menuAbierto);
        assertEquals(0, llamadasAlServidor);
    }

    @Test
    void opensMenuWhenLoginSucceeds() {
        new LoginControlador(vista, servicio((usuario, clave) -> loginOk()));
        vista.usuario = "user";
        vista.clave = "1234";

        vista.presionarIngresar();

        assertTrue(vista.menuAbierto);
        assertNull(vista.error);
        assertEquals("user", sesion.getUsuario());
    }

    @Test
    void showsServerMessageWhenLoginIsRejected() {
        new LoginControlador(vista, servicio((usuario, clave) -> loginError("Credenciales incorrectas")));
        vista.usuario = "user";
        vista.clave = "mala";

        vista.presionarIngresar();

        assertEquals("Credenciales incorrectas", vista.error);
        assertFalse(vista.menuAbierto);
    }

    private ConversorServicio servicio(LoginApi login) {
        LoginApi contador = (usuario, clave) -> {
            llamadasAlServidor++;
            return login.login(usuario, clave);
        };
        return new ConversorServicio(contador, new ConversorSinUso(), sesion);
    }

    private static LoginResponseDTO loginOk() {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setEstado(EstadosEnum.OK);
        dto.setToken("tok-1");
        return dto;
    }

    private static LoginResponseDTO loginError(String mensaje) {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setEstado(EstadosEnum.ERROR);
        dto.setMensaje(mensaje);
        return dto;
    }

    private static final class VistaLoginFalsa implements LoginVista {

        private String usuario = "";
        private String clave = "";
        private String error;
        private boolean menuAbierto;
        private Runnable accionIngresar;

        void presionarIngresar() {
            accionIngresar.run();
        }

        @Override
        public String getUsuario() {
            return usuario;
        }

        @Override
        public String getClave() {
            return clave;
        }

        @Override
        public void mostrarError(String mensaje) {
            error = mensaje;
        }

        @Override
        public void abrirMenu() {
            menuAbierto = true;
        }

        @Override
        public void alIngresar(Runnable accion) {
            accionIngresar = accion;
        }
    }

    private static final class ConversorSinUso implements ConversorApi {

        @Override
        public ConversorResponseDTO convert(String token, String type, double value, String from, String to) {
            throw new AssertionError("Login must not call the converter");
        }

        @Override
        public CatalogResponseDTO catalog(String token, String type) {
            throw new AssertionError("Login must not call the catalog");
        }
    }
}
