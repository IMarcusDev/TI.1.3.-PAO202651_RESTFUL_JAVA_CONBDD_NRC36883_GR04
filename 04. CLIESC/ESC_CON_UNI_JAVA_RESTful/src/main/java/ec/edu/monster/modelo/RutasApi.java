package ec.edu.monster.modelo;

/**
 * REST routes the client consumes, relative to the configured base URL
 * (http://{ws.host}:{ws.port}/{ws.context}).
 *
 * <p>Mirrors the RESTful server ({@code LoginWebService} and {@code MovimientoWebService}).
 * JAX-RS paths are case-sensitive, so "Login" and "Movimiento" must keep their capital letter.
 * If the server changes, adjust the routes here and the JSON field names in
 * {@link LoginServicioRest} and {@link MovimientoServicioRest}; nothing else depends on them.
 *
 * <pre>
 * POST api/Login                       {"username","password"}
 *      -> {"estado":"OK|ERROR","mensaje","token","nombre","tipoUsuario"}
 * POST api/Movimiento/deposito         {"token","cuentaOrigen","cuentaDestino","valor"}
 * POST api/Movimiento/retiro           -> {"estado":"OK|ERROR","mensaje","idMovimiento"}
 * POST api/Movimiento/transferencia
 * </pre>
 */
public final class RutasApi {

    /** API root; also used to check whether the server is up. */
    public static final String RAIZ = "api";
    public static final String LOGIN = RAIZ + "/Login";
    public static final String DEPOSITO = RAIZ + "/Movimiento/deposito";
    public static final String RETIRO = RAIZ + "/Movimiento/retiro";
    public static final String TRANSFERENCIA = RAIZ + "/Movimiento/transferencia";

    private RutasApi() {
    }
}
