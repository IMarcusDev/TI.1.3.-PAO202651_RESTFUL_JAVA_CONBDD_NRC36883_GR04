package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import ec.edu.monster.modelo.EstadoServidor;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EstadoServidorTest {

    private HttpServer servidor;

    @AfterEach
    void apagarServidor() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    // Disponible cuando el servidor RESTful responde su recurso de salud.
    @Test
    void disponibleWhenServerAnswersHealthResource() throws IOException {
        EstadoServidor estado = new EstadoServidor(arrancar(200));

        assertTrue(estado.disponible());
    }

    // Un error del servidor no cuenta como disponible.
    @Test
    void noDisponibleWhenServerFails() throws IOException {
        EstadoServidor estado = new EstadoServidor(arrancar(500));

        assertFalse(estado.disponible());
    }

    // El servidor estuvo arriba y luego se cae: la siguiente consulta debe decir no disponible.
    @Test
    void noDisponibleAfterServerGoesDown() throws IOException {
        EstadoServidor estado = new EstadoServidor(arrancar(200));
        assertTrue(estado.disponible());

        servidor.stop(0);

        assertFalse(estado.disponible());
    }

    private URI arrancar(int codigo) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/jakartaee11", intercambio -> {
            intercambio.sendResponseHeaders(codigo, -1);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.flush();
            }
        });
        servidor.start();
        return URI.create("http://localhost:" + servidor.getAddress().getPort() + "/jakartaee11");
    }
}
