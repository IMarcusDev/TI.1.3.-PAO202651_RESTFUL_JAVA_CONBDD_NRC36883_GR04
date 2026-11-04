package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import ec.edu.monster.dobles.ConfiguracionDePrueba;
import ec.edu.monster.dobles.FakeRestServer;
import ec.edu.monster.modelo.Configuracion;
import ec.edu.monster.modelo.HistorialCuenta;
import ec.edu.monster.modelo.MovimientoServicioRest;
import ec.edu.monster.modelo.ResultadoMovimiento;
import ec.edu.monster.modelo.RutasApi;
import ec.edu.monster.modelo.ServicioException;
import ec.edu.monster.modelo.Sesiones;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Exercises the real HTTP/JSON round trip of «Consultar movimientos» and the three
 * {@code registrar*} operations against a fake REST server listening on a free local port.
 */
class MovimientoServicioRestTest {

    private static final String TOKEN = "token-de-prueba";

    private static final String ACEPTADO = "{\"estado\":\"OK\",\"mensaje\":\"%s\",\"idMovimiento\":%d,"
            + "\"numeroMovimiento\":%d,\"nuevoSaldo\":%s}";

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
        void depositSendsTheAccountAndAmountAndReturnsTheNewBalance() throws ServicioException {
            servidor.responder(RutasApi.REGISTRAR_DEPOSITO, 200,
                    String.format(ACEPTADO, "Depósito registrado correctamente", 101, 101, "1350.00"));

            ResultadoMovimiento resultado = servicio.registrarDeposito(TOKEN, "00100001", new BigDecimal("100"));

            assertEquals(101, resultado.numeroMovimiento());
            assertEquals(0, new BigDecimal("1350.00").compareTo(resultado.nuevoSaldo()));
            assertEquals("Depósito registrado correctamente", resultado.mensaje());
            FakeRestServer.Peticion peticion = servidor.ultimaPeticion();
            assertEquals("POST", peticion.metodo());
            assertEquals("/WS_EUREKABANK_JAVA_RESTFul/api/Movimiento/registrarDeposito", peticion.ruta());
            assertNull(peticion.autorizacion());
            JSONObject cuerpo = new JSONObject(peticion.cuerpo());
            assertEquals(TOKEN, cuerpo.getString("token"));
            assertEquals("00100001", cuerpo.getString("cuenta"));
            assertEquals(0, new BigDecimal("100").compareTo(cuerpo.getBigDecimal("importe")));
            assertFalse(cuerpo.has("clave"));
        }

        @Test
        void withdrawalSendsTheAccountKey() throws ServicioException {
            servidor.responder(RutasApi.REGISTRAR_RETIRO, 200,
                    String.format(ACEPTADO, "Retiro registrado correctamente", 102, 102, "949.50"));

            ResultadoMovimiento resultado =
                    servicio.registrarRetiro(TOKEN, "00100001", new BigDecimal("50.5"), "123456");

            assertEquals(102, resultado.numeroMovimiento());
            assertEquals(0, new BigDecimal("949.50").compareTo(resultado.nuevoSaldo()));
            FakeRestServer.Peticion peticion = servidor.ultimaPeticion();
            assertEquals("/WS_EUREKABANK_JAVA_RESTFul/api/Movimiento/registrarRetiro", peticion.ruta());
            JSONObject cuerpo = new JSONObject(peticion.cuerpo());
            assertEquals(TOKEN, cuerpo.getString("token"));
            assertEquals("00100001", cuerpo.getString("cuenta"));
            assertEquals("123456", cuerpo.getString("clave"));
            assertEquals(0, new BigDecimal("50.5").compareTo(cuerpo.getBigDecimal("importe")));
            // A withdrawal has no destination account
            assertFalse(cuerpo.has("cuentaDestino"));
        }

        @Test
        void aClientWithdrawalSendsAnEmptyKey() throws ServicioException {
            servidor.responder(RutasApi.REGISTRAR_RETIRO, 200,
                    String.format(ACEPTADO, "Retiro registrado correctamente", 102, 102, "949.50"));

            servicio.registrarRetiro(TOKEN, "00100001", new BigDecimal("50.5"), null);

            assertEquals("", new JSONObject(servidor.ultimaPeticion().cuerpo()).getString("clave"));
        }

