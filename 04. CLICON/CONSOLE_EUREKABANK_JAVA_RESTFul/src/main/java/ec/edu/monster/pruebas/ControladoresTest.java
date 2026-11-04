package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.controlador.ErroresCliente;
import ec.edu.monster.controlador.LoginController;
import ec.edu.monster.controlador.MovimientoController;
import ec.edu.monster.modelos.dto.ConsultaResponseDTO;
import ec.edu.monster.modelos.dto.LoginResponseDTO;
import ec.edu.monster.modelos.dto.MovimientoResponseDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.modelos.http.ConfigHTTP;
import java.math.BigDecimal;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

//revisado y aprovado por Marcos Escobar
class ControladoresTest {
  private static final String BASE = "http://servidor:8080/WS_EUREKABANK_JAVA_RESTFul/api";

  private static ServidorFalso preparar(String respuesta) {
    ServidorFalso falso = new ServidorFalso(respuesta);
    ConfigHTTP.setTransporte(falso);
    ConfigHTTP.setBaseUrl(BASE);
    ConfigHTTP.limpiar();
    return falso;
  }

  private static void sesionEmpleado() {
    ConfigHTTP.setToken("tok-e");
    ConfigHTTP.setPerfil(ConfigHTTP.EMPLEADO);
  }

  @Test
  void loginEmpleadoRutaCuerpoYSesion() {
    ServidorFalso s = preparar("{\"estado\":\"OK\",\"mensaje\":\"Ingreso de sesión exitoso\",\"token\":\"tok-e\",\"perfil\":\"EMPLEADO\",\"nombre\":\"Carla Andrade\"}");
    LoginResponseDTO r = new LoginController().loginEmpleado("cajero", "cajero123");
    assertEquals(EstadosEnum.OK, r.getEstado());
    assertEquals(BASE + "/Login/loginEmpleado", s.url);
    JSONObject cuerpo = new JSONObject(s.cuerpo);
    assertEquals("cajero", cuerpo.getString("usuario"));
    assertEquals("cajero123", cuerpo.getString("clave"));
    assertEquals("tok-e", ConfigHTTP.getToken());
    assertFalse(ConfigHTTP.esCliente());
    assertEquals("", ConfigHTTP.getCuenta());
  }

  @Test
  void loginClienteGuardaLaCuenta() {
    ServidorFalso s = preparar("{\"estado\":0,\"mensaje\":\"ok\",\"token\":\"tok-c\",\"perfil\":\"CLIENTE\",\"nombre\":\"Ana Torres\"}");
    new LoginController().loginCliente("00100001", "123456");
    assertEquals(BASE + "/Login/loginCliente", s.url);
    assertEquals("00100001", new JSONObject(s.cuerpo).getString("cuenta"));
    assertTrue(ConfigHTTP.esCliente());
    assertEquals("00100001", ConfigHTTP.getCuenta());
  }

  @Test
  void loginRechazadoNoAbreSesion() {
    preparar("{\"estado\":\"ERROR\",\"mensaje\":\"Credenciales incorrectas\",\"token\":\"\"}");
    LoginResponseDTO r = new LoginController().loginCliente("00100001", "999999");
    assertEquals("Credenciales incorrectas", r.getMensaje());
    assertFalse(ConfigHTTP.haySesion());
  }

  @Test
  void sinServidorORespuestaQueNoEsJson() {
    ServidorFalso s = preparar("");
    s.sinRed = true;
    assertEquals(ErroresCliente.SIN_SERVIDOR, new LoginController().loginEmpleado("cajero", "x").getMensaje());
    s.sinRed = false;
    s.codigo = 404;
    s.respuesta = "<html>404</html>";
    assertEquals(ErroresCliente.RESPUESTA_INCOMPLETA, new LoginController().loginEmpleado("cajero", "x").getMensaje());
  }

  @Test
  void logoutEnviaTokenYBorraLaSesionAunqueFalleLaRed() {
    ServidorFalso s = preparar("");
    sesionEmpleado();
    s.sinRed = true;
    new LoginController().logout();
    assertEquals(BASE + "/Login/logout", s.url);
    assertEquals("tok-e", new JSONObject(s.cuerpo).getString("token"));
    assertFalse(ConfigHTTP.haySesion());
  }

  @Test
  void consultarEnviaTokenYCuenta() {
    ServidorFalso s = preparar("{\"estado\":\"OK\",\"mensaje\":\"ok\",\"cuenta\":{\"numeroCuenta\":\"00100001\",\"saldo\":500},\"movimientos\":[]}");
    sesionEmpleado();
    ConsultaResponseDTO r = new MovimientoController().consultar("00100001");
    assertEquals(BASE + "/Movimiento/consultarMovimientos", s.url);
    assertEquals("00100001", new JSONObject(s.cuerpo).getString("cuenta"));
    assertEquals(0, new BigDecimal("500").compareTo(r.getSaldo()));
  }

  @Test
  void depositoConImporteNumericoYSinClave() {
    ServidorFalso s = preparar("{\"estado\":\"OK\",\"mensaje\":\"Depósito registrado correctamente\",\"numeroMovimiento\":5,\"nuevoSaldo\":620.50}");
    sesionEmpleado();
    MovimientoResponseDTO r = new MovimientoController().deposito("00100001", new BigDecimal("120.50"));
    assertEquals(BASE + "/Movimiento/registrarDeposito", s.url);
    JSONObject cuerpo = new JSONObject(s.cuerpo);
    assertTrue(cuerpo.get("importe") instanceof Number);
    assertEquals(0, new BigDecimal("120.50").compareTo(new BigDecimal(cuerpo.get("importe").toString())));
    assertFalse(cuerpo.has("clave"));
    assertEquals(5, r.getNumeroMovimiento());
    assertEquals(0, new BigDecimal("620.50").compareTo(r.getNuevoSaldo()));
  }

  @Test
  void retiroDelClienteConClaveVacia() {
    ServidorFalso s = preparar("{\"estado\":\"OK\",\"mensaje\":\"ok\"}");
    ConfigHTTP.setToken("tok-c");
    new MovimientoController().retiro("00100001", new BigDecimal("40.00"), "");
    assertEquals(BASE + "/Movimiento/registrarRetiro", s.url);
    JSONObject cuerpo = new JSONObject(s.cuerpo);
    assertEquals("", cuerpo.getString("clave"));
    assertEquals("tok-c", cuerpo.getString("token"));
  }

  @Test
  void transferenciaConLasDosCuentasYLaClave() {
    ServidorFalso s = preparar("{\"estado\":\"OK\",\"mensaje\":\"ok\"}");
    sesionEmpleado();
    new MovimientoController().transferencia("00100002", "00100001", new BigDecimal("200.00"), "654321");
    assertEquals(BASE + "/Movimiento/registrarTransferencia", s.url);
    JSONObject cuerpo = new JSONObject(s.cuerpo);
    assertEquals("00100002", cuerpo.getString("cuentaOrigen"));
    assertEquals("00100001", cuerpo.getString("cuentaDestino"));
    assertEquals("654321", cuerpo.getString("clave"));
  }

  @Test
  void tokenVencidoEsSesionExpirada() {
    preparar("{\"estado\":\"ERROR\",\"mensaje\":\"Token inválido o sesión expirada\"}");
    assertTrue(new MovimientoController().retiro("00100001", BigDecimal.ONE, "").sesionExpirada());
  }
}
