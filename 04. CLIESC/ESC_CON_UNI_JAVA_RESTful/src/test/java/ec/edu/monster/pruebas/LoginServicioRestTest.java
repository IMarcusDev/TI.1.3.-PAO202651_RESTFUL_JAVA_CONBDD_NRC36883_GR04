package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import ec.edu.monster.dobles.ConfiguracionDePrueba;
import ec.edu.monster.dobles.FakeRestServer;
import ec.edu.monster.modelo.Configuracion;
import ec.edu.monster.modelo.DatosSesion;
import ec.edu.monster.modelo.LoginServicioRest;
import ec.edu.monster.modelo.RutasApi;
import ec.edu.monster.modelo.ServicioException;
import ec.edu.monster.modelo.Sesiones;
import java.io.IOException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Exercises the real HTTP/JSON round trip of the login, logout and profiles against a fake REST
 * server listening on a free local port.
 */
class LoginServicioRestTest {

    private static final String TOKEN = "token-de-prueba";

    @Nested
    class WithServer {

        private FakeRestServer servidor;
        private LoginServicioRest servicio;

        @BeforeEach
        void iniciar() throws IOException {
            Configuracion configuracion = ConfiguracionDePrueba.paraPuerto(ConfiguracionDePrueba.puertoLibre());
            servidor = FakeRestServer.iniciar(configuracion);
            servicio = new LoginServicioRest(configuracion);
        }

        @AfterEach
        void detener() {
            servidor.close();
        }

        @Test
        void employeeLoginSendsUserAndKeyAndReturnsTheEmployeeSession() throws ServicioException {
            servidor.responder(RutasApi.LOGIN_EMPLEADO, 200, "{\"estado\":\"OK\",\"mensaje\":\"Ingreso\","
                    + "\"nombre\":\"Administrador\",\"tipoUsuario\":\"Empleado\",\"perfil\":\"EMPLEADO\","
                    + "\"token\":\"" + TOKEN + "\"}");

            DatosSesion datos = servicio.loginEmpleado("admin", "1234");

            assertEquals("Administrador", datos.nombre());
            assertEquals("Empleado", datos.tipoUsuario());
            assertEquals(DatosSesion.EMPLEADO, datos.perfil());
            assertEquals(TOKEN, datos.token());
            FakeRestServer.Peticion peticion = servidor.ultimaPeticion();
            assertEquals("POST", peticion.metodo());
            assertEquals("/WS_CON_UNI_JAVA_RESTFul/api/Login/loginEmpleado", peticion.ruta());
            assertTrue(peticion.contentType().startsWith("application/json"), peticion.contentType());
            assertNull(peticion.autorizacion());
            JSONObject cuerpo = new JSONObject(peticion.cuerpo());
            assertEquals("admin", cuerpo.getString("usuario"));
            assertEquals("1234", cuerpo.getString("clave"));
            assertFalse(cuerpo.has("cuenta"));
        }

        @Test
        void clientLoginSendsAccountAndKeyAndReturnsTheClientSession() throws ServicioException {
            servidor.responder(RutasApi.LOGIN_CLIENTE, 200, "{\"estado\":\"OK\",\"nombre\":\"Ana Pérez\","
                    + "\"tipoUsuario\":\"Cliente\",\"perfil\":\"CLIENTE\",\"token\":\"" + TOKEN + "\"}");

            DatosSesion datos = servicio.loginCliente("00100001", "123456");

            assertEquals("Ana Pérez", datos.nombre());
            assertEquals(DatosSesion.CLIENTE, datos.perfil());
            FakeRestServer.Peticion peticion = servidor.ultimaPeticion();
            assertEquals("/WS_CON_UNI_JAVA_RESTFul/api/Login/loginCliente", peticion.ruta());
            JSONObject cuerpo = new JSONObject(peticion.cuerpo());
            assertEquals("00100001", cuerpo.getString("cuenta"));
            assertEquals("123456", cuerpo.getString("clave"));
            assertFalse(cuerpo.has("usuario"));
        }