        @Test
        void transferSendsBothAccountsAndTheSourceKey() throws ServicioException {
            servidor.responder(RutasApi.REGISTRAR_TRANSFERENCIA, 200,
                    String.format(ACEPTADO, "Transferencia registrada correctamente", 103, 103, "975.00"));

            ResultadoMovimiento resultado = servicio.registrarTransferencia(TOKEN, "00100001", "00100002",
                    new BigDecimal("25"), "123456");

            assertEquals(103, resultado.numeroMovimiento());
            assertEquals("Transferencia registrada correctamente", resultado.mensaje());
            FakeRestServer.Peticion peticion = servidor.ultimaPeticion();
            assertEquals("/WS_EUREKABANK_JAVA_RESTFul/api/Movimiento/registrarTransferencia", peticion.ruta());
            JSONObject cuerpo = new JSONObject(peticion.cuerpo());
            assertEquals(TOKEN, cuerpo.getString("token"));
            assertEquals("00100001", cuerpo.getString("cuentaOrigen"));
            assertEquals("00100002", cuerpo.getString("cuentaDestino"));
            assertEquals("123456", cuerpo.getString("clave"));
            assertEquals(0, new BigDecimal("25").compareTo(cuerpo.getBigDecimal("importe")));
        }

        @Test
        void consultaReturnsTheAccountAndItsMovementsFormatted() throws ServicioException {
            servidor.responder(RutasApi.CONSULTAR_MOVIMIENTOS, 200, "{\"estado\":\"OK\","
                    + "\"mensaje\":\"Consulta de movimientos exitosa\","
                    + "\"cuenta\":{\"numeroCuenta\":\"00100001\",\"titular\":\"Ana Pérez\",\"moneda\":\"USD\","
                    + "\"saldo\":1250.00,\"estadoCuenta\":\"ACTIVO\"},"
                    + "\"movimientos\":["
                    + "{\"numeroMovimiento\":6,\"fecha\":\"2026-11-03T09:15:00\",\"tipoMovimiento\":\"Depósito\","
                    + "\"accion\":\"INGRESO\",\"importe\":100.00},"
                    + "{\"numeroMovimiento\":7,\"fecha\":\"2026-11-03T10:00:00\",\"tipoMovimiento\":\"Transferencia\","
                    + "\"accion\":\"SALIDA\",\"importe\":25.50,\"cuentaReferencia\":\"00100002\"}]}");

            HistorialCuenta historial = servicio.consultarMovimientos(TOKEN, "00100001");

            assertEquals("00100001", historial.cuenta().numero());
            assertEquals("Ana Pérez", historial.cuenta().titular());
            assertEquals("USD", historial.cuenta().moneda());
            assertEquals("$ 1,250.00", historial.cuenta().saldoFormateado());
            assertEquals("ACTIVO", historial.cuenta().estado());
            assertEquals(2, historial.movimientos().size());
            HistorialCuenta.MovimientoConsultado deposito = historial.movimientos().get(0);
            assertEquals("000006", deposito.numero());
            assertEquals("03/11/2026 09:15", deposito.fecha());
            assertEquals("Depósito", deposito.tipo());
            assertTrue(deposito.esIngreso());
            assertEquals("$ 100.00", deposito.importeFormateado());
            assertEquals("-", deposito.referencia());
            assertEquals("00100002", historial.movimientos().get(1).referencia());
            FakeRestServer.Peticion peticion = servidor.ultimaPeticion();
            assertEquals("/WS_EUREKABANK_JAVA_RESTFul/api/Movimiento/consultarMovimientos", peticion.ruta());
            JSONObject cuerpo = new JSONObject(peticion.cuerpo());
            assertEquals(TOKEN, cuerpo.getString("token"));
            assertEquals("00100001", cuerpo.getString("cuenta"));
        }

        @Test
        void anAccountWithoutMovementsReturnsAnEmptyList() throws ServicioException {
            servidor.responder(RutasApi.CONSULTAR_MOVIMIENTOS, 200, "{\"estado\":\"OK\","
                    + "\"cuenta\":{\"numeroCuenta\":\"00100003\",\"titular\":\"Luis\",\"moneda\":\"USD\","
                    + "\"saldo\":0,\"estadoCuenta\":\"ACTIVO\"},\"movimientos\":[]}");

            assertTrue(servicio.consultarMovimientos(TOKEN, "00100003").sinMovimientos());
        }

