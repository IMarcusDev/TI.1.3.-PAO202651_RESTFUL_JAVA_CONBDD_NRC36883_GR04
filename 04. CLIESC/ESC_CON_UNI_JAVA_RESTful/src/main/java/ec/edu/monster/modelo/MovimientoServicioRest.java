package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.time.Duration;
import org.json.JSONObject;

/**
 * Real implementation of {@link MovimientoServicio}: calls {@code POST api/movimientos/...}
 * with the session token and turns its answer (or a connection failure) into a result or an
 * exception.
 */
public class MovimientoServicioRest implements MovimientoServicio {

    private static final String SIN_ID = "El servidor no devolvió el número de movimiento";

    private static final String SIN_RESPUESTA_A_TIEMPO = "El servidor no respondió a tiempo. La operación pudo "
            + "haberse registrado: verifique el saldo antes de volver a intentarlo";

    private final Configuracion configuracion;
    private final ClienteRest cliente;

    public MovimientoServicioRest(Configuracion configuracion) {
        this(configuracion, ClienteRest.ESPERA_RESPUESTA);
    }

    /** Same as above, waiting at most {@code esperaRespuesta} for each answer. */
    public MovimientoServicioRest(Configuracion configuracion, Duration esperaRespuesta) {
        this.configuracion = configuracion;
        this.cliente = new ClienteRest(esperaRespuesta, SIN_RESPUESTA_A_TIEMPO);
    }

    @Override
    public ResultadoMovimiento deposito(String token, String cuentaOrigen, String cuentaDestino, BigDecimal valor)
            throws ServicioException {
        return ejecutar(RutasApi.DEPOSITO, token, cuentaOrigen, cuentaDestino, valor);
    }

    @Override
    public ResultadoMovimiento retiro(String token, String cuentaOrigen, String cuentaDestino, BigDecimal valor)
            throws ServicioException {
        return ejecutar(RutasApi.RETIRO, token, cuentaOrigen, cuentaDestino, valor);
    }

    @Override
    public ResultadoMovimiento transferencia(String token, String cuentaOrigen, String cuentaDestino,
            BigDecimal valor) throws ServicioException {
        return ejecutar(RutasApi.TRANSFERENCIA, token, cuentaOrigen, cuentaDestino, valor);
    }

    // Shared logic of the three operations: same body, same answer, different route
    private ResultadoMovimiento ejecutar(String ruta, String token, String cuentaOrigen, String cuentaDestino,
            BigDecimal valor) throws ServicioException {
        JSONObject movimiento = new JSONObject()
                .put("cuentaOrigen", cuentaOrigen)
                .put("cuentaDestino", cuentaDestino)
                .put("valor", valor);

        JSONObject respuesta = cliente.post(configuracion.getEndpoint(ruta), token, movimiento);

        // Without the id the receipt would show movement number 0
        if (!(respuesta.opt("idMovimiento") instanceof Number id)) {
            throw new ServicioException(SIN_ID);
        }
        return new ResultadoMovimiento(id.intValue(),
                ServicioException.mensaje(respuesta.optString("mensaje", null)));
    }
}
