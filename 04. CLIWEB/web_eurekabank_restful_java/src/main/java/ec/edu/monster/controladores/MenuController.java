package ec.edu.monster.controladores;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller of the main menu (home screen after login).
 * GET /menu -> shows the menu if the user is logged in, otherwise goes to /login
 */
@WebServlet(name = "MenuController", urlPatterns = {"/menu"})
public class MenuController extends HttpServlet {
  // JSP inside WEB-INF: only reachable through this controller
  private static final String VISTA_MENU = "/WEB-INF/ec/edu/monster/vista/menu.jsp";

  private static final String RUTA_LOGIN = "/login";

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // getSession(false): never create a session just to check the login
    HttpSession session = request.getSession(false);

    if (!hasToken(session)) {
      response.sendRedirect(request.getContextPath() + RUTA_LOGIN);
      return;
    }

    // no-store: after logout, the Back button cannot show this page from cache
    response.setHeader("Cache-Control", "no-store");
    request.getRequestDispatcher(VISTA_MENU).forward(request, response);
  }

  private static boolean hasToken(HttpSession session) {
    if (session == null) {
      return false;
    }
    Object token = session.getAttribute("token");
    return token instanceof String value && !value.isBlank();
  }
}