        @Test
        void anUnknownAccountThrowsTheServerMessage() {
            servidor.responder(RutasApi.CONSULTAR_MOVIMIENTOS, 400,
                    "{\"estado\":\"ERROR\",\"mensaje\":\"La cuenta no existe\"}");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.consultarMovimientos(TOKEN, "00109999"));

            assertEquals("La cuenta no existe", error.getMessage());
        }

        @Test
        void anUnauthorizedAnswerMeansTheSessionExpired() {
            // Any 401 counts, whatever text the server puts in the body
            servidor.responder(RutasApi.REGISTRAR_DEPOSITO, 401, "{\"estado\":\"ERROR\",\"mensaje\":\"Token inválido\"}");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.registrarDeposito("otro-token", "00100001", new BigDecimal("100")));

            assertEquals(Sesiones.TOKEN_INVALIDO, error.getMessage());
            assertTrue(Sesiones.esSesionExpirada(error.getMessage()));
        }

        @Test
        void anUnauthorizedAnswerWithoutBodyAlsoMeansTheSessionExpired() {
            servidor.responder(RutasApi.CONSULTAR_MOVIMIENTOS, 401, "");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.consultarMovimientos("otro-token", "00100001"));

            assertTrue(Sesiones.esSesionExpirada(error.getMessage()), error.getMessage());
        }

        @Test
        void aBusinessErrorThrowsTheServerMessage() {
            servidor.responder(RutasApi.REGISTRAR_RETIRO, 400,
                    "{\"estado\":\"ERROR\",\"mensaje\":\"Saldo insuficiente\"}");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.registrarRetiro(TOKEN, "00100001", new BigDecimal("999"), "123456"));

            assertEquals("Saldo insuficiente", error.getMessage());
        }

        /** The request reached the server, so the movement may exist: never say "could not connect". */
        @Test
        void aSlowServerReportsThatTheOutcomeIsUnknown() throws IOException {
            Configuracion configuracion = ConfiguracionDePrueba.paraPuerto(ConfiguracionDePrueba.puertoLibre());
            try (FakeRestServer lento = FakeRestServer.iniciar(configuracion)) {
                lento.responderTarde(RutasApi.REGISTRAR_DEPOSITO, 1_000,
                        String.format(ACEPTADO, "Depósito registrado correctamente", 101, 101, "1350.00"));
                MovimientoServicioRest impaciente = new MovimientoServicioRest(configuracion, Duration.ofMillis(200));

                ServicioException error = assertThrows(ServicioException.class,
                        () -> impaciente.registrarDeposito(TOKEN, "00100001", new BigDecimal("100")));

                assertEquals("El servidor no respondió a tiempo. La operación pudo haberse registrado: "
                        + "verifique el saldo antes de volver a intentarlo", error.getMessage());
            }
        }

        @Test
        void anAcceptedMovementWithoutNumberIsRejected() {
            servidor.responder(RutasApi.REGISTRAR_DEPOSITO, 200,
                    "{\"estado\":\"OK\",\"mensaje\":\"Depósito registrado correctamente\"}");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.registrarDeposito(TOKEN, "00100001", new BigDecimal("1")));

            assertEquals("El servidor no devolvió el número de movimiento", error.getMessage());
        }

        @Test
        void anErrorWithoutMessageUsesTheDefault() {
            servidor.responder(RutasApi.REGISTRAR_DEPOSITO, 200, "{\"estado\":\"ERROR\"}");

            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.registrarDeposito(TOKEN, "00100001", new BigDecimal("1")));

            assertEquals("Error del servidor", error.getMessage());
        }
    }

    /** Nothing is listening at the configured address. */
    @Test
    void serverDownThrowsTheConnectionMessage() {
        Configuracion configuracion = ConfiguracionDePrueba.paraPuerto(ConfiguracionDePrueba.puertoLibre());
        MovimientoServicioRest servicio = new MovimientoServicioRest(configuracion);

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.registrarDeposito(TOKEN, "00100001", new BigDecimal("100")));

        assertEquals("No se pudo conectar con el servidor. Verifique la red e intente nuevamente.",
                error.getMessage());
    }
}
