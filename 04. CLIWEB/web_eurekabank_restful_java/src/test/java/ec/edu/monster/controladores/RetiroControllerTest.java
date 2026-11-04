package ec.edu.monster.controladores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ec.edu.monster.modelos.dto.EstadosEnum;
import ec.edu.monster.modelos.dto.MovimientoResultado;
import ec.edu.monster.modelos.servicios.MovimientoService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

/**
 * Tests the withdrawal controller through doGet/doPost with Mockito mocks;
 * the REST model (MovimientoService) is injected as a mock.
 */
class RetiroControllerTest {
  private static final String VISTA = "/WEB-INF/ec/edu/monster/vista/retiro.jsp";
  private static final String CTX = "/web_eurekabank_restful_java";
  // Surrounding spaces on purpose: the key must reach the service exactly as typed
  private static final String CLAVE = " cl@ve 1 ";

  private final MovimientoService service = mock(MovimientoService.class);
  private final RetiroController controller = new RetiroController(service);
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final RequestDispatcher dispatcher = mock(RequestDispatcher.class);
  private final HttpSession session = mock(HttpSession.class);

  @BeforeEach
  void setUp() {
    when(request.getRequestDispatcher(VISTA)).thenReturn(dispatcher);
    when(request.getContextPath()).thenReturn(CTX);
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("token")).thenReturn("abc123");
  }

  private void params(String paso, String cuenta, String importe, String clave) {
    when(request.getParameter("paso")).thenReturn(paso);
    when(request.getParameter("cuenta")).thenReturn(cuenta);
    when(request.getParameter("importe")).thenReturn(importe);
    when(request.getParameter("clave")).thenReturn(clave);
  }

  private static MovimientoResultado ok(String mensaje, int numero, String saldo) {
    return new MovimientoResultado(EstadosEnum.OK, mensaje, numero,
        saldo == null ? null : new BigDecimal(saldo));
  }

  // The key is never put in the request (so never rendered) nor in the session
  private void verifyKeyNeverKept() {
    verify(request, never()).setAttribute(eq("clave"), any());
    verify(request, never()).setAttribute(anyString(), eq(CLAVE));
    verify(session, never()).setAttribute(eq("clave"), any());
    verify(session, never()).setAttribute(anyString(), eq(CLAVE));
  }

  @Test
  void withoutSessionRedirectsToLogin() throws Exception {
    when(request.getSession(false)).thenReturn(null);

    controller.doGet(request, response);
    controller.doPost(request, response);

    verify(response, times(2)).sendRedirect(CTX + "/login");
    verify(dispatcher, never()).forward(any(ServletRequest.class), any(ServletResponse.class));
    verifyNoInteractions(service);
  }

  @Test
  void sessionWithBlankTokenRedirectsToLogin() throws Exception {
    when(session.getAttribute("token")).thenReturn(" ");

    controller.doPost(request, response);

    verify(response).sendRedirect(CTX + "/login");
    verify(dispatcher, never()).forward(any(ServletRequest.class), any(ServletResponse.class));
    verifyNoInteractions(service);
  }

  @Test
  void getShowsEmptyFormWithoutCache() throws Exception {
    controller.doGet(request, response);

    verify(response).setHeader("Cache-Control", "no-store");
    verify(request).setAttribute("vista", "formulario");
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }

  @ParameterizedTest
  @ValueSource(strings = {"NaN", "abc", "0", "1.234", "-5"})
  void revisarWithInvalidAmountShowsErrorsAndKeepsValues(String importe) throws Exception {
    params("revisar", "00100001", importe, null);

    controller.doPost(request, response);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> errores = ArgumentCaptor.forClass(Map.class);
    verify(request).setAttribute(eq("errores"), errores.capture());
    assertEquals(Map.of("importe",
        "El importe debe ser mayor que cero y tener como máximo dos decimales"),
        errores.getValue());
    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute("cuenta", "00100001");
    verify(request).setAttribute("importe", importe);
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }

  @Test
  void revisarValidShowsConfirmationWithoutCallingService() throws Exception {
    params("revisar", " 00100001 ", "150", null);

    controller.doPost(request, response);

    verify(request).setAttribute("vista", "confirmacion");
    verify(request).setAttribute("cuenta", "00100001");
    verify(request).setAttribute("importe", "150");
    verify(request).setAttribute("importeFormateado", "$ 150.00");
    verify(request, never()).setAttribute(eq("errores"), any());
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }

  @Test
  void editarReturnsToFormKeepingValuesButNotTheKey() throws Exception {
    params("editar", "00100001", "150", CLAVE);

    controller.doPost(request, response);

    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute("cuenta", "00100001");
    verify(request).setAttribute("importe", "150");
    verify(dispatcher).forward(request, response);
    verifyKeyNeverKept();
    verifyNoInteractions(service);
  }

  @Test
  void confirmarRevalidatesHiddenFields() throws Exception {
    params("confirmar", "00100001", "-5", CLAVE);

    controller.doPost(request, response);

    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute(eq("errores"), any());
    verify(dispatcher).forward(request, response);
    verifyKeyNeverKept();
    verifyNoInteractions(service);
    verify(response, never()).sendRedirect(anyString());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "   "})
  void confirmarWithBlankKeyShowsConfirmationAgainWithoutCallingService(String clave)
      throws Exception {
    params("confirmar", "00100001", "150", clave);

    controller.doPost(request, response);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> errores = ArgumentCaptor.forClass(Map.class);
    verify(request).setAttribute(eq("errores"), errores.capture());
    assertEquals(Map.of("clave", "Ingrese la clave de la cuenta"), errores.getValue());
    verify(request).setAttribute("vista", "confirmacion");
    verify(request).setAttribute("cuenta", "00100001");
    verify(request).setAttribute("importe", "150");
    verify(request).setAttribute("importeFormateado", "$ 150.00");
    verify(request, never()).setAttribute(eq("clave"), any());
    verify(dispatcher).forward(request, response);
    verify(response, never()).sendRedirect(anyString());
    verifyNoInteractions(service);
  }

  @Test
  void confirmarOkPassesKeyAsTypedStoresReceiptAndRedirects() throws Exception {
    params("confirmar", "00100001", "150,5", CLAVE);
    when(service.retiro("abc123", "00100001", new BigDecimal("150.5"), CLAVE))
        .thenReturn(ok("Retiro registrado", 42, "849.5"));

    controller.doPost(request, response);

    verify(service, times(1)).retiro("abc123", "00100001", new BigDecimal("150.5"), CLAVE);
    verify(service, never()).deposito(any(), any(), any());
    verify(service, never()).transferencia(any(), any(), any(), any(), any());
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> comprobante = ArgumentCaptor.forClass(Map.class);
    verify(session).setAttribute(eq("comprobanteRetiro"), comprobante.capture());
    Map<String, String> c = comprobante.getValue();
    assertEquals("Retiro", c.get("operacion"));
    assertEquals("00100001", c.get("cuenta"));
    assertEquals("$ 150.50", c.get("importe"));
    assertEquals("42", c.get("idMovimiento"));
    assertEquals("$ 849.50", c.get("nuevoSaldo"));
    assertEquals("Retiro registrado", c.get("mensaje"));
    assertNotNull(c.get("fecha"));
    assertFalse(c.containsKey("clave"));
    assertFalse(c.containsValue(CLAVE));
    verifyKeyNeverKept();
    verify(response).sendRedirect(CTX + "/retiro");
    verify(dispatcher, never()).forward(any(ServletRequest.class), any(ServletResponse.class));
    verify(session, never()).invalidate();
  }

  @Test
  void confirmarOkWithoutBalanceOmitsItFromReceipt() throws Exception {
    params("confirmar", "00100001", "150", CLAVE);
    when(service.retiro("abc123", "00100001", new BigDecimal("150"), CLAVE))
        .thenReturn(ok("Retiro registrado", 42, null));

    controller.doPost(request, response);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> comprobante = ArgumentCaptor.forClass(Map.class);
    verify(session).setAttribute(eq("comprobanteRetiro"), comprobante.capture());
    assertFalse(comprobante.getValue().containsKey("nuevoSaldo"));
    verify(response).sendRedirect(CTX + "/retiro");
  }

  @Test
  void confirmarErrorShowsServerMessageAndKeepsValuesButNotTheKey() throws Exception {
    params("confirmar", "00100001", "150", CLAVE);
    when(service.retiro("abc123", "00100001", new BigDecimal("150"), CLAVE))
        .thenReturn(MovimientoResultado.error("Clave incorrecta"));

    controller.doPost(request, response);

    verify(request).setAttribute("error", "Clave incorrecta");
    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute("cuenta", "00100001");
    verify(request).setAttribute("importe", "150");
    verifyKeyNeverKept();
    verify(session, never()).setAttribute(eq("comprobanteRetiro"), any());
    verify(session, never()).invalidate();
    verify(response, never()).sendRedirect(anyString());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void confirmarExpiredSessionInvalidatesAndRedirectsToLogin() throws Exception {
    params("confirmar", "00100001", "150", CLAVE);
    when(service.retiro("abc123", "00100001", new BigDecimal("150"), CLAVE))
        .thenReturn(MovimientoResultado.fallo(EstadosEnum.SESION_EXPIRADA, "Token inválido"));

    controller.doPost(request, response);

    verify(session).invalidate();
    verify(response).sendRedirect(CTX + "/login?expirada=1");
    verify(dispatcher, never()).forward(any(ServletRequest.class), any(ServletResponse.class));
    verify(session, never()).setAttribute(eq("comprobanteRetiro"), any());
    verify(request, never()).setAttribute(eq("error"), any());
    verifyKeyNeverKept();
  }

  @Test
  void getAfterSuccessShowsReceiptAndClearsFlash() throws Exception {
    Map<String, String> comprobante = Map.of("operacion", "Retiro");
    when(session.getAttribute("comprobanteRetiro")).thenReturn(comprobante);

    controller.doGet(request, response);

    verify(request).setAttribute("comprobante", comprobante);
    verify(request).setAttribute("vista", "comprobante");
    verify(session).removeAttribute("comprobanteRetiro");
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }
}
