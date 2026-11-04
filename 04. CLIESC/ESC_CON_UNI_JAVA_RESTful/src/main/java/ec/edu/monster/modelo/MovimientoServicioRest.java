package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Real implementation of {@link MovimientoServicio}: calls {@code POST api/Movimiento/...} with the
 * session token in the body and turns its answer (or a connection failure) into a result or an
 * exception.
 */
public class MovimientoServicioRest implements MovimientoServicio {

    private static final String SIN_NUMERO = "El servidor no devolvió el número de movimiento";

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
    public HistorialCuenta consultarMovimientos(String token, String cuenta) throws ServicioException {
        JSONObject respuesta = cliente.post(configuracion.getEndpoint(RutasApi.CONSULTAR_MOVIMIENTOS),
                new JSONObject().put("token", token).put("cuenta", cuenta));
        return new HistorialCuenta(cuenta(respuesta.optJSONObject("cuenta")),
                movimientos(respuesta.optJSONArray("movimientos")));
    }

    @Override
    public ResultadoMovimiento registrarDeposito(String token, String cuenta, BigDecimal importe)
            throws ServicioException {
        return registrar(RutasApi.REGISTRAR_DEPOSITO, new JSONObject()
                .put("token", token)
                .put("cuenta", cuenta)
                .put("importe", importe));
    }

    @Override
    public ResultadoMovimiento registrarRetiro(String token, String cuenta, BigDecimal importe, String clave)
            throws ServicioException {
        return registrar(RutasApi.REGISTRAR_RETIRO, new JSONObject()
                .put("token", token)
                .put("cuenta", cuenta)
                .put("importe", importe)
                .put("clave", clave == null ? "" : clave));
    }

    @Override
    public ResultadoMovimiento registrarTransferencia(String token, String cuentaOrigen, String cuentaDestino,
            BigDecimal importe, String clave) throws ServicioException {
        return registrar(RutasApi.REGISTRAR_TRANSFERENCIA, new JSONObject()
                .put("token", token)
                .put("cuentaOrigen", cuentaOrigen)
                .put("cuentaDestino", cuentaDestino)
                .put("importe", importe)
                .put("clave", clave == null ? "" : clave));
    }

    // Shared logic of the three operations: same answer, different route and body
    private ResultadoMovimiento registrar(String ruta, JSONObject cuerpo) throws ServicioException {
        JSONObject respuesta = cliente.post(configuracion.getEndpoint(ruta), cuerpo);

        // Without the number the receipt would show movement 0
        if (!(respuesta.opt("numeroMovimiento") instanceof Number numero) || numero.intValue() <= 0) {
            throw new ServicioException(SIN_NUMERO);
        }
        return new ResultadoMovimiento(numero.intValue(), respuesta.optBigDecimal("nuevoSaldo", null),
                ServicioException.mensaje(respuesta.optString("mensaje", null)));
    }

    private static HistorialCuenta.CuentaConsultada cuenta(JSONObject datos) {
        if (datos == null) {
            return new HistorialCuenta.CuentaConsultada("", "", "", null, "");
        }
        return new HistorialCuenta.CuentaConsultada(datos.optString("numeroCuenta", ""),
                datos.optString("titular", ""), datos.optString("moneda", ""),
                datos.optBigDecimal("saldo", null), datos.optString("estadoCuenta", ""));
    }

    private static List<HistorialCuenta.MovimientoConsultado> movimientos(JSONArray datos) {
        List<HistorialCuenta.MovimientoConsultado> filas = new ArrayList<>();
        if (datos == null) {
            return filas;
        }
        for (int i = 0; i < datos.length(); i++) {
            JSONObject movimiento = datos.optJSONObject(i);
            if (movimiento == null) {
                continue;
            }
            filas.add(new HistorialCuenta.MovimientoConsultado(
                    Formatos.numeroMovimiento(movimiento.optInt("numeroMovimiento")),
                    Formatos.fecha(movimiento.optString("fecha", "")),
                    movimiento.optString("tipoMovimiento", ""),
                    movimiento.optString("accion", ""),
                    movimiento.optBigDecimal("importe", null),
                    Formatos.referencia(movimiento.optString("cuentaReferencia", ""))));
        }
        return filas;
    }
}
