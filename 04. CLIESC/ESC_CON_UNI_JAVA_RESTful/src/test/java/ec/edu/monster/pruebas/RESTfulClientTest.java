package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import ec.edu.monster.modelo.dto.CatalogResponseDTO;
import ec.edu.monster.modelo.dto.ConversorResponseDTO;
import ec.edu.monster.modelo.dto.LoginResponseDTO;
import ec.edu.monster.modelo.enums.EstadosEnum;
import ec.edu.monster.modelo.http.ConversorRestApi;
import ec.edu.monster.modelo.http.LoginRestApi;
import ec.edu.monster.modelo.http.RESTfulClient;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Exercises the HTTP layer against a loopback fake server; no real server is needed. */
class RESTfulClientTest {

    private HttpServer servidor;
    private RESTfulClient cliente;
    private String metodo;
    private String ruta;
    private String autorizacion;
    private String cuerpo;
    private String respuesta;
    private int codigo = 200;

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        servidor.createContext("/api", intercambio -> {
            metodo = intercambio.getRequestMethod();
            ruta = intercambio.getRequestURI().getPath();
            autorizacion = intercambio.getRequestHeaders().getFirst("Authorization");
            cuerpo = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(codigo, bytes.length);
            intercambio.getResponseBody().write(bytes);
            intercambio.close();
        });
        servidor.start();
        cliente = new RESTfulClient("http://localhost:" + servidor.getAddress().getPort() + "/api");
    }

    @AfterEach
    void detenerServidor() {
        servidor.stop(0);
    }

    @Test
    void loginPostsCredentialsWithoutAuthorizationHeader() throws IOException {
        respuesta = "{\"estado\":\"OK\",\"mensaje\":\"Ingreso\",\"token\":\"tok-1\"}";

        LoginResponseDTO dto = new LoginRestApi(cliente).login("user", "1234");

        assertEquals("POST", metodo);
        assertEquals("/api/login", ruta);
        assertNull(autorizacion);
        JSONObject enviado = new JSONObject(cuerpo);
        assertEquals("user", enviado.getString("username"));
        assertEquals("1234", enviado.getString("password"));
        assertEquals(EstadosEnum.OK, dto.getEstado());
        assertEquals("tok-1", dto.getToken());
    }

    @Test
    void convertPostsJsonWithTokenInAuthorizationHeader() throws IOException {
        respuesta = "{\"estado\":\"OK\",\"mensaje\":\"Conversión Exitosa\",\"resultado\":328.084}";

        ConversorResponseDTO dto = new ConversorRestApi(cliente).convert("tok-1", "Longitud", 100, "m", "ft");

        assertEquals("POST", metodo);
        assertEquals("/api/converter", ruta);
        assertEquals("tok-1", autorizacion);
        JSONObject enviado = new JSONObject(cuerpo);
        assertEquals("Longitud", enviado.getString("type"));
        assertEquals(100.0, enviado.getDouble("value"));
        assertEquals("m", enviado.getString("from"));
        assertEquals("ft", enviado.getString("to"));
        assertEquals(328.084, dto.getResultado());
    }

    @Test
    void catalogGetsUnitsInServerOrderWithTokenInAuthorizationHeader() throws IOException {
        respuesta = "{\"estado\":\"OK\",\"mensaje\":\"ok\",\"resultado\":[\"m\",\"ft\",\"km\"]}";

        CatalogResponseDTO dto = new ConversorRestApi(cliente).catalog("tok-1", "Longitud");

        assertEquals("GET", metodo);
        assertEquals("/api/catalog/Longitud", ruta);
        assertEquals("tok-1", autorizacion);
        assertEquals(List.of("m", "ft", "km"), dto.getResultado());
    }

    @Test
    void readsErrorBodyOfRejectedRequests() throws IOException {
        codigo = 401;
        respuesta = "{\"estado\":\"ERROR\",\"mensaje\":\"Token Inválido\",\"resultado\":[]}";

        CatalogResponseDTO dto = new ConversorRestApi(cliente).catalog("malo", "Masa");

        assertEquals(EstadosEnum.ERROR, dto.getEstado());
        assertEquals("Token Inválido", dto.getMensaje());
        assertEquals(List.of(), dto.getResultado());
    }

    @Test
    void treatsUnknownStatusAsErrorAndMissingFieldsAsNull() {
        LoginResponseDTO dto = LoginResponseDTO.fromJSON("{\"estado\":\"RARO\"}");

        assertEquals(EstadosEnum.ERROR, dto.getEstado());
        assertNull(dto.getMensaje());
        assertNull(dto.getToken());
        assertNull(CatalogResponseDTO.fromJSON("{\"estado\":\"OK\"}").getResultado());
    }

    @Test
    void rejectsNonJsonBodies() throws IOException {
        respuesta = "<html>Error</html>";

        assertThrows(JSONException.class, () -> new LoginRestApi(cliente).login("user", "1234"));
    }

    @Test
    void failsWithIoExceptionWhenServerIsDown() {
        servidor.stop(0);

        assertThrows(IOException.class, () -> new LoginRestApi(cliente).login("user", "1234"));
    }
}
