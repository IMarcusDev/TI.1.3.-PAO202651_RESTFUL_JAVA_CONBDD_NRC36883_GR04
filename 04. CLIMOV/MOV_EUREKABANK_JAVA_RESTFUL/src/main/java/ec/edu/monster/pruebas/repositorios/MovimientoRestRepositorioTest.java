package ec.edu.monster.pruebas.repositorios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.modelos.data.Comprobante;
import ec.edu.monster.modelos.data.ConsultaMovimientos;
import ec.edu.monster.modelos.data.Movimiento;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.SolicitudOperacion;
import ec.edu.monster.modelos.enums.TipoOperacion;
import ec.edu.monster.modelos.repositorios.MovimientoRestRepositorio;
import ec.edu.monster.pruebas.apoyo.TransporteFalso;
import java.math.BigDecimal;
import org.json.JSONObject;
import org.junit.Test;

public class MovimientoRestRepositorioTest {

    private static final String URL_BASE = "http://servidor:8080/WS_CON_UNI_JAVA_RESTFul/api/";

    private static MovimientoRestRepositorio repositorio(TransporteFalso transporte) {
        return new MovimientoRestRepositorio(URL_BASE, transporte, () -> false);
    }

    private static final String MOVIMIENTO_1 = "{\"numeroMovimiento\":1,\"fecha\":\"2026-10-30T09:15:00\","
            + "\"tipoMovimiento\":\"Depósito\",\"accion\":\"INGRESO\",\"importe\":500.00,\"cuentaReferencia\":null}";
    private static final String MOVIMIENTO_2 = "{\"numeroMovimiento\":2,\"fecha\":\"2026-10-31T16:40:00\","
            + "\"tipoMovimiento\":\"Transferencia\",\"accion\":\"SALIDA\",\"importe\":120.50,\"cuentaReferencia\":\"00100002\"}";

    @Test
    public void consultar_leeLaCuentaYSusMovimientos() throws Exception {
        TransporteFalso transporte = TransporteFalso.responde("{\"estado\":\"OK\",\"mensaje\":\"Consulta de movimientos exitosa\","
                + "\"cuenta\":{\"numeroCuenta\":\"00100001\",\"titular\":\"Ana Pérez\",\"moneda\":\"USD\","
                + "\"saldo\":379.50,\"estadoCuenta\":\"ACTIVO\"},"
                + "\"movimientos\":[" + MOVIMIENTO_1 + "," + MOVIMIENTO_2 + "]}");

        Resultado<ConsultaMovimientos> resultado = repositorio(transporte).consultarMovimientos("tok", "00100001");

        assertTrue(resultado.isExito());
        assertEquals("Ana Pérez", resultado.getDatos().getCuenta().getTitular());
        assertEquals(0, new BigDecimal("379.50").compareTo(resultado.getDatos().getCuenta().getSaldo()));
        assertEquals(2, resultado.getDatos().getMovimientos().size());
        Movimiento deposito = resultado.getDatos().getMovimientos().get(0);
        assertEquals("1", deposito.getNumero());
        assertFalse(deposito.tieneReferencia());
        Movimiento transferencia = resultado.getDatos().getMovimientos().get(1);
        assertFalse(transferencia.esIngreso());
        assertEquals("00100002", transferencia.getCuentaReferencia());
        assertEquals(URL_BASE + "Movimiento/consultarMovimientos", transporte.ultimaUrl());
        JSONObject cuerpo = new JSONObject(transporte.ultimoCuerpo());
        assertEquals("tok", cuerpo.getString("token"));
        assertEquals("00100001", cuerpo.getString("cuenta"));
    }

    @Test
    public void consultar_cuentaSinMovimientos_devuelveListaVacia() {
        TransporteFalso transporte = TransporteFalso.responde(
                "{\"estado\":\"OK\",\"mensaje\":\"ok\",\"cuenta\":{\"numeroCuenta\":\"00100001\"},\"movimientos\":[]}");

        assertTrue(repositorio(transporte).consultarMovimientos("tok", "00100001").getDatos().getMovimientos().isEmpty());
    }

    @Test
    public void consultar_nombresEnPascalCase_tambienSeLeen() {
        TransporteFalso transporte = TransporteFalso.responde("{\"Estado\":\"OK\",\"Mensaje\":\"ok\","
                + "\"Cuenta\":{\"NumeroCuenta\":\"00100001\",\"Titular\":\"Ana\",\"Saldo\":10},"
                + "\"Movimientos\":[{\"NumeroMovimiento\":1,\"Accion\":\"INGRESO\",\"Importe\":10}]}");

        ConsultaMovimientos consulta = repositorio(transporte).consultarMovimientos("tok", "00100001").getDatos();

        assertEquals("Ana", consulta.getCuenta().getTitular());
        assertEquals(1, consulta.getMovimientos().size());
        assertTrue(consulta.getMovimientos().get(0).esIngreso());
    }

