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
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

/**
 * Tests the deposit controller through doGet/doPost with Mockito mocks;
 * the REST model (MovimientoService) is injected as a mock.
 */
class DepositoControllerTest {
  private static final String VISTA = "/WEB-INF/ec/edu/monster/vista/deposito.jsp";
  private static final String CTX = "/web_eurekabank_restful_java";

  private final MovimientoService service = mock(MovimientoService.class);
  private final DepositoController controller = new DepositoController(service);
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
    when(session.getAttribute("username")).thenReturn("jperez");
  }

  private void params(String paso, String cuenta, String importe) {
    when(request.getParameter("paso")).thenReturn(paso);
    when(request.getParameter("cuenta")).thenReturn(cuenta);
    when(request.getParameter("importe")).thenReturn(importe);
  }

  private static MovimientoResultado ok(String mensaje, int numero, String saldo) {
    return new MovimientoResultado(EstadosEnum.OK, mensaje, numero,
        saldo == null ? null : new BigDecimal(saldo));
  }

  @Test
  void mappedExactlyToDepositoPath() {
    WebServlet mapping = DepositoController.class.getAnnotation(WebServlet.class);

    assertNotNull(mapping);
    assertEquals(1, mapping.urlPatterns().length);
    assertEquals("/deposito", mapping.urlPatterns()[0]);
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

  @Test
  void revisarValidShowsConfirmationWithoutEmployeeField() throws Exception {
    params("revisar", " 0004 ", "100");

    controller.doPost(request, response);

    verify(request).setAttribute("vista", "confirmacion");
    verify(request).setAttribute("cuenta", "0004");
    verify(request).setAttribute("importe", "100");
    verify(request).setAttribute("importeFormateado", "$ 100.00");
    // The server takes the employee from the token: the form never asks for it
    verify(request, never()).getParameter("empleado");
    verify(request, never()).setAttribute(eq("empleado"), any());
    verify(request, never()).setAttribute(eq("errores"), any());
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }

  @Test
  void revisarWithBlankAccountShowsFieldErrorAndKeepsValues() throws Exception {
    params("revisar", null, "100");

    controller.doPost(request, response);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> errores = ArgumentCaptor.forClass(Map.class);
    verify(request).setAttribute(eq("errores"), errores.capture());
    assertEquals(Map.of("cuenta", "Ingrese la cuenta de destino"), errores.getValue());
    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute("cuenta", null);
    verify(request).setAttribute("importe", "100");
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }

  @ParameterizedTest
  @ValueSource(strings = {"NaN", "abc", "0", "1.234", "-5"})
  void revisarWithInvalidAmountShowsErrorsAndKeepsValues(String importe) throws Exception {
    params("revisar", "0004", importe);

    controller.doPost(request, response);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> errores = ArgumentCaptor.forClass(Map.class);
    verify(request).setAttribute(eq("errores"), errores.capture());
    assertEquals(Map.of("importe",
        "El importe debe ser mayor que cero y tener como máximo dos decimales"),
        errores.getValue());
    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute("cuenta", "0004");
    verify(request).setAttribute("importe", importe);
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }

  @Test
  void editarReturnsToFormKeepingValues() throws Exception {
    params("editar", "0004", "100");

    controller.doPost(request, response);

    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute("cuenta", "0004");
    verify(request).setAttribute("importe", "100");
    verify(request, never()).setAttribute(eq("errores"), any());
    verify(request, never()).setAttribute(eq("error"), any());
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }

  @Test
  void confirmarRevalidatesHiddenFields() throws Exception {
    params("confirmar", "0004", "-5");

    controller.doPost(request, response);

    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute(eq("errores"), any());
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  void confirmarOkCallsServiceOnceStoresReceiptAndRedirects() throws Exception {
    params("confirmar", "0004", "100");
    when(service.deposito("abc123", "0004", new BigDecimal("100")))
        .thenReturn(ok("Depósito registrado", 7, "1350.5"));

    controller.doPost(request, response);

    verify(service, times(1)).deposito("abc123", "0004", new BigDecimal("100"));
    verify(service, never()).retiro(any(), any(), any(), any());
    verify(service, never()).transferencia(any(), any(), any(), any(), any());
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> comprobante = ArgumentCaptor.forClass(Map.class);
    verify(session).setAttribute(eq("comprobanteDeposito"), comprobante.capture());
    Map<String, String> c = comprobante.getValue();
    assertEquals("Depósito", c.get("operacion"));
    // The receipt shows who is logged in: the server took the employee from the token
    assertEquals("jperez", c.get("empleado"));
    assertEquals("0004", c.get("cuenta"));
    assertEquals("$ 100.00", c.get("importe"));
    assertEquals("7", c.get("idMovimiento"));
    assertEquals("$ 1,350.50", c.get("nuevoSaldo"));
    assertEquals("Depósito registrado", c.get("mensaje"));
    assertNotNull(c.get("fecha"));
    verify(response).sendRedirect(CTX + "/deposito");
    verify(dispatcher, never()).forward(any(ServletRequest.class), any(ServletResponse.class));
    verify(session, never()).invalidate();
  }

  @Test
  void confirmarOkWithoutBalanceOmitsItFromReceipt() throws Exception {
    params("confirmar", "0004", "100");
    when(service.deposito("abc123", "0004", new BigDecimal("100")))
        .thenReturn(ok("Depósito registrado", 7, null));

    controller.doPost(request, response);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> comprobante = ArgumentCaptor.forClass(Map.class);
    verify(session).setAttribute(eq("comprobanteDeposito"), comprobante.capture());
    assertFalse(comprobante.getValue().containsKey("nuevoSaldo"));
    assertEquals("7", comprobante.getValue().get("idMovimiento"));
    verify(response).sendRedirect(CTX + "/deposito");
  }

  @ParameterizedTest
  @ValueSource(strings = {"Cuenta inexistente",
      "No se pudo conectar con el servidor: http://localhost:8080/api"})
  void confirmarErrorShowsServerMessageAndKeepsValues(String mensaje) throws Exception {
    params("confirmar", "0004", "100");
    when(service.deposito("abc123", "0004", new BigDecimal("100")))
        .thenReturn(MovimientoResultado.error(mensaje));

    controller.doPost(request, response);

    verify(request).setAttribute("error", mensaje);
    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute("cuenta", "0004");
    verify(request).setAttribute("importe", "100");
    verify(session, never()).setAttribute(eq("comprobanteDeposito"), any());
    verify(session, never()).invalidate();
    verify(response, never()).sendRedirect(anyString());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void confirmarExpiredSessionInvalidatesAndRedirectsToLogin() throws Exception {
    params("confirmar", "0004", "100");
    when(service.deposito("abc123", "0004", new BigDecimal("100")))
        .thenReturn(MovimientoResultado.fallo(EstadosEnum.SESION_EXPIRADA, "Token inválido"));

    controller.doPost(request, response);

    verify(session).invalidate();
    verify(response).sendRedirect(CTX + "/login?expirada=1");
    verify(dispatcher, never()).forward(any(ServletRequest.class), any(ServletResponse.class));
    verify(session, never()).setAttribute(eq("comprobanteDeposito"), any());
    verify(request, never()).setAttribute(eq("error"), any());
  }

  @Test
  void getAfterSuccessShowsReceiptAndClearsFlash() throws Exception {
    Map<String, String> comprobante = Map.of("operacion", "Depósito");
    when(session.getAttribute("comprobanteDeposito")).thenReturn(comprobante);

    controller.doGet(request, response);

    verify(request).setAttribute("comprobante", comprobante);
    verify(request).setAttribute("vista", "comprobante");
    verify(session).removeAttribute("comprobanteDeposito");
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }
}
