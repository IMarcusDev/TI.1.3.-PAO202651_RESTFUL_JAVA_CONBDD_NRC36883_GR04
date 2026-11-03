package ec.edu.monster.modelo;

/**
 * REST routes the client consumes, relative to the configured base URL
 * (http://{ws.host}:{ws.port}/{ws.context}).
 *
 * <p>The RESTful server does not exist yet, so this contract mirrors the SOAP one operation by
 * operation. When the server is built, adjust the routes here and the JSON field names in
 * {@link LoginServicioRest} and {@link MovimientoServicioRest}; nothing else depends on them.
 *
 * <pre>
 * POST api/login                       {"username","password"}
 *      -> {"estado":"OK|ERROR","mensaje","token","nombre","tipoUsuario"}
 * POST api/movimientos/deposito        Authorization: {token}
 * POST api/movimientos/retiro          {"cuentaOrigen","cuentaDestino","valor"}
 * POST api/movimientos/transferencia   -> {"estado":"OK|ERROR","mensaje","idMovimiento"}
 * </pre>
 */
public final class RutasApi {

    /** API root; also used to check whether the server is up. */
    public static final String RAIZ = "api";
    public static final String LOGIN = RAIZ + "/login";
    public static final String DEPOSITO = RAIZ + "/movimientos/deposito";
    public static final String RETIRO = RAIZ + "/movimientos/retiro";
    public static final String TRANSFERENCIA = RAIZ + "/movimientos/transferencia";

    private RutasApi() {
    }
}
