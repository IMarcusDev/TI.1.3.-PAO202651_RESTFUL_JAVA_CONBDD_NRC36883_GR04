package ec.edu.monster.controladores;

import ec.edu.monster.modelos.dto.EstadosEnum;
import ec.edu.monster.modelos.dto.LoginResultado;
import ec.edu.monster.modelos.servicios.LoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller of the login screen.
 * GET  /login             -> shows the form
 * GET  /login?expirada=1  -> shows the form with the "session expired" notice
 * POST /login             -> validates the credentials against the RESTful server
 */
@WebServlet(name = "LoginController", urlPatterns = {"/login"})
public class LoginController extends HttpServlet {
  // JSP inside WEB-INF: only reachable through this controller
  private static final String VISTA_LOGIN = "/WEB-INF/ec/edu/monster/vista/login.jsp";

  // Page shown after a successful login
  private static final String RUTA_MENU = "/menu";

  // Fixed text: the parameter only selects it and is never echoed into the page
  private static final String AVISO_EXPIRADA =
      "Su sesión expiró o no es válida. Inicie sesión nuevamente.";

  // Model: the only class that talks to the REST login routes
  private final transient LoginService loginService;

  public LoginController() {
    this(new LoginService());
  }

  // Used by the tests to inject a mock model
  LoginController(LoginService loginService) {
    this.loginService = loginService;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Set by the operation controllers when the server rejects the token
    if ("1".equals(request.getParameter("expirada"))) {
      request.setAttribute("aviso", AVISO_EXPIRADA);
    }
    request.setAttribute("tipo", "empleado");
    request.getRequestDispatcher(VISTA_LOGIN).forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String tipo = request.getParameter("tipo");
    boolean esCliente = "cliente".equalsIgnoreCase(tipo);

    // Read the form fields (name="username" / name="password" in the JSP)
    String username = request.getParameter("username");
    String password = request.getParameter("password");

    // Avoid a REST call when the form is incomplete
    if (isBlank(username) || isBlank(password)) {
      showError(request, response, "Ingrese usuario y contraseña", username, esCliente ? "cliente" : "empleado");
      return;
    }

    String identificador = username.trim();
    LoginResultado resultado = esCliente
        ? loginService.loginCliente(identificador, password)
        : loginService.login(identificador, password);

    if (resultado.estado() != EstadosEnum.OK) {
      // Wrong credentials or connection problem: message comes from the model
      showError(request, response, resultado.mensaje(), username, esCliente ? "cliente" : "empleado");
      return;
    }

    // New session id after login, so an old (possibly stolen) id is not reused
    request.getSession().invalidate();
    HttpSession session = request.getSession(true);

    // Only plain Strings in the session, so it stays Serializable
    session.setAttribute("token", resultado.token());
    session.setAttribute("username", identificador);
    session.setAttribute("nombre", resultado.nombre());
    session.setAttribute("tipoUsuario", resultado.tipoUsuario());
    if (esCliente) {
      session.setAttribute("cuenta", identificador);
    }

    // Redirect (not forward): a browser refresh will not resend the password
    response.sendRedirect(request.getContextPath() + RUTA_MENU);
  }

  // Back to the form with an error message; the username is kept, the password is not
  private void showError(HttpServletRequest request, HttpServletResponse response,
      String mensaje, String username, String tipo) throws ServletException, IOException {
    request.setAttribute("error", mensaje);
    request.setAttribute("username", username);
    request.setAttribute("tipo", tipo);
    request.getRequestDispatcher(VISTA_LOGIN).forward(request, response);
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
