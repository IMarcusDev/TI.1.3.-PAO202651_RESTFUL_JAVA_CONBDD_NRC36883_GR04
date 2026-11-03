package ec.edu.monster.pruebas.repositorios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.enums.Perfil;
import ec.edu.monster.modelos.repositorios.LoginRestRepositorio;
import ec.edu.monster.modelos.repositorios.RestErrores;
import ec.edu.monster.pruebas.apoyo.TransporteFalso;
import java.net.SocketTimeoutException;
import org.json.JSONObject;
import org.junit.Test;

public class LoginRestRepositorioTest {

    private static final String URL_BASE = "http://servidor:8080/WS_CON_UNI_JAVA_RESTFul/api/";

    private static LoginRestRepositorio repositorio(TransporteFalso transporte) {
        return new LoginRestRepositorio(URL_BASE, transporte, () -> false);
    }

    @Test
    public void loginEmpleado_ok_devuelveTokenPerfilYNombre() throws Exception {
        TransporteFalso transporte = TransporteFalso.responde("{\"estado\":\"OK\",\"mensaje\":\"Ingreso de sesión exitoso\","
                + "\"token\":\"abc-123\",\"perfil\":\"EMPLEADO\",\"nombre\":\"Marcos Escobar\"}");

        Resultado<Sesion> resultado = repositorio(transporte).loginEmpleado("mescobar", "secreta");

        assertTrue(resultado.isExito());
        assertEquals("abc-123", resultado.getDatos().getToken());
        assertEquals(Perfil.EMPLEADO, resultado.getDatos().getPerfil());
        assertEquals("Marcos Escobar", resultado.getDatos().getNombre());
        assertEquals(URL_BASE + "Login/loginEmpleado", transporte.ultimaUrl());
        JSONObject cuerpo = new JSONObject(transporte.ultimoCuerpo());
        assertEquals("mescobar", cuerpo.getString("usuario"));
        assertEquals("secreta", cuerpo.getString("clave"));
        assertEquals(2, cuerpo.length());
    }

    @Test
    public void loginCliente_guardaLaCuentaDeLaSesion() throws Exception {
        TransporteFalso transporte = TransporteFalso.responde(
                "{\"estado\":\"OK\",\"mensaje\":\"ok\",\"token\":\"t\",\"perfil\":\"CLIENTE\",\"nombre\":\"Ana\"}");

        Resultado<Sesion> resultado = repositorio(transporte).loginCliente("00100001", "123456");

        assertEquals(Perfil.CLIENTE, resultado.getDatos().getPerfil());
        assertEquals("00100001", resultado.getDatos().getCuenta());
        assertEquals(URL_BASE + "Login/loginCliente", transporte.ultimaUrl());
        JSONObject cuerpo = new JSONObject(transporte.ultimoCuerpo());
        assertEquals("00100001", cuerpo.getString("cuenta"));
        assertEquals("123456", cuerpo.getString("clave"));
    }

    @Test
    public void login_estadoNumericoDeDotNet_seInterpreta() {
        TransporteFalso transporte = TransporteFalso.responde(
                "{\"estado\":0,\"mensaje\":\"ok\",\"token\":\"t\",\"perfil\":\"EMPLEADO\",\"nombre\":\"Ana\"}");

        assertTrue(repositorio(transporte).loginEmpleado("u", "c").isExito());
    }

    @Test
    public void login_credencialesIncorrectasCon401_muestraElMensajeDelServidor() {
        TransporteFalso transporte = TransporteFalso.responde(401,
                "{\"estado\":\"ERROR\",\"mensaje\":\"La cuenta no se encuentra activa\"}");

        Resultado<Sesion> resultado = repositorio(transporte).loginCliente("00100001", "123456");

        assertFalse(resultado.isExito());
        assertEquals("La cuenta no se encuentra activa", resultado.getMensaje());
    }

    @Test
    public void login_okSinToken_esRespuestaIncompleta() {
        TransporteFalso transporte = TransporteFalso.responde("{\"estado\":\"OK\",\"mensaje\":\"ok\",\"token\":\"\"}");

        assertEquals(RestErrores.RESPUESTA_INCOMPLETA, repositorio(transporte).loginEmpleado("u", "c").getMensaje());
    }

    @Test
    public void login_rutaInexistente_informaElCodigoHttp() {
        TransporteFalso transporte = TransporteFalso.responde(404, TransporteFalso.HTML_PAYARA);

        assertTrue(repositorio(transporte).loginEmpleado("u", "c").getMensaje().contains("HTTP 404"));
    }

    @Test
    public void login_sinRespuestaEnQuinceSegundos_informaQueNoHayServidor() {
        TransporteFalso transporte = TransporteFalso.lanza(new SocketTimeoutException("timeout"));

        assertTrue(repositorio(transporte).loginEmpleado("u", "c").getMensaje().startsWith(RestErrores.SIN_SERVIDOR));
    }

    @Test
    public void login_sinRed_noLlamaAlServidor() {
        TransporteFalso transporte = TransporteFalso.responde("");
        LoginRestRepositorio repositorio = new LoginRestRepositorio(URL_BASE, transporte, () -> true);

        assertEquals(RestErrores.SIN_CONEXION, repositorio.loginEmpleado("u", "c").getMensaje());
        assertEquals(0, transporte.peticiones());
    }

    @Test
    public void logout_enviaElToken() throws Exception {
        TransporteFalso transporte = TransporteFalso.responde("{\"estado\":\"OK\",\"mensaje\":\"Sesión cerrada\"}");

        assertTrue(repositorio(transporte).logout("abc-123").isExito());
        assertEquals(URL_BASE + "Login/logout", transporte.ultimaUrl());
        assertEquals("abc-123", new JSONObject(transporte.ultimoCuerpo()).getString("token"));
    }
}
