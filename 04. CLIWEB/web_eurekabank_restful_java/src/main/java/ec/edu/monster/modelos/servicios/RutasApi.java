package ec.edu.monster.modelos.servicios;

/**
 * REST routes the client consumes, relative to the configured base URL
 * (http://{ws.host}:{ws.port}/{ws.context}).
 *
 * <p>Mirrors the RESTful server ({@code LoginWebService} and {@code MovimientoWebService}).
 * JAX-RS paths are case-sensitive, so "Login" and "Movimiento" must keep their capital letter.
 * All routes are POST with a JSON body; the session token travels in the body, and an unknown or
 * expired token answers HTTP 401. If the server changes, adjust the routes here and the JSON field
 * names in {@link LoginService} and {@link MovimientoService}; nothing else depends on them.
 *
 * <pre>
 * POST api/Login/loginEmpleado   {"usuario","clave"}
 *      -> {"estado","mensaje","token","nombre","tipoUsuario","perfil"}
 * POST api/Login/logout          {"token"}
 * POST api/Movimiento/registrarDeposito        {"token","cuenta","importe"}
 * POST api/Movimiento/registrarRetiro          {"token","cuenta","importe","clave"}
 * POST api/Movimiento/registrarTransferencia   {"token","cuentaOrigen","cuentaDestino","importe","clave"}
 *      -> {"estado","mensaje","numeroMovimiento","nuevoSaldo"}
 * </pre>
 */
public final class RutasApi {
  public static final String LOGIN_EMPLEADO = "api/Login/loginEmpleado";
  public static final String LOGOUT = "api/Login/logout";

  public static final String REGISTRAR_DEPOSITO = "api/Movimiento/registrarDeposito";
  public static final String REGISTRAR_RETIRO = "api/Movimiento/registrarRetiro";
  public static final String REGISTRAR_TRANSFERENCIA = "api/Movimiento/registrarTransferencia";

  private RutasApi() {
    // Constants only
  }
}
