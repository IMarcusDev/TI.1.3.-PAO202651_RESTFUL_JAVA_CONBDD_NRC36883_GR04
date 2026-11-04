package ec.edu.monster.modelos.servicios;

import ec.edu.monster.config.WsConfig;
import ec.edu.monster.modelos.dto.EstadosEnum;
import ec.edu.monster.modelos.dto.MovimientoResultado;
import java.math.BigDecimal;
import java.time.Duration;
import org.json.JSONObject;

/**
 * Client-side access to the RESTful movement routes.
 * Every operation needs the token returned by LoginService.login.
 */
public class MovimientoService {
  private static final Duration ESPERA_RESPUESTA = Duration.ofSeconds(15);
  // The request may have been committed on the server, so retrying blindly could duplicate it
  private static final String SIN_RESPUESTA_A_TIEMPO = "El servidor no respondió a tiempo. "
      + "La operación pudo haberse registrado: verifique el saldo antes de volver a intentarlo";
  private static final String SIN_NUMERO = "El servidor no devolvió el número de movimiento";

  private final String baseUrl;
  private final ClienteRest cliente;

  public MovimientoService() {
    this(WsConfig.getBaseUrl(), ESPERA_RESPUESTA);
  }

  MovimientoService(String baseUrl, Duration esperaRespuesta) {
    this.baseUrl = baseUrl;
    this.cliente = new ClienteRest(esperaRespuesta, SIN_RESPUESTA_A_TIEMPO);
  }

  /** The server takes the employee from the token, so a deposit needs no account key. */
  public MovimientoResultado deposito(String token, String cuenta, BigDecimal importe) {
    JSONObject cuerpo = new JSONObject()
        .put("token", token)
        .put("cuenta", cuenta)
        .put("importe", importe);
    return call(RutasApi.REGISTRAR_DEPOSITO, cuerpo);
  }

  public MovimientoResultado retiro(String token, String cuenta, BigDecimal importe, String clave) {
    JSONObject cuerpo = new JSONObject()
        .put("token", token)
        .put("cuenta", cuenta)
        .put("importe", importe)
        .put("clave", clave);
    return call(RutasApi.REGISTRAR_RETIRO, cuerpo);
  }

  public MovimientoResultado transferencia(
      String token, String cuentaOrigen, String cuentaDestino, BigDecimal importe, String clave) {
    JSONObject cuerpo = new JSONObject()
        .put("token", token)
        .put("cuentaOrigen", cuentaOrigen)
        .put("cuentaDestino", cuentaDestino)
        .put("importe", importe)
        .put("clave", clave);
    return call(RutasApi.REGISTRAR_TRANSFERENCIA, cuerpo);
  }

  /**
   * Shared logic of the three operations: post the body and read the receipt,
   * so the answer is interpreted once instead of three times.
   */
  private MovimientoResultado call(String ruta, JSONObject cuerpo) {
    ClienteRest.Respuesta respuesta = cliente.post(baseUrl + "/" + ruta, cuerpo);
    if (!respuesta.ok()) {
      return MovimientoResultado.fallo(respuesta.estado(), respuesta.mensaje());
    }
    JSONObject json = respuesta.json();
    // Without a movement number there is no receipt to show, even if estado says OK
    Object numero = json.opt("numeroMovimiento");
    if (!(numero instanceof Number) || ((Number) numero).intValue() <= 0) {
      return MovimientoResultado.error(SIN_NUMERO);
    }
    return new MovimientoResultado(EstadosEnum.OK, respuesta.mensaje(),
        ((Number) numero).intValue(), json.optBigDecimal("nuevoSaldo", null));
  }
}
