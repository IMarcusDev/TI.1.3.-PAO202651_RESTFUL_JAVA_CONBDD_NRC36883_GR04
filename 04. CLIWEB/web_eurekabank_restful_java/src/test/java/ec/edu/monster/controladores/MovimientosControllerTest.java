package ec.edu.monster.controladores;

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
import ec.edu.monster.modelos.dto.HistorialCuenta;
import ec.edu.monster.modelos.servicios.MovimientoService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests MovimientosController with Mockito mocks for request, response,
 * session and MovimientoService.
 */
class MovimientosControllerTest {
  private static final String VISTA = "/WEB-INF/ec/edu/monster/vista/movimientos.jsp";
  private static final String CTX = "/web_eurekabank_restful_java";

  private final MovimientoService service = mock(MovimientoService.class);
  private final MovimientosController controller = new MovimientosController(service);
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final RequestDispatcher dispatcher = mock(RequestDispatcher.class);
  private final HttpSession session = mock(HttpSession.class);

  @BeforeEach
  void setUp() {
    when(request.getRequestDispatcher(VISTA)).thenReturn(dispatcher);
    when(request.getContextPath()).thenReturn(CTX);
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("token")).thenReturn("tok-123");
  }

  private static HistorialCuenta historialOk(String cuenta, int movCount) {
    HistorialCuenta.CuentaConsultada datosCuenta = new HistorialCuenta.CuentaConsultada(
        cuenta, "Cliente Prueba", "USD", new BigDecimal("1250.00"), "ACTIVO");
    List<HistorialCuenta.MovimientoConsultado> lista = movCount == 0
        ? List.of()
        : List.of(new HistorialCuenta.MovimientoConsultado(
            "000001", "03/11/2026 10:00", "Depósito", "INGRESO", new BigDecimal("100.00"), "-"));
    return new HistorialCuenta(EstadosEnum.OK, "Consulta exitosa", datosCuenta, lista);
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
  void getAsEmployeeRendersFormWithoutPreselectedAccount() throws Exception {
    controller.doGet(request, response);

    verify(response).setHeader("Cache-Control", "no-store");
    verify(request, never()).setAttribute(eq("cuenta"), any());
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }

  @Test
  void getAsClientePrefillsOwnAccountFromSession() throws Exception {
    when(session.getAttribute("cuenta")).thenReturn("00100001");

    controller.doGet(request, response);

    verify(response).setHeader("Cache-Control", "no-store");
    verify(request).setAttribute("cuenta", "00100001");
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }

  @Test
  void employeeConsultsAnyAccount() throws Exception {
    when(request.getParameter("cuenta")).thenReturn(" 00100002 ");
    HistorialCuenta historial = historialOk("00100002", 1);
    when(service.consultarMovimientos("tok-123", "00100002")).thenReturn(historial);

    controller.doPost(request, response);

    verify(service).consultarMovimientos("tok-123", "00100002");
    verify(request).setAttribute("cuenta", "00100002");
    verify(request).setAttribute("historial", historial);
    verify(dispatcher).forward(request, response);
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  void clienteForcedOwnAccountIgnoringRequest() throws Exception {
    when(session.getAttribute("cuenta")).thenReturn("00100001");
    when(request.getParameter("cuenta")).thenReturn("99999999");
    HistorialCuenta historial = historialOk("00100001", 1);
    when(service.consultarMovimientos("tok-123", "00100001")).thenReturn(historial);

    controller.doPost(request, response);

    verify(service).consultarMovimientos("tok-123", "00100001");
    verify(service, never()).consultarMovimientos("tok-123", "99999999");
    verify(request).setAttribute("cuenta", "00100001");
    verify(request).setAttribute("historial", historial);
    verify(dispatcher).forward(request, response);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "   "})
  void validationIngreseNumeroDeCuentaWhenBlank(String blankAccount) throws Exception {
    when(request.getParameter("cuenta")).thenReturn(blankAccount);

    controller.doPost(request, response);

    verify(request).setAttribute("error", "Ingrese el número de cuenta");
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
    verify(request, never()).setAttribute(eq("historial"), any());
  }

  @Test
  void emptyListReturnsHistorialWithSinMovimientos() throws Exception {
    when(request.getParameter("cuenta")).thenReturn("00100004");
    HistorialCuenta historial = historialOk("00100004", 0);
    when(service.consultarMovimientos("tok-123", "00100004")).thenReturn(historial);

    controller.doPost(request, response);

    verify(service).consultarMovimientos("tok-123", "00100004");
    verify(request).setAttribute("historial", historial);
    verify(dispatcher).forward(request, response);
    verify(request, never()).setAttribute(eq("error"), any());
  }

  @Test
  void serverErrorShowsServerMessage() throws Exception {
    when(request.getParameter("cuenta")).thenReturn("00100099");
    when(service.consultarMovimientos("tok-123", "00100099"))
        .thenReturn(HistorialCuenta.error("La cuenta no existe"));

    controller.doPost(request, response);

    verify(service).consultarMovimientos("tok-123", "00100099");
    verify(request).setAttribute("error", "La cuenta no existe");
    verify(request, never()).setAttribute(eq("historial"), any());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void unauthorized401InvalidatesSessionAndRedirectsToLogin() throws Exception {
    when(request.getParameter("cuenta")).thenReturn("00100001");
    when(service.consultarMovimientos("tok-123", "00100001"))
        .thenReturn(HistorialCuenta.fallo(EstadosEnum.SESION_EXPIRADA, "Token inválido"));

    controller.doPost(request, response);

    verify(session).invalidate();
    verify(response).sendRedirect(CTX + "/login?expirada=1");
    verify(dispatcher, never()).forward(any(ServletRequest.class), any(ServletResponse.class));
    verify(request, never()).setAttribute(eq("historial"), any());
  }
}
