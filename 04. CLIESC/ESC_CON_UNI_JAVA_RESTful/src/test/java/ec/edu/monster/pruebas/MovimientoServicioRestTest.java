package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ec.edu.monster.dobles.ConfiguracionDePrueba;
import ec.edu.monster.dobles.FakeRestServer;
import ec.edu.monster.modelo.Configuracion;
import ec.edu.monster.modelo.MovimientoServicioRest;
import ec.edu.monster.modelo.ResultadoMovimiento;
import ec.edu.monster.modelo.RutasApi;
import ec.edu.monster.modelo.ServicioException;
import java.io.IOException;
import java.math.BigDecimal;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Exercises the real HTTP/JSON round trip of the three movements against a fake
 * REST server listening on a free local port.
 */
class MovimientoServicioRestTest {

    private static final String TOKEN = "token-de-prueba";

    @Nested
    class WithServer {

        private FakeRestServer servidor;
        private MovimientoServicioRest servicio;

        @BeforeEach
        void iniciar() throws IOException {
            Configuracion configuracion = ConfiguracionDePrueba.paraPuerto(ConfiguracionDePrueba.puertoLibre());
            servidor = FakeRestServer.iniciar(configuracion);
            servicio = new MovimientoServicioRest(configuracion);
        }

        @AfterEach
        void detener() {
            servidor.close();
        }

        @Test
        void depositReturnsTheGeneratedIdAndSendsTheMovement() throws ServicioException {
            servidor.responder(RutasApi.DEPOSITO, 200,
                    "{\"estado\":\"OK\",\"mensaje\":\"Depósito registrado\",\"idMovimiento\":101}");

            ResultadoMovimiento resultado = servicio.deposito(TOKEN, "0001", "0004", new BigDecimal("100"));

            assertEquals(101, resultado.idMovimiento());
            assertEquals("Depósito registrado", resultado.mensaje());
            FakeRestServer.Peticion peticion = servidor.ultimaPeticion();
            assertEquals("POST", peticion.metodo());
            assertEquals("/WS_CON_UNI_JAVA_RESTFUL/api/movimientos/deposito", peticion.ruta());
            assertEquals(TOKEN, peticion.autorizacion());
            JSONObject cuerpo = new JSONObject(peticion.cuerpo());
            assertEquals("0001", cuerpo.getString("cuentaOrigen"));
            assertEquals("0004", cuerpo.getString("cuentaDestino"));
            assertEquals(0, new BigDecimal("100").compareTo(cuerpo.getBigDecimal("valor")));
        }

        @Test
        void withdrawalReturnsTheGeneratedIdAndSendsTheMovement() throws ServicioException {
            servidor.responder(RutasApi.RETIRO, 200,
                    "{\"estado\":\"OK\",\"mensaje\":\"Retiro registrado\",\"idMovimiento\":102}");

            ResultadoMovimiento resultado = servicio.retiro(TOKEN, "0004", "", new BigDecimal("50.5"));

            assertEquals(102, resultado.idMovimiento());
            assertEquals("Retiro registrado", resultado.mensaje());
            FakeRestServer.Peticion peticion = servidor.ultimaPeticion();
            assertEquals("/WS_CON_UNI_JAVA_RESTFUL/api/movimientos/retiro", peticion.ruta());
            JSONObject cuerpo = new JSONObject(peticion.cuerpo());
            assertEquals("0004", cuerpo.getString("cuentaOrigen"));
            assertEquals("", cuerpo.getString("cuentaDestino"));
            assertEquals(0, new BigDecimal("50.5").compareTo(cuerpo.getBigDecimal("valor")));
        }

        @Test
        void transferReturnsTheGeneratedIdAndSendsTheMovement() throws ServicioException {
            servidor.responder(RutasApi.TRANSFERENCIA, 200,
                    "{\"estado\":\"OK\",\"mensaje\":\"Transferencia registrada\",\"idMovimiento\":103}");

            ResultadoMovimiento resultado = servicio.transferencia(TOKEN, "0001", "0002", new BigDecimal("25"));

            assertEquals(103, resultado.idMovimiento());
            assertEquals("Transferencia registrada", resultado.mensaje());
            FakeRestServer.Peticion peticion = servidor.ultimaPeticion();
            assertEquals("/WS_CON_UNI_JAVA_RESTFUL/api/movimientos/transferencia", peticion.ruta());
            assertEquals(TOKEN, peticion.autorizacion());
        }

        @Test
        void invalidTokenThrowsTheServerMessage() {
            servidor.responder(RutasApi.DEPOSITO, 401, "{\"estado\":\"ERROR\",\"mensaje\":\"Token inválido\"}");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.deposito("otro-token", "0001", "0004", new BigDecimal("100")));

            assertEquals("Token inválido", error.getMessage());
        }

        @Test
        void aBusinessErrorThrowsTheServerMessage() {
            servidor.responder(RutasApi.RETIRO, 200,
                    "{\"estado\":\"ERROR\",\"mensaje\":\"Saldo insuficiente\"}");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.retiro(TOKEN, "0004", "", new BigDecimal("999")));

            assertEquals("Saldo insuficiente", error.getMessage());
        }

        @Test
        void anErrorWithoutMessageUsesTheDefault() {
            servidor.responder(RutasApi.DEPOSITO, 200, "{\"estado\":\"ERROR\"}");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.deposito(TOKEN, "0001", "0004", new BigDecimal("1")));

            assertEquals("Error del servidor", error.getMessage());
        }
    }

    /** Nothing is listening at the configured address. */
    @Test
    void serverDownThrowsTheConnectionMessage() {
        Configuracion configuracion = ConfiguracionDePrueba.paraPuerto(ConfiguracionDePrueba.puertoLibre());
        MovimientoServicioRest servicio = new MovimientoServicioRest(configuracion);

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.deposito(TOKEN, "0001", "0004", new BigDecimal("100")));

        assertEquals("No se pudo conectar con el servidor: " + configuracion.getEndpoint(RutasApi.DEPOSITO),
                error.getMessage());
    }
}
