package ec.edu.monster.dobles;

import ec.edu.monster.modelo.Configuracion;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

/**
 * Configuration pointing at a free local port, so the REST tests never collide
 * with a real server running on 8080 or with another test.
 */
public final class ConfiguracionDePrueba {

    private ConfiguracionDePrueba() {
    }

    public static int puertoLibre() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo reservar un puerto local", e);
        }
    }

    public static Configuracion paraPuerto(int puerto) {
        Properties propiedades = new Properties();
        propiedades.setProperty("ws.host", "localhost");
        propiedades.setProperty("ws.port", String.valueOf(puerto));
        propiedades.setProperty("ws.context", "WS_EUREKABANK_JAVA_RESTFul");
        return new Configuracion(propiedades);
    }
}
