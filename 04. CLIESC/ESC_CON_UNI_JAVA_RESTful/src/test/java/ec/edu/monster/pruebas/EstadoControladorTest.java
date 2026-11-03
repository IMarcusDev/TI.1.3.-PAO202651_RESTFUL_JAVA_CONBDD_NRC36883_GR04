package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sun.net.httpserver.HttpServer;
import ec.edu.monster.controlador.EstadoControlador;
import ec.edu.monster.modelo.EstadoServidor;
import ec.edu.monster.vista.EstadoVista;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EstadoControladorTest {

    private HttpServer servidor;

    @AfterEach
    void apagarServidor() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    // Cada revisión pregunta al servidor: verde mientras responde, rojo cuando se cae.
    @Test
    void showsServerGoingDownOnTheNextCheck() throws IOException {
        VistaFalsa vista = new VistaFalsa();
        new EstadoControlador(vista, new EstadoServidor(arrancar()));
        assertNotNull(vista.revisar, "the controller must register its check");

        vista.revisar.run();
        servidor.stop(0);
        vista.revisar.run();

        assertEquals(List.of(true, false), vista.mostrados);
    }

    private URI arrancar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/jakartaee11", intercambio -> {
            intercambio.sendResponseHeaders(200, -1);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.flush();
            }
        });
        servidor.start();
        return URI.create("http://localhost:" + servidor.getAddress().getPort() + "/jakartaee11");
    }

    private static final class VistaFalsa implements EstadoVista {

        final List<Boolean> mostrados = new ArrayList<>();
        Runnable revisar;

        @Override
        public void mostrarEstado(boolean disponible) {
            mostrados.add(disponible);
        }

        @Override
        public void alRevisarEstado(Runnable accion) {
            revisar = accion;
        }
    }
}
