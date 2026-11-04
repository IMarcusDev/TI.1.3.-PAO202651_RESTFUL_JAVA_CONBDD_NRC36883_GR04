package ec.edu.monster.filtros;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF protection with the synchronizer token pattern.
 * 1. GET (and any non-POST): make sure the session has a token so the forms can render it
 *    (partials/csrf.jspf). Only GET /login may create the session; other paths never do.
 * 2. POST: the "_csrf" parameter must equal the session token, otherwise the request is
 *    rejected before the controller runs (POST /login -> fresh login form, others -> 403).
 *
 * The token lives as long as the session (no per-request rotation), so multi-step forms
 * and the browser Back button keep working. A new session after login gets a new token.
 */
@WebFilter(filterName = "CsrfFilter",
    urlPatterns = {"/login", "/logout", "/menu", "/retiro", "/transferencia", "/deposito"})
public class CsrfFilter extends HttpFilter {
  public static final String SESSION_ATTRIBUTE = "csrfToken";
  public static final String PARAMETER = "_csrf";

  private static final int TOKEN_BYTES = 32;
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

  private static final String RUTA_LOGIN = "/login";

  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    boolean isLogin = RUTA_LOGIN.equals(request.getServletPath());

    if ("POST".equalsIgnoreCase(request.getMethod())) {
      if (!isValid(request)) {
        if (isLogin) {
          // Expired or forged login form: show a fresh one
          response.sendRedirect(request.getContextPath() + RUTA_LOGIN);
        } else {
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
        return;
      }
    } else {
      // The login form needs a session to hold its token; other screens only reuse one
      HttpSession session = isLogin ? request.getSession(true) : request.getSession(false);
      if (session != null && currentToken(session) == null) {
        session.setAttribute(SESSION_ATTRIBUTE, newToken());
      }
    }

    chain.doFilter(request, response);
  }

  private static boolean isValid(HttpServletRequest request) {
    // getSession(false): a POST without a session can never carry a valid token
    HttpSession session = request.getSession(false);
    if (session == null) {
      return false;
    }
    String expected = currentToken(session);
    String sent = request.getParameter(PARAMETER);
    if (expected == null || sent == null || sent.isBlank()) {
      return false;
    }
    // Constant-time comparison: do not leak how many characters matched
    return MessageDigest.isEqual(
        expected.getBytes(StandardCharsets.UTF_8), sent.getBytes(StandardCharsets.UTF_8));
  }

  private static String currentToken(HttpSession session) {
    Object token = session.getAttribute(SESSION_ATTRIBUTE);
    return token instanceof String value && !value.isBlank() ? value : null;
  }

  private static String newToken() {
    byte[] bytes = new byte[TOKEN_BYTES];
    RANDOM.nextBytes(bytes);
    return ENCODER.encodeToString(bytes);
  }
}
