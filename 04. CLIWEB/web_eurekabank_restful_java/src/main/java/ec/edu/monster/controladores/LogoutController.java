package ec.edu.monster.controladores;

import ec.edu.monster.modelos.servicios.LoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller of the logout action.
 * POST /logout -> ends the token on the server (best effort), ends the session
 *                 and goes back to /login
 * Only POST: a link or prefetch (GET) must not log the user out.
 */
@WebServlet(name = "LogoutController", urlPatterns = {"/logout"})
public class LogoutController extends HttpServlet {
  private static final String RUTA_LOGIN = "/login";

  private final transient LoginService loginService;

  public LogoutController() {
    this(new LoginService());
  }

  // Used by the tests to inject a mock model
  LogoutController(LoginService loginService) {
    this.loginService = loginService;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // getSession(false): do not create a session only to destroy it
    HttpSession session = request.getSession(false);
    if (session != null) {
      // Read the token before invalidating; logout never throws, so the session always ends
      Object token = session.getAttribute("token");
      if (token instanceof String value && !value.isBlank()) {
        loginService.logout(value);
      }
      session.invalidate();
    }
    response.sendRedirect(request.getContextPath() + RUTA_LOGIN);
  }
}
