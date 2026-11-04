package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.io.IOException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Exercises the real HTTP/JSON round trip of the login against a fake REST server
 * listening on a free local port.
 */
class LoginServicioRestTest {

    private static final String TOKEN = "token-de-prueba";

    @Nested
    class WithServer {

        private Configuracion configuracion;
        private FakeRestServer servidor;
        private LoginServicioRest servicio;

        @BeforeEach
        void iniciar() throws IOException {
            configuracion = ConfiguracionDePrueba.paraPuerto(ConfiguracionDePrueba.puertoLibre());
            servidor = FakeRestServer.iniciar(configuracion);
            servicio = new LoginServicioRest(configuracion);
        }

        @AfterEach
        void detener() {
            servidor.close();
        }

        @Test
        void validCredentialsReturnTheSessionData() throws ServicioException {
            servidor.responder(RutasApi.LOGIN, 200, "{\"estado\":\"OK\",\"mensaje\":\"Bienvenido\","
                    + "\"nombre\":\"Administrador\",\"tipoUsuario\":\"Empleado\",\"token\":\"" + TOKEN + "\"}");

            DatosSesion datos = servicio.iniciarSesion("admin", "1234");

            assertEquals("Administrador", datos.nombre());
            assertEquals("Empleado", datos.tipoUsuario());
            assertEquals(TOKEN, datos.token());
        }

        @Test
        void sendsTheCredentialsAsJsonByPost() throws ServicioException {
            servidor.responder(RutasApi.LOGIN, 200, "{\"estado\":\"OK\",\"nombre\":\"A\","
                    + "\"tipoUsuario\":\"Empleado\",\"token\":\"" + TOKEN + "\"}");

            servicio.iniciarSesion("admin", "1234");

            FakeRestServer.Peticion peticion = servidor.ultimaPeticion();
            assertEquals("POST", peticion.metodo());
            assertEquals("/WS_EUREKABANK_JAVA_RESTFul/api/Login", peticion.ruta());
            assertTrue(peticion.contentType().startsWith("application/json"), peticion.contentType());
            assertNull(peticion.autorizacion());
            JSONObject cuerpo = new JSONObject(peticion.cuerpo());
            assertEquals("admin", cuerpo.getString("username"));
            assertEquals("1234", cuerpo.getString("password"));
        }

        @Test
        void wrongCredentialsThrowTheServerMessage() {
            servidor.responder(RutasApi.LOGIN, 200, "{\"estado\":\"ERROR\",\"mensaje\":\"Contraseña incorrecta\"}");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.iniciarSesion("admin", "mala"));

            assertEquals("Contraseña incorrecta", error.getMessage());
        }

        @Test
        void anErrorStatusWithMessageThrowsTheServerMessage() {
            servidor.responder(RutasApi.LOGIN, 401, "{\"estado\":\"ERROR\",\"mensaje\":\"Usuario inactivo\"}");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.iniciarSesion("admin", "1234"));

            assertEquals("Usuario inactivo", error.getMessage());
        }

        @Test
        void aMissingTokenIsRejected() {
            servidor.responder(RutasApi.LOGIN, 200, "{\"estado\":\"OK\",\"nombre\":\"A\",\"tipoUsuario\":\"Empleado\"}");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.iniciarSesion("admin", "1234"));

            assertEquals("El servidor no devolvió un token de sesión", error.getMessage());
        }

        @Test
        void anEmptyBodyIsRejected() {
            servidor.responder(RutasApi.LOGIN, 200, "");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.iniciarSesion("admin", "1234"));

            assertEquals("El servidor no devolvió respuesta", error.getMessage());
        }

        @Test
        void anUnknownRouteReportsTheHttpStatus() {
            // Nothing registered: the fake answers 404 without body, as a server without that endpoint would
            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.iniciarSesion("admin", "1234"));

            assertEquals("El servidor respondió con un error (HTTP 404)", error.getMessage());
        }
    }

    /** Nothing is listening at the configured address. */
    @Test
    void serverDownThrowsTheConnectionMessage() {
        Configuracion configuracion = ConfiguracionDePrueba.paraPuerto(ConfiguracionDePrueba.puertoLibre());
        LoginServicioRest servicio = new LoginServicioRest(configuracion);

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.iniciarSesion("admin", "1234"));

        assertEquals("No se pudo conectar con el servidor: " + configuracion.getEndpoint(RutasApi.LOGIN),
                error.getMessage());
    }
}
