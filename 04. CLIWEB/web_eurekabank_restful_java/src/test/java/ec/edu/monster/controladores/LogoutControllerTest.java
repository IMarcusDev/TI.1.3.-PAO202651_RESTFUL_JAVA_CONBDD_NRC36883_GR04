package ec.edu.monster.controladores;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ec.edu.monster.modelos.servicios.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

/**
 * Tests the logout controller through its public doPost, with a fake browser
 * and a mocked LoginService (Mockito mocks).
 */
class LogoutControllerTest {
  private final LoginService loginService = mock(LoginService.class);
  private final LogoutController controller = new LogoutController(loginService);
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final HttpSession session = mock(HttpSession.class);

  @BeforeEach
  void setUp() {
    when(request.getContextPath()).thenReturn("/web_eurekabank_restful_java");
  }

  @Test
  void withTokenLogsOutOnServerThenInvalidatesAndRedirectsToLogin() throws Exception {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("token")).thenReturn("abc123");

    controller.doPost(request, response);

    // Server logout first, while the token is still known; then the web session ends
    InOrder order = inOrder(loginService, session, response);
    order.verify(loginService, times(1)).logout("abc123");
    order.verify(session).invalidate();
    order.verify(response).sendRedirect("/web_eurekabank_restful_java/login");
  }

  @Test
  void sessionWithoutTokenInvalidatesWithoutCallingServer() throws Exception {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("token")).thenReturn(null);

    controller.doPost(request, response);

    verifyNoInteractions(loginService);
    verify(session).invalidate();
    verify(response).sendRedirect("/web_eurekabank_restful_java/login");
  }

  @Test
  void withoutSessionRedirectsWithoutCreatingOneOrCallingServer() throws Exception {
    when(request.getSession(false)).thenReturn(null);

    controller.doPost(request, response);

    verifyNoInteractions(loginService);
    verify(request, never()).getSession();
    verify(request, never()).getSession(true);
    verify(response).sendRedirect("/web_eurekabank_restful_java/login");
  }
}
