package ec.edu.monster.filtros;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * AuthFilter runs before the protected controllers:
 * no login -> /login, a Cliente on an employee-only screen -> /menu.
 */
class AuthFilterTest {
  private static final String CTX = "/web_eurekabank_restful_java";

  private final AuthFilter filter = new AuthFilter();
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final FilterChain chain = mock(FilterChain.class);
  private final HttpSession session = mock(HttpSession.class);

  // Simulates the request: path, and the session of a logged-in user (or none)
  private void givenRequest(String path, String token, String tipoUsuario) {
    when(request.getServletPath()).thenReturn(path);
    when(request.getContextPath()).thenReturn(CTX);
    if (token == null) {
      when(request.getSession(false)).thenReturn(null);
    } else {
      when(request.getSession(false)).thenReturn(session);
      when(session.getAttribute("token")).thenReturn(token);
      when(session.getAttribute("tipoUsuario")).thenReturn(tipoUsuario);
    }
  }

  private void assertBlockedTo(String route) throws Exception {
    verify(response).sendRedirect(CTX + route);
    verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
  }

  private void assertPassesThrough() throws Exception {
    verify(chain).doFilter(request, response);
    verify(response, never()).sendRedirect(anyString());
  }

  @ParameterizedTest
  @ValueSource(strings = {"/menu", "/retiro", "/transferencia", "/deposito", "/movimientos"})
  void withoutSessionGoesToLogin(String path) throws Exception {
    givenRequest(path, null, null);
    filter.doFilter(request, response, chain);
    assertBlockedTo("/login");
  }

  @ParameterizedTest
  @ValueSource(strings = {"/menu", "/retiro", "/transferencia", "/deposito", "/movimientos"})
  void blankTokenGoesToLogin(String path) throws Exception {
    givenRequest(path, " ", "Empleado");
    filter.doFilter(request, response, chain);
    assertBlockedTo("/login");
  }

  @Test
  void clienteCannotOpenDeposito() throws Exception {
    givenRequest("/deposito", "token", "Cliente");
    filter.doFilter(request, response, chain);
    assertBlockedTo("/menu");
  }

  @ParameterizedTest
  @ValueSource(strings = {"/menu", "/retiro", "/transferencia", "/movimientos"})
  void clienteAllowedScreens(String path) throws Exception {
    givenRequest(path, "token", "Cliente");
    filter.doFilter(request, response, chain);
    assertPassesThrough();
  }

  @ParameterizedTest
  @ValueSource(strings = {"/deposito"})
  void sessionWithoutRoleCannotOpenEmployeeScreens(String path) throws Exception {
    givenRequest(path, "token", null);
    filter.doFilter(request, response, chain);
    assertBlockedTo("/menu");
  }

  @Test
  void sessionWithoutTokenGoesToLogin() throws Exception {
    givenRequest("/retiro", "token", "Empleado");
    when(session.getAttribute("token")).thenReturn(null);
    filter.doFilter(request, response, chain);
    assertBlockedTo("/login");
  }

  // The role name must match the server value exactly
  @ParameterizedTest
  @ValueSource(strings = {"empleado", "EMPLEADO", " Empleado"})
  void roleMustMatchExactly(String tipoUsuario) throws Exception {
    givenRequest("/deposito", "token", tipoUsuario);
    filter.doFilter(request, response, chain);
    assertBlockedTo("/menu");
  }

  @ParameterizedTest
  @ValueSource(strings = {"Cliente", "Empleado"})
  void anyLoggedUserOpensMenu(String tipoUsuario) throws Exception {
    givenRequest("/menu", "token", tipoUsuario);
    filter.doFilter(request, response, chain);
    assertPassesThrough();
  }

  @ParameterizedTest
  @ValueSource(strings = {"/retiro", "/transferencia", "/deposito", "/movimientos"})
  void empleadoOpensEmployeeScreens(String path) throws Exception {
    givenRequest(path, "token", "Empleado");
    filter.doFilter(request, response, chain);
    assertPassesThrough();
  }
}
