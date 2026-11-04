package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.dobles.ConfiguracionDePrueba;
import ec.edu.monster.modelo.Configuracion;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfiguracionTest {

    private static final String CONTEXTO = "WS_EUREKABANK_JAVA_RESTFul";

    @Test
    void buildsTheBaseUrlAndEndpoints() {
        Configuracion configuracion = ConfiguracionDePrueba.paraPuerto(9090);

        assertEquals("http://localhost:9090/" + CONTEXTO, configuracion.getUrlBase());
        assertEquals("http://localhost:9090/" + CONTEXTO + "/api/login",
                configuracion.getEndpoint("api/login"));
        assertEquals("localhost", configuracion.getHost());
        assertEquals(9090, configuracion.getPuerto());
    }

    @Test
    void missingKeyFailsWithAClearMessage() {
        Properties propiedades = new Properties();
        propiedades.setProperty("ws.host", "localhost");
        propiedades.setProperty("ws.port", "8080");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new Configuracion(propiedades).getUrlBase());
        assertEquals("Falta 'ws.context' en config.properties", error.getMessage());
    }

    @Test
    void blankValueCountsAsMissing() {
        Properties propiedades = new Properties();
        propiedades.setProperty("ws.host", " ");
        propiedades.setProperty("ws.port", "8080");
        propiedades.setProperty("ws.context", "contexto");

        assertThrows(IllegalStateException.class, () -> new Configuracion(propiedades).getUrlBase());
    }

    @Test
    void nonNumericPortFailsWithAClearMessage() {
        Properties propiedades = new Properties();
        propiedades.setProperty("ws.host", "localhost");
        propiedades.setProperty("ws.port", "ocho mil");
        propiedades.setProperty("ws.context", "contexto");

        assertThrows(IllegalStateException.class, () -> new Configuracion(propiedades).getPuerto());
    }

    @Test
    void readsTheFileFromTheClasspath() {
        // src/test/resources/config.properties shadows the application one
        assertEquals("http://localhost:18080/" + CONTEXTO,
                Configuracion.desdeClasspath().getUrlBase());
    }

    @Test
    void theFileNextToTheJarWinsOverThePackagedOne(@TempDir Path carpeta) throws IOException {
        escribir(carpeta.resolve(Configuracion.ARCHIVO), "10.0.0.7", "9191");

        Configuracion configuracion = Configuracion.cargar(carpeta);

        assertEquals("http://10.0.0.7:9191/" + CONTEXTO, configuracion.getUrlBase());
        assertEquals(9191, configuracion.getPuerto());
        assertEquals("10.0.0.7", configuracion.getHost());
    }

    @Test
    void theExplicitFileWinsOverTheOneNextToTheJar(@TempDir Path carpeta) throws IOException {
        escribir(carpeta.resolve(Configuracion.ARCHIVO), "10.0.0.7", "9191");
        Path explicito = carpeta.resolve("servidor-de-la-clase.properties");
        escribir(explicito, "192.168.1.50", "9292");

        String anterior = System.getProperty(Configuracion.CLAVE_RUTA);
        try {
            System.setProperty(Configuracion.CLAVE_RUTA, explicito.toString());

            assertEquals("http://192.168.1.50:9292/" + CONTEXTO, Configuracion.cargar(carpeta).getUrlBase());
        } finally {
            restaurar(anterior);
        }
    }

    @Test
    void anIncompleteExternalFileFailsNamingTheFile(@TempDir Path carpeta) throws IOException {
        Path archivo = carpeta.resolve("incompleto.properties");
        Files.writeString(archivo, "ws.host=localhost\nws.port=8080\n", StandardCharsets.UTF_8);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> Configuracion.desdeArchivo(archivo).getUrlBase());

        assertTrue(error.getMessage().contains("Falta 'ws.context'"), error.getMessage());
        assertTrue(error.getMessage().contains("incompleto.properties"), error.getMessage());
    }

    @Test
    void aMissingExplicitFileFailsWithItsPath(@TempDir Path carpeta) {
        Path inexistente = carpeta.resolve("no-existe.properties");

        String anterior = System.getProperty(Configuracion.CLAVE_RUTA);
        try {
            System.setProperty(Configuracion.CLAVE_RUTA, inexistente.toString());

            IllegalStateException error = assertThrows(IllegalStateException.class, () -> Configuracion.cargar(carpeta));

            assertTrue(error.getMessage().contains("-Dcliente.config"), error.getMessage());
            assertTrue(error.getMessage().contains("no-existe.properties"), error.getMessage());
        } finally {
            restaurar(anterior);
        }
    }

    @Test
    void validateAcceptsACompleteConfiguration() {
        Configuracion configuracion = ConfiguracionDePrueba.paraPuerto(9090);

        assertEquals(configuracion, configuracion.validar());
    }

    @Test
    void validateReportsAMissingKeyNamingTheFile(@TempDir Path carpeta) throws IOException {
        Path archivo = carpeta.resolve("incompleto.properties");
        Files.writeString(archivo, "ws.host=localhost\nws.port=8080\n", StandardCharsets.UTF_8);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> Configuracion.desdeArchivo(archivo).validar());

        assertTrue(error.getMessage().contains("Falta 'ws.context'"), error.getMessage());
        assertTrue(error.getMessage().contains("incompleto.properties"), error.getMessage());
    }

    @Test
    void validateRejectsANonNumericPort() {
        Properties propiedades = new Properties();
        propiedades.setProperty("ws.host", "localhost");
        propiedades.setProperty("ws.port", "ocho mil");
        propiedades.setProperty("ws.context", CONTEXTO);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new Configuracion(propiedades).validar());

        assertEquals("'ws.port' no es un número en config.properties: ocho mil", error.getMessage());
    }

    @Test
    void validateRejectsAPortOutOfRange() {
        for (String puerto : new String[] {"0", "-1", "65536"}) {
            Properties propiedades = new Properties();
            propiedades.setProperty("ws.host", "localhost");
            propiedades.setProperty("ws.port", puerto);
            propiedades.setProperty("ws.context", CONTEXTO);

            IllegalStateException error = assertThrows(IllegalStateException.class,
                    () -> new Configuracion(propiedades).validar());

            assertEquals("'ws.port' debe estar entre 1 y 65535 en config.properties: " + puerto, error.getMessage());
        }
    }

    @Test
    void validateRejectsAMalformedHost() {
        Properties propiedades = new Properties();
        propiedades.setProperty("ws.host", "192.168.1 .50");
        propiedades.setProperty("ws.port", "8080");
        propiedades.setProperty("ws.context", CONTEXTO);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new Configuracion(propiedades).validar());

        assertTrue(error.getMessage().startsWith("La dirección del servidor no es válida en config.properties"),
                error.getMessage());
    }

    private static void escribir(Path archivo, String host, String puerto) throws IOException {
        Files.writeString(archivo,
                "ws.host=" + host + "\nws.port=" + puerto + "\nws.context=" + CONTEXTO + "\n",
                StandardCharsets.UTF_8);
    }

    private static void restaurar(String anterior) {
        if (anterior == null) {
            System.clearProperty(Configuracion.CLAVE_RUTA);
        } else {
            System.setProperty(Configuracion.CLAVE_RUTA, anterior);
        }
    }
}
