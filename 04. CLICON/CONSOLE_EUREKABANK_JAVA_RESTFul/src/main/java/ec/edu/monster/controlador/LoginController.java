package ec.edu.monster.controlador;

import ec.edu.monster.modelos.data.Endpoints;
import ec.edu.monster.modelos.dto.LoginResponseDTO;
import ec.edu.monster.modelos.dto.ResponseDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.modelos.http.ConfigHTTP;
import ec.edu.monster.modelos.http.RESTfulClient;
import org.json.JSONObject;

//revisado y aprovado por Marcos Escobar
public class LoginController {
  /** ECUD1: el empleado ingresa con su usuario y clave. */
  public LoginResponseDTO loginEmpleado(String usuario, String clave) {
    JSONObject body = new JSONObject()
      .put("usuario", usuario)
      .put("clave", clave);

    return iniciarSesion(Endpoints.LOGIN_EMPLEADO, body, null);
  }

  /** ECUD1: el cliente ingresa con el número de su cuenta y la clave de esa cuenta. */
  public LoginResponseDTO loginCliente(String cuenta, String clave) {
    JSONObject body = new JSONObject()
      .put("cuenta", cuenta)
      .put("clave", clave);

    return iniciarSesion(Endpoints.LOGIN_CLIENTE, body, cuenta);
  }

  /** Cierra la sesión en el servidor; los datos locales se borran aunque el servidor no responda. */
  public ResponseDTO logout() {
    try {
      String json = new RESTfulClient().post(Endpoints.LOGOUT, new JSONObject().put("token", ConfigHTTP.getToken()));
      return ResponseDTO.fromJSON(json);
    } catch (Exception e) {
      return new ResponseDTO(EstadosEnum.ERROR, ErroresCliente.mensaje(e));
    } finally {
      ConfigHTTP.limpiar();
    }
  }

  private LoginResponseDTO iniciarSesion(String endpoint, JSONObject body, String cuenta) {
    try {
      String json = new RESTfulClient().post(endpoint, body);
      LoginResponseDTO dto = LoginResponseDTO.fromJSON(json);

      if (dto.getEstado() == EstadosEnum.OK) {
        ConfigHTTP.setToken(dto.getToken());
        ConfigHTTP.setPerfil(dto.getPerfil());
        ConfigHTTP.setNombre(dto.getNombre());
        ConfigHTTP.setCuenta(cuenta);
      }

      return dto;
    } catch (Exception e) {
      return new LoginResponseDTO(EstadosEnum.ERROR, ErroresCliente.mensaje(e), "");
    }
  }
}
