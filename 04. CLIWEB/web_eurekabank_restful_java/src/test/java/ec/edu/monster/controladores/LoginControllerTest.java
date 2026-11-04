package ec.edu.monster.controladores;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ec.edu.monster.modelos.dto.EstadosEnum;
import ec.edu.monster.modelos.dto.LoginResultado;
import ec.edu.monster.modelos.servicios.LoginService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the controller through its public doGet/doPost, with a fake browser
 * and a mocked LoginService (Mockito mocks).
 */
class LoginControllerTest {
  private static final String VISTA = "/WEB-INF/ec/edu/monster/vista/login.jsp";
  private static final String AVISO =
      "Su sesión expiró o no es válida. Inicie sesión nuevamente.";
  private static final String TOKEN = "token-de-prueba";

  private final LoginService loginService = mock(LoginService.class);
  private final LoginController controller = new LoginController(loginService);
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final RequestDispatcher dispatcher = mock(RequestDispatcher.class);
  private final HttpSession oldSession = mock(HttpSession.class);
  private final HttpSession newSession = mock(HttpSession.class);

  @BeforeEach
  void setUp() {
    when(request.getRequestDispatcher(VISTA)).thenReturn(dispatcher);
    when(request.getContextPath()).thenReturn("/web_eurekabank_restful_java");
    when(request.getSession()).thenReturn(oldSession);
    when(request.getSession(true)).thenReturn(newSession);
  }

  @Test
  void getShowsLoginViewWithoutNotice() throws Exception {
    controller.doGet(request, response);

    verify(dispatcher).forward(request, response);
    verify(request, never()).setAttribute(eq("aviso"), any());
    verify(response, never()).sendRedirect(anyString());
    verifyNoInteractions(loginService);
  }

  @Test
  void getWithExpiredSessionShowsFixedNotice() throws Exception {
    when(request.getParameter("expirada")).thenReturn("1");

    controller.doGet(request, response);

    verify(request).setAttribute("aviso", AVISO);
    verify(dispatcher).forward(request, response);
    verify(response, never()).sendRedirect(anyString());
    verifyNoInteractions(loginService);
  }

  // Only the exact flag shows the notice; the parameter value is never echoed
  @Test
  void getWithOtherExpiredValueShowsNoNotice() throws Exception {
    when(request.getParameter("expirada")).thenReturn("<script>alert(1)</script>");

    controller.doGet(request, response);

    verify(request, never()).setAttribute(eq("aviso"), any());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void blankFieldsShowErrorWithoutCallingServerOrCreatingSession() throws Exception {
    when(request.getParameter("username")).thenReturn("admin");
    when(request.getParameter("password")).thenReturn("  ");

    controller.doPost(request, response);

    verify(request).setAttribute("error", "Ingrese usuario y contraseña");
    verify(request).setAttribute("username", "admin");
    verify(dispatcher).forward(request, response);
    verify(request, never()).getSession(anyBoolean());
    verify(response, never()).sendRedirect(anyString());
    verifyNoInteractions(loginService);
  }

  @Test
  void wrongCredentialsShowServerMessageWithoutCreatingSession() throws Exception {
    when(request.getParameter("username")).thenReturn("admin");
    when(request.getParameter("password")).thenReturn("mala");
    when(loginService.login("admin", "mala"))
        .thenReturn(LoginResultado.error("Contraseña incorrecta"));

    controller.doPost(request, response);

    verify(loginService).login("admin", "mala");
    verify(request).setAttribute("error", "Contraseña incorrecta");
    verify(request).setAttribute("username", "admin");
    verify(dispatcher).forward(request, response);
    verify(request, never()).getSession(anyBoolean());
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  void validCredentialsStoreUserInNewSessionAndRedirectToMenu() throws Exception {
    when(request.getParameter("username")).thenReturn(" admin ");
    when(request.getParameter("password")).thenReturn("1234");
    when(loginService.login("admin", "1234")).thenReturn(new LoginResultado(
        EstadosEnum.OK, "Ingreso de sesión exitoso", TOKEN, "Administrador", "Empleado"));

    controller.doPost(request, response);

    // Username trimmed, password sent as typed
    verify(loginService).login("admin", "1234");
    // Old session discarded, data saved in the new one
    verify(oldSession).invalidate();
    verify(newSession).setAttribute("token", TOKEN);
    verify(newSession).setAttribute("username", "admin");
    verify(newSession).setAttribute("nombre", "Administrador");
    verify(newSession).setAttribute("tipoUsuario", "Empleado");
    // Post/Redirect/Get: redirect, never forward to the login view
    verify(response).sendRedirect("/web_eurekabank_restful_java/menu");
    verify(dispatcher, never()).forward(request, response);
  }
}
