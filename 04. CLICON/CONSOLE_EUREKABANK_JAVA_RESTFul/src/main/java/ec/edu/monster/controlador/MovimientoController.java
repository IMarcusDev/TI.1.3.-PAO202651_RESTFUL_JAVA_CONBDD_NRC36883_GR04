package ec.edu.monster.controlador;

import ec.edu.monster.modelos.data.Endpoints;
import ec.edu.monster.modelos.dto.ConsultaResponseDTO;
import ec.edu.monster.modelos.dto.MovimientoResponseDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.modelos.http.ConfigHTTP;
import ec.edu.monster.modelos.http.RESTfulClient;
import java.math.BigDecimal;
import org.json.JSONObject;

//revisado y aprovado por Marcos Escobar
public class MovimientoController {
  /** ECUD2: datos de la cuenta y lista de sus movimientos. */
  public ConsultaResponseDTO consultar(String cuenta) {
    JSONObject body = new JSONObject()
      .put("token", ConfigHTTP.getToken())
      .put("cuenta", cuenta);

    try {
      return ConsultaResponseDTO.fromJSON(new RESTfulClient().post(Endpoints.CONSULTAR, body));
    } catch (Exception e) {
      return new ConsultaResponseDTO(EstadosEnum.ERROR, ErroresCliente.mensaje(e));
    }
  }

  /** ECUD3: el depósito lo registra el empleado de la sesión; no lleva clave. */
  public MovimientoResponseDTO deposito(String cuenta, BigDecimal importe) {
    JSONObject body = new JSONObject()
      .put("token", ConfigHTTP.getToken())
      .put("cuenta", cuenta)
      .put("importe", importe);

    return registrar(Endpoints.DEPOSITO, body);
  }

  /** ECUD3: el empleado envía la clave de la cuenta; el cliente opera su cuenta y la clave va vacía. */
  public MovimientoResponseDTO retiro(String cuenta, BigDecimal importe, String clave) {
    JSONObject body = new JSONObject()
      .put("token", ConfigHTTP.getToken())
      .put("cuenta", cuenta)
      .put("importe", importe)
      .put("clave", clave);

    return registrar(Endpoints.RETIRO, body);
  }

  public MovimientoResponseDTO transferencia(String cuentaOrigen, String cuentaDestino, BigDecimal importe,
      String clave) {
    JSONObject body = new JSONObject()
      .put("token", ConfigHTTP.getToken())
      .put("cuentaOrigen", cuentaOrigen)
      .put("cuentaDestino", cuentaDestino)
      .put("importe", importe)
      .put("clave", clave);

    return registrar(Endpoints.TRANSFERENCIA, body);
  }

  private MovimientoResponseDTO registrar(String endpoint, JSONObject body) {
    try {
      return MovimientoResponseDTO.fromJSON(new RESTfulClient().post(endpoint, body));
    } catch (Exception e) {
      return new MovimientoResponseDTO(EstadosEnum.ERROR, ErroresCliente.mensaje(e), 0);
    }
  }
}
