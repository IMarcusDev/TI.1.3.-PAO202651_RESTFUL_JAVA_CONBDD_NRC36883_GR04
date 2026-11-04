package ec.edu.monster.modelos.servicios;

import ec.edu.monster.config.WsConfig;
import ec.edu.monster.modelos.dto.EstadosEnum;
import ec.edu.monster.modelos.dto.LoginResultado;
import java.time.Duration;
import org.json.JSONObject;

/**
 * Client-side access to the RESTful login routes.
 * login returns the token every movement needs; logout invalidates it on the server.
 */
public class LoginService {
  private static final Duration ESPERA_RESPUESTA = Duration.ofSeconds(15);
  private static final String SIN_TOKEN = "El servidor no devolvió un token de sesión";

  private final String loginEndpoint;
  private final String loginClienteEndpoint;
  private final String logoutEndpoint;
  // A login creates nothing on the server, so a late answer reads like a connection failure
  private final ClienteRest cliente;

  public LoginService() {
    this(WsConfig.getBaseUrl());
  }

  LoginService(String baseUrl) {
    this.loginEndpoint = baseUrl + "/" + RutasApi.LOGIN_EMPLEADO;
    this.loginClienteEndpoint = baseUrl + "/" + RutasApi.LOGIN_CLIENTE;
    this.logoutEndpoint = baseUrl + "/" + RutasApi.LOGOUT;
    this.cliente = new ClienteRest(ESPERA_RESPUESTA, ClienteRest.sinConexion(loginEndpoint));
  }

  public LoginResultado login(String usuario, String clave) {
    return autenticar(loginEndpoint, new JSONObject().put("usuario", usuario).put("clave", clave));
  }

  public LoginResultado loginCliente(String cuenta, String clave) {
    return autenticar(loginClienteEndpoint, new JSONObject().put("cuenta", cuenta).put("clave", clave));
  }

  private LoginResultado autenticar(String endpoint, JSONObject cuerpo) {
    ClienteRest.Respuesta respuesta = cliente.post(endpoint, cuerpo);
    if (!respuesta.ok()) {
      return LoginResultado.error(respuesta.mensaje());
    }
    JSONObject json = respuesta.json();
    String token = json.optString("token", "");
    if (token.isBlank()) {
      return LoginResultado.error(SIN_TOKEN);
    }
    return new LoginResultado(EstadosEnum.OK, respuesta.mensaje(), token,
        json.optString("nombre", ""), json.optString("tipoUsuario", ""));
  }

  /**
   * Best effort: the web session ends anyway, so a server that is down or
   * a token that already expired must not stop the user from leaving.
   */
  public void logout(String token) {
    try {
      cliente.post(logoutEndpoint, new JSONObject().put("token", token));
    } catch (RuntimeException e) {
      // Nothing to report: the caller closes the web session regardless
    }
  }
}
