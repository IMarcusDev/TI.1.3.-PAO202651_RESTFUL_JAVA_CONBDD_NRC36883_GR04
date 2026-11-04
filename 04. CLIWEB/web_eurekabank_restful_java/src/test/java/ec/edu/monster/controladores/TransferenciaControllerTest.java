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
 * Tests the transfer controller through doGet/doPost with Mockito mocks;
 * the REST model (MovimientoService) is injected as a mock.
 */
class TransferenciaControllerTest {
  private static final String VISTA = "/WEB-INF/ec/edu/monster/vista/transferencia.jsp";
  private static final String CTX = "/web_eurekabank_restful_java";
  // Surrounding spaces on purpose: the key must reach the service exactly as typed
  private static final String CLAVE = " cl@ve 1 ";

  private final MovimientoService service = mock(MovimientoService.class);
  private final TransferenciaController controller = new TransferenciaController(service);
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

  private void params(String paso, String origen, String destino, String importe, String clave) {
    when(request.getParameter("paso")).thenReturn(paso);
    when(request.getParameter("origen")).thenReturn(origen);
    when(request.getParameter("destino")).thenReturn(destino);
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
  void getShowsEmptyFormWithoutCache() throws Exception {
    controller.doGet(request, response);

    verify(response).setHeader("Cache-Control", "no-store");
    verify(request).setAttribute("vista", "formulario");
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }

  @ParameterizedTest
  @ValueSource(strings = {"NaN", "abc", "0", "1.234", "-5"})
  void revisarWithInvalidAmountShowsErrors(String importe) throws Exception {
    params("revisar", "00100001", "00200002", importe, null);

    controller.doPost(request, response);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> errores = ArgumentCaptor.forClass(Map.class);
    verify(request).setAttribute(eq("errores"), errores.capture());
    assertEquals(Map.of("importe",
        "El importe debe ser mayor que cero y tener como máximo dos decimales"),
        errores.getValue());
    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute("origen", "00100001");
    verify(request).setAttribute("destino", "00200002");
    verify(request).setAttribute("importe", importe);
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }

  @Test
  void sameAccountIsRejectedWithoutCallingService() throws Exception {
    params("confirmar", "00100001", " 00100001", "10", CLAVE);

    controller.doPost(request, response);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> errores = ArgumentCaptor.forClass(Map.class);
    verify(request).setAttribute(eq("errores"), errores.capture());
    assertEquals(Map.of("destino", "La cuenta de origen y la de destino deben ser diferentes"),
        errores.getValue());
    verify(request).setAttribute("vista", "formulario");
    verify(dispatcher).forward(request, response);
    verifyKeyNeverKept();
    verifyNoInteractions(service);
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  void revisarValidShowsConfirmationWithoutCallingService() throws Exception {
    params("revisar", "00100001", "00200002", "150", null);

    controller.doPost(request, response);

    verify(request).setAttribute("vista", "confirmacion");
    verify(request).setAttribute("origen", "00100001");
    verify(request).setAttribute("destino", "00200002");
    verify(request).setAttribute("importe", "150");
    verify(request).setAttribute("importeFormateado", "$ 150.00");
    verify(request, never()).setAttribute(eq("errores"), any());
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }

  @Test
  void editarReturnsToFormKeepingValuesButNotTheKey() throws Exception {
    params("editar", "00100001", "00200002", "150", CLAVE);

    controller.doPost(request, response);

    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute("origen", "00100001");
    verify(request).setAttribute("destino", "00200002");
    verify(request).setAttribute("importe", "150");
    verify(dispatcher).forward(request, response);
    verifyKeyNeverKept();
    verifyNoInteractions(service);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "   "})
  void confirmarWithBlankKeyShowsConfirmationAgainWithoutCallingService(String clave)
      throws Exception {
    params("confirmar", "00100001", "00200002", "150", clave);

    controller.doPost(request, response);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> errores = ArgumentCaptor.forClass(Map.class);
    verify(request).setAttribute(eq("errores"), errores.capture());
    assertEquals(Map.of("clave", "Ingrese la clave de la cuenta"), errores.getValue());
    verify(request).setAttribute("vista", "confirmacion");
    verify(request).setAttribute("origen", "00100001");
    verify(request).setAttribute("destino", "00200002");
    verify(request).setAttribute("importe", "150");
    verify(request).setAttribute("importeFormateado", "$ 150.00");
    verify(request, never()).setAttribute(eq("clave"), any());
    verify(dispatcher).forward(request, response);
    verify(response, never()).sendRedirect(anyString());
    verifyNoInteractions(service);
  }

  @Test
  void confirmarOkPassesKeyAsTypedStoresReceiptAndRedirects() throws Exception {
    params("confirmar", "00100001", "00200002", "1250", CLAVE);
    when(service.transferencia("abc123", "00100001", "00200002", new BigDecimal("1250"), CLAVE))
        .thenReturn(ok("Transferencia registrada", 7, "3750"));

    controller.doPost(request, response);

    verify(service, times(1))
        .transferencia("abc123", "00100001", "00200002", new BigDecimal("1250"), CLAVE);
    verify(service, never()).retiro(any(), any(), any(), any());
    verify(service, never()).deposito(any(), any(), any());
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> comprobante = ArgumentCaptor.forClass(Map.class);
    verify(session).setAttribute(eq("comprobanteTransferencia"), comprobante.capture());
    Map<String, String> c = comprobante.getValue();
    assertEquals("Transferencia", c.get("operacion"));
    assertEquals("00100001", c.get("origen"));
    assertEquals("00200002", c.get("destino"));
    assertEquals("$ 1,250.00", c.get("importe"));
    assertEquals("7", c.get("idMovimiento"));
    assertEquals("$ 3,750.00", c.get("nuevoSaldo"));
    assertEquals("Transferencia registrada", c.get("mensaje"));
    assertNotNull(c.get("fecha"));
    assertFalse(c.containsKey("clave"));
    assertFalse(c.containsValue(CLAVE));
    verifyKeyNeverKept();
    verify(response).sendRedirect(CTX + "/transferencia");
    verify(dispatcher, never()).forward(any(ServletRequest.class), any(ServletResponse.class));
    verify(session, never()).invalidate();
  }

  @Test
  void confirmarOkWithoutBalanceOmitsItFromReceipt() throws Exception {
    params("confirmar", "00100001", "00200002", "150", CLAVE);
    when(service.transferencia("abc123", "00100001", "00200002", new BigDecimal("150"), CLAVE))
        .thenReturn(ok("Transferencia registrada", 7, null));

    controller.doPost(request, response);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> comprobante = ArgumentCaptor.forClass(Map.class);
    verify(session).setAttribute(eq("comprobanteTransferencia"), comprobante.capture());
    assertFalse(comprobante.getValue().containsKey("nuevoSaldo"));
    verify(response).sendRedirect(CTX + "/transferencia");
  }

  @Test
  void confirmarErrorShowsServerMessageAndKeepsValuesButNotTheKey() throws Exception {
    params("confirmar", "00100001", "00200002", "150", CLAVE);
    when(service.transferencia("abc123", "00100001", "00200002", new BigDecimal("150"), CLAVE))
        .thenReturn(MovimientoResultado.error("Cuenta destino no existe"));

    controller.doPost(request, response);

    verify(request).setAttribute("error", "Cuenta destino no existe");
    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute("origen", "00100001");
    verify(request).setAttribute("destino", "00200002");
    verify(request).setAttribute("importe", "150");
    verifyKeyNeverKept();
    verify(session, never()).setAttribute(eq("comprobanteTransferencia"), any());
    verify(session, never()).invalidate();
    verify(response, never()).sendRedirect(anyString());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void confirmarExpiredSessionInvalidatesAndRedirectsToLogin() throws Exception {
    params("confirmar", "00100001", "00200002", "150", CLAVE);
    when(service.transferencia("abc123", "00100001", "00200002", new BigDecimal("150"), CLAVE))
        .thenReturn(MovimientoResultado.fallo(EstadosEnum.SESION_EXPIRADA, "Token inválido"));

    controller.doPost(request, response);

    verify(session).invalidate();
    verify(response).sendRedirect(CTX + "/login?expirada=1");
    verify(dispatcher, never()).forward(any(ServletRequest.class), any(ServletResponse.class));
    verify(session, never()).setAttribute(eq("comprobanteTransferencia"), any());
    verify(request, never()).setAttribute(eq("error"), any());
    verifyKeyNeverKept();
  }

  @Test
  void getAfterSuccessShowsReceiptAndClearsFlash() throws Exception {
    Map<String, String> comprobante = Map.of("operacion", "Transferencia");
    when(session.getAttribute("comprobanteTransferencia")).thenReturn(comprobante);

    controller.doGet(request, response);

    verify(request).setAttribute("comprobante", comprobante);
    verify(request).setAttribute("vista", "comprobante");
    verify(session).removeAttribute("comprobanteTransferencia");
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }
}
