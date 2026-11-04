package ec.edu.monster.controladores;

import ec.edu.monster.modelos.dto.EstadosEnum;
import ec.edu.monster.modelos.dto.HistorialCuenta;
import ec.edu.monster.modelos.servicios.MovimientoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller of the movement consultation screen ("Consultar movimientos").
 * GET  /movimientos -> displays the search form (prefilled for Cliente)
 * POST /movimientos -> calls the REST service and renders account details and movements
 * Read-only screen: no PRG or session flash needed.
 */
@WebServlet(name = "MovimientosController", urlPatterns = {"/movimientos"})
public class MovimientosController extends HttpServlet {
  private static final String VISTA = "/WEB-INF/ec/edu/monster/vista/movimientos.jsp";
  private static final String RUTA_LOGIN = "/login";
  private static final String RUTA_EXPIRADA = "/login?expirada=1";
  private static final String CUENTA_VACIA = "Ingrese el número de cuenta";

  private final transient MovimientoService movimientoService;

  public MovimientosController() {
    this(new MovimientoService());
  }

  // Used by the tests to inject a mock model
  MovimientosController(MovimientoService movimientoService) {
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

    String cuentaCliente = cuentaCliente(session);
    if (cuentaCliente != null) {
      request.setAttribute("cuenta", cuentaCliente);
    }
    request.getRequestDispatcher(VISTA).forward(request, response);
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
    String cuenta = esCliente ? cuentaCliente : request.getParameter("cuenta");

    if (cuenta == null || cuenta.isBlank()) {
      request.setAttribute("error", CUENTA_VACIA);
      request.getRequestDispatcher(VISTA).forward(request, response);
      return;
    }

    cuenta = cuenta.trim();
    request.setAttribute("cuenta", cuenta);

    HistorialCuenta historial = movimientoService.consultarMovimientos(token, cuenta);

    if (historial != null && historial.estado() == EstadosEnum.SESION_EXPIRADA) {
      session.invalidate();
      response.sendRedirect(request.getContextPath() + RUTA_EXPIRADA);
      return;
    }

    if (historial == null || historial.estado() != EstadosEnum.OK) {
      request.setAttribute("error", historial == null
          ? "No se pudieron consultar los movimientos" : historial.mensaje());
      request.getRequestDispatcher(VISTA).forward(request, response);
      return;
    }

    request.setAttribute("historial", historial);
    request.getRequestDispatcher(VISTA).forward(request, response);
  }

  private static String token(HttpSession session) {
    if (session == null) {
      return null;
    }
    Object token = session.getAttribute("token");
    return token instanceof String value && !value.isBlank() ? value : null;
  }

  private static String cuentaCliente(HttpSession session) {
    if (session == null) {
      return null;
    }
    Object cuenta = session.getAttribute("cuenta");
    return cuenta instanceof String value && !value.isBlank() ? value : null;
  }
}
