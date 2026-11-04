package ec.edu.monster.modelos.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import ec.edu.monster.fakes.FakeRestServer;
import ec.edu.monster.modelos.dto.EstadosEnum;
import ec.edu.monster.modelos.dto.MovimientoResultado;
import java.math.BigDecimal;
import java.time.Duration;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Calls the real MovimientoService against a fake RESTful server. */
class MovimientoServiceTest {
  private static final String OK = "{\"estado\":\"OK\",\"mensaje\":\"Registrado correctamente\","
      + "\"numeroMovimiento\":42,\"nuevoSaldo\":1250.50}";

  private FakeRestServer server;
  private MovimientoService service;

  @BeforeEach
  void setUp() throws Exception {
    server = FakeRestServer.start();
    service = new MovimientoService(server.baseUrl(), Duration.ofMillis(500));
  }

  @AfterEach
  void tearDown() {
    server.close();
  }

  @Test
  void depositoPostsTokenAccountAndAmount() {
    server.answer(200, OK);

    MovimientoResultado resultado = service.deposito("t1", "00100001", new BigDecimal("150.50"));

    assertEquals("POST " + FakeRestServer.CONTEXT + "/api/Movimiento/registrarDeposito", server.lastPath());
    JSONObject body = server.lastBody();
    assertEquals("t1", body.getString("token"));
    assertEquals("00100001", body.getString("cuenta"));
    assertEquals(0, new BigDecimal("150.50").compareTo(body.getBigDecimal("importe")));
    assertFalse(body.has("clave"));
    assertEquals(EstadosEnum.OK, resultado.estado());
    assertEquals("Registrado correctamente", resultado.mensaje());
    assertEquals(42, resultado.numeroMovimiento());
    assertEquals(0, new BigDecimal("1250.50").compareTo(resultado.nuevoSaldo()));
  }

  @Test
  void retiroPostsTheAccountKey() {
    server.answer(200, OK);

    MovimientoResultado resultado = service.retiro("t1", "00100001", new BigDecimal("20"), "123456");

    assertEquals("POST " + FakeRestServer.CONTEXT + "/api/Movimiento/registrarRetiro", server.lastPath());
    JSONObject body = server.lastBody();
    assertEquals("t1", body.getString("token"));
    assertEquals("00100001", body.getString("cuenta"));
    assertEquals(0, new BigDecimal("20").compareTo(body.getBigDecimal("importe")));
    assertEquals("123456", body.getString("clave"));
    assertEquals(EstadosEnum.OK, resultado.estado());
  }

  @Test
  void transferenciaPostsBothAccountsAndTheKey() {
    server.answer(200, OK);

    MovimientoResultado resultado =
        service.transferencia("t1", "00100001", "00200002", new BigDecimal("75.25"), "123456");

    assertEquals("POST " + FakeRestServer.CONTEXT + "/api/Movimiento/registrarTransferencia",
        server.lastPath());
    JSONObject body = server.lastBody();
    assertEquals("t1", body.getString("token"));
    assertEquals("00100001", body.getString("cuentaOrigen"));
    assertEquals("00200002", body.getString("cuentaDestino"));
    assertEquals(0, new BigDecimal("75.25").compareTo(body.getBigDecimal("importe")));
    assertEquals("123456", body.getString("clave"));
    assertEquals(EstadosEnum.OK, resultado.estado());
  }

  @Test
  void rejectedOperationReturnsServerMessage() {
    server.answer(400, "{\"estado\":\"ERROR\",\"mensaje\":\"Saldo insuficiente\",\"numeroMovimiento\":0}");

    MovimientoResultado resultado = service.retiro("t1", "00100001", BigDecimal.TEN, "123456");

    assertEquals(EstadosEnum.ERROR, resultado.estado());
    assertEquals("Saldo insuficiente", resultado.mensaje());
  }

  @Test
  void emptyServerMessageGetsADefault() {
    server.answer(400, "{\"estado\":\"ERROR\",\"mensaje\":\"\"}");

    MovimientoResultado resultado = service.deposito("t1", "00100001", BigDecimal.TEN);

    assertEquals(EstadosEnum.ERROR, resultado.estado());
    assertEquals("Error del servidor", resultado.mensaje());
  }

  @Test
  void unauthorizedMeansExpiredSession() {
    server.answer(401, "{\"estado\":\"ERROR\",\"mensaje\":\"Token inválido o sesión expirada\"}");

    MovimientoResultado resultado = service.deposito("t1", "00100001", BigDecimal.TEN);

    assertEquals(EstadosEnum.SESION_EXPIRADA, resultado.estado());
    assertEquals("Su sesión expiró o no es válida. Inicie sesión nuevamente.", resultado.mensaje());
  }

  @Test
  void okWithoutMovementNumberIsAnError() {
    server.answer(200, "{\"estado\":\"OK\",\"mensaje\":\"Registrado\"}");

    MovimientoResultado resultado = service.deposito("t1", "00100001", BigDecimal.TEN);

    assertEquals(EstadosEnum.ERROR, resultado.estado());
    assertEquals("El servidor no devolvió el número de movimiento", resultado.mensaje());
  }

  @Test
  void answerTooLateWarnsThatTheOperationMayExist() {
    server.answer(200, OK);
    server.delay(2000);

    MovimientoResultado resultado = service.deposito("t1", "00100001", BigDecimal.TEN);

    assertEquals(EstadosEnum.ERROR, resultado.estado());
    assertEquals("El servidor no respondió a tiempo. La operación pudo haberse registrado: "
        + "verifique el saldo antes de volver a intentarlo", resultado.mensaje());
  }

  @Test
  void serverDownReturnsConnectionErrorWithEndpoint() throws Exception {
    String base = FakeRestServer.unreachableBaseUrl();

    MovimientoResultado resultado = new MovimientoService(base, Duration.ofMillis(500))
        .deposito("t1", "00100001", BigDecimal.TEN);

    assertEquals(EstadosEnum.ERROR, resultado.estado());
    assertEquals("No se pudo conectar con el servidor: " + base + "/api/Movimiento/registrarDeposito",
        resultado.mensaje());
  }
}
