package ec.edu.monster.controladores;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the menu controller through its public doGet, with a fake browser
 * (Mockito mocks). No REST call is involved.
 */
class MenuControllerTest {
  private static final String VISTA = "/WEB-INF/ec/edu/monster/vista/menu.jsp";

  private final MenuController controller = new MenuController();
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final RequestDispatcher dispatcher = mock(RequestDispatcher.class);
  private final HttpSession session = mock(HttpSession.class);

  @BeforeEach
  void setUp() {
    when(request.getRequestDispatcher(VISTA)).thenReturn(dispatcher);
    when(request.getContextPath()).thenReturn("/web_eurekabank_restful_java");
  }

  @Test
  void withoutSessionRedirectsToLogin() throws Exception {
    when(request.getSession(false)).thenReturn(null);

    controller.doGet(request, response);

    verify(response).sendRedirect("/web_eurekabank_restful_java/login");
    verify(dispatcher, never()).forward(any(ServletRequest.class), any(ServletResponse.class));
  }

  @Test
  void sessionWithoutTokenRedirectsToLogin() throws Exception {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("token")).thenReturn(null);

    controller.doGet(request, response);

    verify(response).sendRedirect("/web_eurekabank_restful_java/login");
    verify(dispatcher, never()).forward(any(ServletRequest.class), any(ServletResponse.class));
  }

  @Test
  void sessionWithBlankTokenRedirectsToLogin() throws Exception {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("token")).thenReturn("  ");

    controller.doGet(request, response);

    verify(response).sendRedirect("/web_eurekabank_restful_java/login");
    verify(dispatcher, never()).forward(any(ServletRequest.class), any(ServletResponse.class));
  }

  @Test
  void sessionWithTokenShowsMenuViewWithoutCache() throws Exception {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("token")).thenReturn("abc123");

    controller.doGet(request, response);

    verify(response).setHeader("Cache-Control", "no-store");
    verify(dispatcher).forward(request, response);
    verify(response, never()).sendRedirect(anyString());
  }
}