    @Test
    public void tokenInvalido_marcaLaSesionComoExpirada() {
        TransporteFalso transporte = TransporteFalso.responde(401,
                "{\"estado\":\"ERROR\",\"mensaje\":\"Token inválido o sesión expirada\"}");

        assertTrue(repositorio(transporte).consultarMovimientos("tok", "00100001").isSesionExpirada());
    }

    @Test
    public void registrarTransferencia_enviaLasDosCuentasLaClaveYElImporte() throws Exception {
        TransporteFalso transporte = TransporteFalso.responde("{\"estado\":\"OK\",\"mensaje\":\"Transferencia registrada correctamente\","
                + "\"numeroMovimiento\":15,\"nuevoSaldo\":879.50}");
        SolicitudOperacion solicitud = new SolicitudOperacion(TipoOperacion.TRANSFERENCIA,
                "00100001", "00100002", new BigDecimal("120.50"), "123456");

        Resultado<Comprobante> resultado = repositorio(transporte).registrar("tok", solicitud);

        assertTrue(resultado.isExito());
        assertEquals("15", resultado.getDatos().getNumeroMovimiento());
        assertEquals(0, new BigDecimal("879.50").compareTo(resultado.getDatos().getNuevoSaldo()));
        assertEquals(URL_BASE + "Movimiento/registrarTransferencia", transporte.ultimaUrl());
        JSONObject cuerpo = new JSONObject(transporte.ultimoCuerpo());
        assertEquals("tok", cuerpo.getString("token"));
        assertEquals("00100001", cuerpo.getString("cuentaOrigen"));
        assertEquals("00100002", cuerpo.getString("cuentaDestino"));
        // El importe viaja como número JSON (no como texto).
        assertTrue(cuerpo.get("importe") instanceof Number);
        assertEquals(0, new BigDecimal("120.50").compareTo(new BigDecimal(cuerpo.get("importe").toString())));
        assertEquals("123456", cuerpo.getString("clave"));
    }

    @Test
    public void registrarDeposito_noEnviaClave() throws Exception {
        TransporteFalso transporte = TransporteFalso.responde(
                "{\"estado\":\"OK\",\"mensaje\":\"ok\",\"numeroMovimiento\":3,\"nuevoSaldo\":10}");
        SolicitudOperacion solicitud = new SolicitudOperacion(TipoOperacion.DEPOSITO,
                "00100001", null, new BigDecimal("10.00"), "");

        repositorio(transporte).registrar("tok", solicitud);

        assertEquals(URL_BASE + "Movimiento/registrarDeposito", transporte.ultimaUrl());
        JSONObject cuerpo = new JSONObject(transporte.ultimoCuerpo());
        assertEquals("00100001", cuerpo.getString("cuenta"));
        assertFalse(cuerpo.has("clave"));
        assertFalse(cuerpo.has("cuentaDestino"));
    }

    @Test
    public void registrarRetiroDelCliente_enviaClaveVacia() throws Exception {
        TransporteFalso transporte = TransporteFalso.responde(
                "{\"estado\":\"OK\",\"mensaje\":\"ok\",\"numeroMovimiento\":4,\"nuevoSaldo\":5}");
        SolicitudOperacion solicitud = new SolicitudOperacion(TipoOperacion.RETIRO,
                "00100001", null, new BigDecimal("5.00"), "");

        repositorio(transporte).registrar("tok", solicitud);

        assertEquals(URL_BASE + "Movimiento/registrarRetiro", transporte.ultimaUrl());
        assertEquals("", new JSONObject(transporte.ultimoCuerpo()).getString("clave"));
    }

    @Test
    public void registrar_errorDeNegocio_muestraElMensajeDelServidor() {
        TransporteFalso transporte = TransporteFalso.responde(400,
                "{\"estado\":\"ERROR\",\"mensaje\":\"Saldo insuficiente\"}");
        SolicitudOperacion solicitud = new SolicitudOperacion(TipoOperacion.RETIRO,
                "00100001", null, new BigDecimal("9999.00"), "123456");

        Resultado<Comprobante> resultado = repositorio(transporte).registrar("tok", solicitud);

        assertFalse(resultado.isExito());
        assertFalse(resultado.isSesionExpirada());
        assertEquals("Saldo insuficiente", resultado.getMensaje());
    }

    @Test
    public void registrar_errorInternoSinJson_noSeTomaComoExito() {
        TransporteFalso transporte = TransporteFalso.responde(500, "Internal Server Error");
        SolicitudOperacion solicitud = new SolicitudOperacion(TipoOperacion.RETIRO,
                "00100001", null, new BigDecimal("1.00"), "123456");

        Resultado<Comprobante> resultado = repositorio(transporte).registrar("tok", solicitud);

        assertFalse(resultado.isExito());
        assertTrue(resultado.getMensaje().contains("HTTP 500"));
    }
}
