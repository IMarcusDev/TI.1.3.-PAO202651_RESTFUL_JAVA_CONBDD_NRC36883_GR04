package ec.edu.monster.filtros;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
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
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * CsrfFilter (synchronizer token): every POST must send the session token in "_csrf".
 * GET requests only make sure the token exists so the forms can render it.
 */
class CsrfFilterTest {
  private static final String CTX = "/web_eurekabank_restful_java";

  private final CsrfFilter filter = new CsrfFilter();
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final FilterChain chain = mock(FilterChain.class);

  // Session mock backed by a real map, so the token stored by the filter can be read back
  private static HttpSession newSession() {
    Map<String, Object> attributes = new HashMap<>();
    HttpSession session = mock(HttpSession.class);
    when(session.getAttribute(anyString())).thenAnswer(inv -> attributes.get(inv.<String>getArgument(0)));
    doAnswer(inv -> attributes.put(inv.getArgument(0), inv.getArgument(1)))
        .when(session).setAttribute(anyString(), any());
    return session;
  }

  private void givenRequest(String method, String path, HttpSession session) {
    when(request.getMethod()).thenReturn(method);
    when(request.getServletPath()).thenReturn(path);
    when(request.getContextPath()).thenReturn(CTX);
    when(request.getSession(false)).thenReturn(session);
  }

  private static String tokenOf(HttpSession session) {
    return (String) session.getAttribute(CsrfFilter.SESSION_ATTRIBUTE);
  }

  private void assertPassesThrough() throws Exception {
    verify(chain).doFilter(request, response);
    verify(response, never()).sendError(anyInt());
    verify(response, never()).sendRedirect(anyString());
  }

  private void assertChainNeverCalled() throws Exception {
    verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
  }

  @Test
  void namesArePublicConstants() {
    assertEquals("csrfToken", CsrfFilter.SESSION_ATTRIBUTE);
    assertEquals("_csrf", CsrfFilter.PARAMETER);
  }

  @Test
  void getLoginWithoutSessionCreatesSessionAndToken() throws Exception {
    HttpSession session = newSession();
    givenRequest("GET", "/login", null);
    when(request.getSession(true)).thenReturn(session);

    filter.doFilter(request, response, chain);

    verify(request).getSession(true);
    String token = tokenOf(session);
    assertNotNull(token);
    assertFalse(token.isBlank());
    // 32 bytes, Base64 URL-safe without padding -> 43 chars
    assertTrue(token.matches("[A-Za-z0-9_-]{43}"), token);
    assertPassesThrough();
  }

  @Test
  void getMenuWithSessionWithoutTokenStoresToken() throws Exception {
    HttpSession session = newSession();
    givenRequest("GET", "/menu", session);

    filter.doFilter(request, response, chain);

    assertNotNull(tokenOf(session));
    assertPassesThrough();
  }

  @Test
  void getMenuWithoutSessionDoesNotCreateOne() throws Exception {
    givenRequest("GET", "/menu", null);

    filter.doFilter(request, response, chain);

    verify(request, never()).getSession(true);
    verify(request, never()).getSession();
    assertPassesThrough();
  }

  @Test
  void tokenIsReusedOnSecondGet() throws Exception {
    HttpSession session = newSession();
    givenRequest("GET", "/retiro", session);

    filter.doFilter(request, response, chain);
    String first = tokenOf(session);
    filter.doFilter(request, response, chain);

    assertEquals(first, tokenOf(session));
  }

  @Test
  void differentSessionsGetDifferentTokens() throws Exception {
    HttpSession one = newSession();
    HttpSession two = newSession();

    givenRequest("GET", "/menu", one);
    filter.doFilter(request, response, chain);
    givenRequest("GET", "/menu", two);
    filter.doFilter(request, response, chain);

    assertNotEquals(tokenOf(one), tokenOf(two));
  }

  @Test
  void postWithMatchingTokenPasses() throws Exception {
    HttpSession session = newSession();
    session.setAttribute(CsrfFilter.SESSION_ATTRIBUTE, "abc123");
    givenRequest("POST", "/retiro", session);
    when(request.getParameter(CsrfFilter.PARAMETER)).thenReturn("abc123");

    filter.doFilter(request, response, chain);

    assertPassesThrough();
  }

  @ParameterizedTest
  @ValueSource(strings = {"/retiro", "/transferencia", "/deposito", "/logout", "/movimientos"})
  void postWithMissingTokenIsForbidden(String path) throws Exception {
    assertPostForbidden(path, null);
  }