        @Test
        void aMissingProfileIsTakenFromTheUserType() throws ServicioException {
            servidor.responder(RutasApi.LOGIN_CLIENTE, 200, "{\"estado\":\"OK\",\"nombre\":\"Ana\","
                    + "\"tipoUsuario\":\"Cliente\",\"token\":\"" + TOKEN + "\"}");

            assertEquals(DatosSesion.CLIENTE, servicio.loginCliente("00100001", "123456").perfil());
        }

        @Test
        void wrongCredentialsThrowTheServerMessage() {
            servidor.responder(RutasApi.LOGIN_EMPLEADO, 400,
                    "{\"estado\":\"ERROR\",\"mensaje\":\"Credenciales incorrectas\"}");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.loginEmpleado("admin", "mala"));

            assertEquals("Credenciales incorrectas", error.getMessage());
        }

        /** A login sends no token, so a 401 there is a rejection, not an expired session. */
        @Test
        void anUnauthorizedLoginThrowsTheServerMessage() {
            servidor.responder(RutasApi.LOGIN_CLIENTE, 401, "{\"estado\":\"ERROR\",\"mensaje\":\"Usuario inactivo\"}");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.loginCliente("00100001", "123456"));

            assertEquals("Usuario inactivo", error.getMessage());
        }

        @Test
        void aMissingTokenIsRejected() {
            servidor.responder(RutasApi.LOGIN_EMPLEADO, 200,
                    "{\"estado\":\"OK\",\"nombre\":\"A\",\"tipoUsuario\":\"Empleado\"}");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.loginEmpleado("admin", "1234"));

            assertEquals("El servidor no devolvió un token de sesión", error.getMessage());
        }

        @Test
        void anEmptyBodyIsRejected() {
            servidor.responder(RutasApi.LOGIN_EMPLEADO, 200, "");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.loginEmpleado("admin", "1234"));

            assertEquals("El servidor no devolvió respuesta", error.getMessage());
        }

        @Test
        void anUnknownRouteReportsTheHttpStatus() {
            // Nothing registered: the fake answers 404 without body, as a server without that endpoint would
            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.loginEmpleado("admin", "1234"));

            assertEquals("El servidor respondió con un error (HTTP 404)", error.getMessage());
        }

        @Test
        void logoutSendsTheTokenToTheServer() {
            servidor.responder(RutasApi.LOGOUT, 200, "{\"estado\":\"OK\",\"mensaje\":\"Sesión cerrada\"}");

            assertDoesNotThrow(() -> servicio.logout(TOKEN));

            FakeRestServer.Peticion peticion = servidor.ultimaPeticion();
            assertEquals("/WS_CON_UNI_JAVA_RESTFul/api/Login/logout", peticion.ruta());
            assertEquals(TOKEN, new JSONObject(peticion.cuerpo()).getString("token"));
        }

        @Test
        void anUnauthorizedAnswerMeansTheSessionExpired() {
            servidor.responder(RutasApi.LOGOUT, 401, "{\"estado\":\"ERROR\",\"mensaje\":\"Token inválido\"}");

            ServicioException error = assertThrows(ServicioException.class, () -> servicio.logout(TOKEN));

            assertTrue(Sesiones.esSesionExpirada(error.getMessage()), error.getMessage());
        }
    }

    /** Nothing is listening at the configured address. */
    @Test
    void serverDownThrowsTheConnectionMessage() {
        Configuracion configuracion = ConfiguracionDePrueba.paraPuerto(ConfiguracionDePrueba.puertoLibre());
        LoginServicioRest servicio = new LoginServicioRest(configuracion);

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.loginEmpleado("admin", "1234"));

        assertEquals("No se pudo conectar con el servidor. Verifique la red e intente nuevamente.",
                error.getMessage());
    }
}
