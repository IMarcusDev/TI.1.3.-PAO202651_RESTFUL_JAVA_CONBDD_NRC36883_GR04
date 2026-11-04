package ec.edu.monster.modelo;

/**
 * REST routes the client consumes, relative to the configured base URL
 * (http://{ws.host}:{ws.port}/{ws.context}).
 *
 * <p>Mirrors the RESTful server ({@code LoginWebService} and {@code MovimientoWebService}).
 * JAX-RS paths are case-sensitive, so "Login" and "Movimiento" must keep their capital letter.
 * All routes are POST with a JSON body; the session token travels in the body, and an unknown or
 * expired token answers HTTP 401. If the server changes, adjust the routes here and the JSON field
 * names in {@link LoginServicioRest} and {@link MovimientoServicioRest}; nothing else depends on them.
 *
 * <pre>
 * POST api/Login/loginEmpleado   {"usuario","clave"}
 * POST api/Login/loginCliente    {"cuenta","clave"}
 *      -> {"estado","mensaje","token","nombre","tipoUsuario","perfil"}
 * POST api/Login/logout          {"token"}
 * POST api/Movimiento/consultarMovimientos     {"token","cuenta"}
 *      -> {"estado","mensaje","cuenta":{...},"movimientos":[...]}
 * POST api/Movimiento/registrarDeposito        {"token","cuenta","importe"}
 * POST api/Movimiento/registrarRetiro          {"token","cuenta","importe","clave"}
 * POST api/Movimiento/registrarTransferencia   {"token","cuentaOrigen","cuentaDestino","importe","clave"}
 *      -> {"estado","mensaje","numeroMovimiento","nuevoSaldo"}
 * </pre>
 */
public final class RutasApi {

    /** API root; also used to check whether the server is up. */
    public static final String RAIZ = "api";

    public static final String LOGIN_EMPLEADO = RAIZ + "/Login/loginEmpleado";
    public static final String LOGIN_CLIENTE = RAIZ + "/Login/loginCliente";
    public static final String LOGOUT = RAIZ + "/Login/logout";

    public static final String CONSULTAR_MOVIMIENTOS = RAIZ + "/Movimiento/consultarMovimientos";
    public static final String REGISTRAR_DEPOSITO = RAIZ + "/Movimiento/registrarDeposito";
    public static final String REGISTRAR_RETIRO = RAIZ + "/Movimiento/registrarRetiro";
    public static final String REGISTRAR_TRANSFERENCIA = RAIZ + "/Movimiento/registrarTransferencia";

    private RutasApi() {
    }
}