  @ParameterizedTest
  @ValueSource(strings = {"/retiro", "/transferencia", "/deposito", "/logout", "/movimientos"})
  void postWithBlankTokenIsForbidden(String path) throws Exception {
    assertPostForbidden(path, " ");
  }

  @ParameterizedTest
  @ValueSource(strings = {"/retiro", "/transferencia", "/deposito", "/logout", "/movimientos"})
  void postWithWrongTokenIsForbidden(String path) throws Exception {
    assertPostForbidden(path, "otro-token");
  }

  private void assertPostForbidden(String path, String sent) throws Exception {
    HttpSession session = newSession();
    session.setAttribute(CsrfFilter.SESSION_ATTRIBUTE, "abc123");
    givenRequest("POST", path, session);
    when(request.getParameter(CsrfFilter.PARAMETER)).thenReturn(sent);

    filter.doFilter(request, response, chain);

    verify(response).sendError(403);
    assertChainNeverCalled();
    // The session token is not changed by a rejected request
    assertEquals("abc123", tokenOf(session));
  }

  @Test
  void postWithoutSessionIsForbidden() throws Exception {
    givenRequest("POST", "/retiro", null);
    when(request.getParameter(CsrfFilter.PARAMETER)).thenReturn("abc123");

    filter.doFilter(request, response, chain);

    verify(response).sendError(403);
    verify(request, never()).getSession(true);
    assertChainNeverCalled();
  }

  @Test
  void postWithSessionWithoutTokenIsForbidden() throws Exception {
    givenRequest("POST", "/deposito", newSession());
    when(request.getParameter(CsrfFilter.PARAMETER)).thenReturn("abc123");

    filter.doFilter(request, response, chain);

    verify(response).sendError(403);
    assertChainNeverCalled();
  }

  @Test
  void postLoginWithWrongTokenRedirectsToLogin() throws Exception {
    HttpSession session = newSession();
    session.setAttribute(CsrfFilter.SESSION_ATTRIBUTE, "abc123");
    givenRequest("POST", "/login", session);
    when(request.getParameter(CsrfFilter.PARAMETER)).thenReturn("otro-token");

    filter.doFilter(request, response, chain);

    verify(response).sendRedirect(CTX + "/login");
    verify(response, never()).sendError(anyInt());
    assertChainNeverCalled();
  }

  @Test
  void postLoginWithoutSessionRedirectsToLogin() throws Exception {
    givenRequest("POST", "/login", null);
    when(request.getParameter(CsrfFilter.PARAMETER)).thenReturn("abc123");

    filter.doFilter(request, response, chain);

    verify(response).sendRedirect(CTX + "/login");
    assertChainNeverCalled();
  }

  @Test
  void postLoginWithMatchingTokenPasses() throws Exception {
    HttpSession session = newSession();
    session.setAttribute(CsrfFilter.SESSION_ATTRIBUTE, "abc123");
    givenRequest("POST", "/login", session);
    when(request.getParameter(CsrfFilter.PARAMETER)).thenReturn("abc123");

    filter.doFilter(request, response, chain);

    assertPassesThrough();
  }

  // Edge cases suggested by the independent verification

  @Test
  void postLoginWithoutSessionNeverCreatesOne() throws Exception {
    givenRequest("POST", "/login", null);
    when(request.getParameter(CsrfFilter.PARAMETER)).thenReturn("abc123");

    filter.doFilter(request, response, chain);

    verify(request, never()).getSession(true);
    verify(request, never()).getSession();
    assertChainNeverCalled();
  }

  @Test
  void postMenuWithoutTokenIsForbidden() throws Exception {
    assertPostForbidden("/menu", null);
  }

  @Test
  void lowercasePostIsStillChecked() throws Exception {
    HttpSession session = newSession();
    session.setAttribute(CsrfFilter.SESSION_ATTRIBUTE, "abc123");
    givenRequest("post", "/retiro", session);

    filter.doFilter(request, response, chain);

    verify(response).sendError(403);
    assertChainNeverCalled();
  }

  @Test
  void nonStringSessionTokenIsTreatedAsMissing() throws Exception {
    HttpSession session = newSession();
    session.setAttribute(CsrfFilter.SESSION_ATTRIBUTE, 42);
    givenRequest("POST", "/deposito", session);
    when(request.getParameter(CsrfFilter.PARAMETER)).thenReturn("42");

    filter.doFilter(request, response, chain);

    verify(response).sendError(403);
    assertChainNeverCalled();
  }
}
