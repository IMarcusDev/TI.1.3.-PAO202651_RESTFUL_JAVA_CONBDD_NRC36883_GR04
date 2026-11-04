package ec.edu.monster.filtros;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;

/**
 * Access control for every protected screen, applied BEFORE the controller runs.
 * 1. No logged-in user (no session or blank token) -> /login
 * 2. Employee-only screen (/deposito) and the user is not "Empleado" -> /menu
 *
 * A Cliente is limited to their own account registered in the session and can
 * reach /menu, /retiro, /transferencia, and /movimientos. /deposito remains
 * employee-only.
 */
@WebFilter(filterName = "AuthFilter",
    urlPatterns = {"/menu", "/retiro", "/transferencia", "/deposito", "/movimientos"})
public class AuthFilter extends HttpFilter {
  // Must match the user type names returned by the server (USUARIO_TIPOS)
  public static final String ROL_EMPLEADO = "Empleado";

  // Screens only an employee (bank teller) can use
  private static final Set<String> SOLO_EMPLEADO = Set.of("/deposito");

  private static final String RUTA_LOGIN = "/login";
  private static final String RUTA_MENU = "/menu";

  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    // getSession(false): never create a session just to check the login
    HttpSession session = request.getSession(false);

    if (!hasToken(session)) {
      response.sendRedirect(request.getContextPath() + RUTA_LOGIN);
      return;
    }

    boolean soloEmpleado = SOLO_EMPLEADO.contains(request.getServletPath());
    if (soloEmpleado && !ROL_EMPLEADO.equals(session.getAttribute("tipoUsuario"))) {
      response.sendRedirect(request.getContextPath() + RUTA_MENU);
      return;
    }

    // Allowed: continue to the controller
    chain.doFilter(request, response);
  }

  private static boolean hasToken(HttpSession session) {
    if (session == null) {
      return false;
    }
    Object token = session.getAttribute("token");
    return token instanceof String value && !value.isBlank();
  }
}
