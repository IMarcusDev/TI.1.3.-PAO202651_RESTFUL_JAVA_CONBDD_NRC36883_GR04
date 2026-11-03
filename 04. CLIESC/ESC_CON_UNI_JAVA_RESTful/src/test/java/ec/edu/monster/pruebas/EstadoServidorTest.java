package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ec.edu.monster.dobles.ConfiguracionDePrueba;
import ec.edu.monster.modelo.Configuracion;
import ec.edu.monster.modelo.EstadoServidor;
import ec.edu.monster.modelo.RutasApi;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class EstadoServidorTest {

    @Test
    void availableWhenTheApiAnswers200() throws IOException {
        assertTrue(disponibleSiResponde(200));
    }

    /** The API root has no resource of its own: a 404 still proves the server is up. */
    @Test
    void availableWhenTheApiAnswersAClientError() throws IOException {
        assertTrue(disponibleSiResponde(404));
    }

    @Test
    void unavailableWhenTheServerFails() throws IOException {
        assertFalse(disponibleSiResponde(500));
    }

    @Test
    void unavailableWhenNothingIsListening() {
        URI api = URI.create("http://localhost:" + ConfiguracionDePrueba.puertoLibre() + "/api");

        assertFalse(new EstadoServidor(api).disponible());
    }

    @Test
    void checksTheApiRootOfTheConfiguredServer() throws IOException {
        Configuracion configuracion = ConfiguracionDePrueba.paraPuerto(ConfiguracionDePrueba.puertoLibre());
        URI esperado = URI.create(configuracion.getEndpoint(RutasApi.RAIZ));
        HttpServer servidor = HttpServer.create(new InetSocketAddress("localhost", esperado.getPort()), 0);
        StringBuilder ruta = new StringBuilder();
        servidor.createContext("/", intercambio -> {
            ruta.append(intercambio.getRequestURI().getPath());
            responder(intercambio, 404);
        });
        servidor.start();
        try {
            assertTrue(new EstadoServidor(configuracion).disponible());
            assertEquals(esperado.getPath(), ruta.toString());
        } finally {
            servidor.stop(0);
        }
    }

    private static boolean disponibleSiResponde(int estado) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/api", intercambio -> responder(intercambio, estado));
        servidor.start();
        try {
            URI api = URI.create("http://localhost:" + servidor.getAddress().getPort() + "/api");
            return new EstadoServidor(api).disponible();
        } finally {
            servidor.stop(0);
        }
    }

    private static void responder(HttpExchange intercambio, int estado) throws IOException {
        byte[] cuerpo = "{}".getBytes(StandardCharsets.UTF_8);
        intercambio.sendResponseHeaders(estado, cuerpo.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(cuerpo);
        }
    }
}
