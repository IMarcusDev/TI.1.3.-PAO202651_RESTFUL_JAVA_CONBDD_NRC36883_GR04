package ec.edu.monster.controladores;

import ec.edu.monster.modelos.dto.EstadosEnum;
import ec.edu.monster.modelos.dto.MovimientoResultado;
import ec.edu.monster.modelos.servicios.MovimientoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller of the withdrawal screen.
 * GET  /retiro                 -> empty form, or the receipt after a successful withdrawal
 * POST /retiro paso=revisar    -> validates and shows the confirmation (asks the account key)
 * POST /retiro paso=editar     -> back to the form keeping the values
 * POST /retiro paso=confirmar  -> re-validates, calls the REST service and redirects (PRG)
 * The account key is only read on confirm: it is never put in the request, the view
 * or the session.
 */
@WebServlet(name = "RetiroController", urlPatterns = {"/retiro"})
public class RetiroController extends HttpServlet {
  private static final String VISTA = "/WEB-INF/ec/edu/monster/vista/retiro.jsp";
  private static final String RUTA = "/retiro";
  private static final String RUTA_LOGIN = "/login";
  // The server rejected the token: LoginController shows the "session expired" notice
  private static final String RUTA_EXPIRADA = "/login?expirada=1";
  // Session flash key: the receipt survives exactly one redirect
  private static final String FLASH = "comprobanteRetiro";
  private static final String CLAVE_VACIA = "Ingrese la clave de la cuenta";
  private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private final transient MovimientoService movimientoService;

  public RetiroController() {
    this(new MovimientoService());
  }

  // Used by the tests to inject a mock model
  RetiroController(MovimientoService movimientoService) {
    this.movimientoService = movimientoService;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    String token = token(session);
    if (token == null) {
      response.sendRedirect(request.getContextPath() + RUTA_LOGIN);
      return;
    }
    response.setHeader("Cache-Control", "no-store");

    Object comprobante = session.getAttribute(FLASH);
    if (comprobante != null) {
      session.removeAttribute(FLASH);
      request.setAttribute("comprobante", comprobante);
      render(request, response, "comprobante");
      return;
    }
    String cuentaCliente = cuentaCliente(session);
    if (cuentaCliente != null) {
      request.setAttribute("cuenta", cuentaCliente);
    }
    render(request, response, "formulario");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    String token = token(session);
    if (token == null) {
      response.sendRedirect(request.getContextPath() + RUTA_LOGIN);
      return;
    }
    response.setHeader("Cache-Control", "no-store");

    String cuentaCliente = cuentaCliente(session);
    boolean esCliente = cuentaCliente != null;

    String paso = request.getParameter("paso");
    String cuenta = esCliente ? cuentaCliente : request.getParameter("cuenta");
    String importe = request.getParameter("importe");

    if ("editar".equals(paso)) {
      showForm(request, response, cuenta, importe);
      return;
    }

    // Always validated, also on confirm: hidden fields can be tampered with
    MovimientoForm form = MovimientoForm.retiro(cuenta, importe);
    if (!form.isValido()) {
      request.setAttribute("errores", form.getErrores());
      showForm(request, response, cuenta, importe);
      return;
    }

    if (!"confirmar".equals(paso)) {
      showConfirmation(request, response, form);
      return;
    }

    String clave = "";
    if (!esCliente) {
      // Sent as typed (no trim): the server compares it with the stored key
      clave = request.getParameter("clave");
      if (clave == null || clave.isBlank()) {
        request.setAttribute("errores", Map.of("clave", CLAVE_VACIA));
        showConfirmation(request, response, form);
        return;
      }
    }

    MovimientoResultado resultado =
        movimientoService.retiro(token, form.getCuenta(), form.getImporte(), clave);

    if (resultado != null && resultado.estado() == EstadosEnum.SESION_EXPIRADA) {
      // The token is no longer valid: end the web session and log in again
      session.invalidate();
      response.sendRedirect(request.getContextPath() + RUTA_EXPIRADA);
      return;
    }

    if (resultado == null || resultado.estado() != EstadosEnum.OK) {
      request.setAttribute("error", resultado == null
          ? "No se pudo registrar el retiro" : resultado.mensaje());
      showForm(request, response, cuenta, importe);
      return;
    }

    // Only Strings in the session, so it stays Serializable (never the key)
    Map<String, String> comprobante = new LinkedHashMap<>();
    comprobante.put("operacion", "Retiro");
    comprobante.put("cuenta", form.getCuenta());
    comprobante.put("importe", form.getImporteFormateado());
    comprobante.put("idMovimiento", String.valueOf(resultado.numeroMovimiento()));
    if (resultado.nuevoSaldo() != null) {
      comprobante.put("nuevoSaldo", MovimientoForm.formatearImporte(resultado.nuevoSaldo()));
    }
    comprobante.put("fecha", LocalDateTime.now().format(FECHA));
    comprobante.put("mensaje", resultado.mensaje());
    session.setAttribute(FLASH, comprobante);

    // Post/Redirect/Get: a browser refresh never repeats the withdrawal
    response.sendRedirect(request.getContextPath() + RUTA);
  }

  // Confirmation state with the validated values (the key input is always empty)
  private void showConfirmation(HttpServletRequest request, HttpServletResponse response,
      MovimientoForm form) throws ServletException, IOException {
    request.setAttribute("cuenta", form.getCuenta());
    request.setAttribute("importe", form.getImporte().toPlainString());
    request.setAttribute("importeFormateado", form.getImporteFormateado());
    render(request, response, "confirmacion");
  }

  // Form state keeping what the user typed
  private void showForm(HttpServletRequest request, HttpServletResponse response,
      String cuenta, String importe) throws ServletException, IOException {
    request.setAttribute("cuenta", cuenta);
    request.setAttribute("importe", importe);
    render(request, response, "formulario");
  }

  private void render(HttpServletRequest request, HttpServletResponse response, String vista)
      throws ServletException, IOException {
    request.setAttribute("vista", vista);
    request.getRequestDispatcher(VISTA).forward(request, response);
  }

  // Session token, or null when there is no logged-in user
  private static String token(HttpSession session) {
    if (session == null) {
      return null;
    }
    Object token = session.getAttribute("token");
    return token instanceof String value && !value.isBlank() ? value : null;
  }

  // Account number stored in session for a Cliente
  private static String cuentaCliente(HttpSession session) {
    if (session == null) {
      return null;
    }
    Object cuenta = session.getAttribute("cuenta");
    return cuenta instanceof String value && !value.isBlank() ? value : null;
  }
}
