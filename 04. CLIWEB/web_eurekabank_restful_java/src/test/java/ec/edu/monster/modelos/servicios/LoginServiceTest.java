package ec.edu.monster.modelos.servicios;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ec.edu.monster.fakes.FakeRestServer;
import ec.edu.monster.modelos.dto.EstadosEnum;
import ec.edu.monster.modelos.dto.LoginResultado;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Calls the real LoginService against a fake RESTful server. */
class LoginServiceTest {
  private FakeRestServer server;
  private LoginService service;

  @BeforeEach
  void setUp() throws Exception {
    server = FakeRestServer.start();
    service = new LoginService(server.baseUrl());
  }

  @AfterEach
  void tearDown() {
    server.close();
  }

  @Test
  void loginOkPostsCredentialsAndReturnsSessionData() {
    server.answer(200, "{\"estado\":\"OK\",\"mensaje\":\"Ingreso de sesión exitoso\",\"token\":\"t1\","
        + "\"nombre\":\"Ana Pérez\",\"tipoUsuario\":\"Empleado\",\"perfil\":\"EMPLEADO\"}");

    LoginResultado resultado = service.login("ana", "secreta");

    assertEquals("POST " + FakeRestServer.CONTEXT + "/api/Login/loginEmpleado", server.lastPath());
    JSONObject body = server.lastBody();
    assertEquals("ana", body.getString("usuario"));
    assertEquals("secreta", body.getString("clave"));
    assertEquals(EstadosEnum.OK, resultado.estado());
    assertEquals("t1", resultado.token());
    assertEquals("Ana Pérez", resultado.nombre());
    assertEquals("Empleado", resultado.tipoUsuario());
  }

  @Test
  void rejectedCredentialsReturnServerMessage() {
    server.answer(400, "{\"estado\":\"ERROR\",\"mensaje\":\"Usuario o clave incorrectos\",\"token\":\"\"}");

    LoginResultado resultado = service.login("ana", "mala");

    assertEquals(EstadosEnum.ERROR, resultado.estado());
    assertEquals("Usuario o clave incorrectos", resultado.mensaje());
  }

  @Test
  void okWithoutTokenIsAnError() {
    server.answer(200, "{\"estado\":\"OK\",\"mensaje\":\"Ingreso\",\"token\":\"\"}");

    LoginResultado resultado = service.login("ana", "secreta");

    assertEquals(EstadosEnum.ERROR, resultado.estado());
    assertEquals("El servidor no devolvió un token de sesión", resultado.mensaje());
  }

  @Test
  void answerThatIsNotJsonNamesTheHttpStatus() {
    server.answer(500, "<html>Internal Server Error</html>");

    LoginResultado resultado = service.login("ana", "secreta");

    assertEquals(EstadosEnum.ERROR, resultado.estado());
    assertEquals("El servidor respondió con un error (HTTP 500)", resultado.mensaje());
  }

  @Test
  void serverDownReturnsConnectionErrorWithEndpoint() throws Exception {
    String base = FakeRestServer.unreachableBaseUrl();

    LoginResultado resultado = new LoginService(base).login("ana", "secreta");

    assertEquals(EstadosEnum.ERROR, resultado.estado());
    assertEquals("No se pudo conectar con el servidor: " + base + "/api/Login/loginEmpleado",
        resultado.mensaje());
  }

  @Test
  void logoutPostsTheToken() {
    service.logout("t1");

    assertEquals("POST " + FakeRestServer.CONTEXT + "/api/Login/logout", server.lastPath());
    assertEquals("t1", server.lastBody().getString("token"));
  }

  @Test
  void logoutWithServerDownDoesNotFail() throws Exception {
    LoginService sinServidor = new LoginService(FakeRestServer.unreachableBaseUrl());

    assertDoesNotThrow(() -> sinServidor.logout("t1"));
  }
}
