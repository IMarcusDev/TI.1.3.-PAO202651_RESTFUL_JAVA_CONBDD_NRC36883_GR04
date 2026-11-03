package ec.edu.monster.modelo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Reads the RESTful server location and builds the endpoint URLs used by the client services.
 *
 * <p>The values are looked up, in this order, in:
 * <ol>
 *   <li>the file named by {@code -Dcliente.config=<ruta>};</li>
 *   <li>{@code config.properties} in the working directory, which is where Maven copies it next
 *       to the jar;</li>
 *   <li>{@code config.properties} inside the jar (classpath).</li>
 * </ol>
 *
 * <p>The first two let the operator point the client at another machine —the server will live on
 * a different computer— by editing a single file, without recompiling or opening the jar.
 */
public final class Configuracion {

    public static final String ARCHIVO = "config.properties";

    /** System property that names an explicit configuration file. */
    public static final String CLAVE_RUTA = "cliente.config";

    private final Properties propiedades;
    private final String origen;

    /** Builds a configuration from an in-memory set of properties (used by the tests). */
    public Configuracion(Properties propiedades) {
        this(propiedades, ARCHIVO);
    }

    private Configuracion(Properties propiedades, String origen) {
        this.propiedades = propiedades;
        this.origen = origen;
    }

    /**
     * Loads the configuration the application should use: the explicit file, the one next to the
     * jar, or the packaged default.
     */
    public static Configuracion cargar() {
        return cargar(Path.of("."));
    }

    /** Same as {@link #cargar()}, looking for {@code config.properties} in the given directory. */
    public static Configuracion cargar(Path directorioTrabajo) {
        String ruta = System.getProperty(CLAVE_RUTA);
        if (ruta != null && !ruta.isBlank()) {
            Path archivo = Path.of(ruta.trim());
            if (!Files.isReadable(archivo)) {
                throw new IllegalStateException("No se encontró el archivo indicado en -D"
                        + CLAVE_RUTA + ": " + archivo.toAbsolutePath());
            }
            return desdeArchivo(archivo);
        }
        Path juntoAlJar = directorioTrabajo.resolve(ARCHIVO);
        if (Files.isReadable(juntoAlJar)) {
            return desdeArchivo(juntoAlJar);
        }
        return desdeClasspath();
    }

    /** Reads config.properties from the classpath, so it works inside the packaged jar. */
    public static Configuracion desdeClasspath() {
        Properties props = new Properties();
        try (InputStream entrada = Configuracion.class.getClassLoader().getResourceAsStream(ARCHIVO)) {
            if (entrada == null) {
                throw new IllegalStateException(ARCHIVO + " no está en el classpath");
            }
            props.load(entrada);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ARCHIVO, e);
        }
        return new Configuracion(props, ARCHIVO);
    }

    /** Reads a configuration file from an explicit path, so a wrong path or key names it. */
    public static Configuracion desdeArchivo(Path archivo) {
        Properties props = new Properties();
        try (InputStream entrada = Files.newInputStream(archivo)) {
            props.load(entrada);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + archivo.toAbsolutePath(), e);
        }
        return new Configuracion(props, archivo.toAbsolutePath().toString());
    }

    /** e.g. http://localhost:8080/WS_CON_UNI_JAVA_RESTFUL */
    public String getUrlBase() {
        return "http://" + valor("ws.host") + ":" + valor("ws.port") + "/" + valor("ws.context");
    }

    /** Full address of one service, e.g. getEndpoint("api/Login"). */
    public String getEndpoint(String servicio) {
        return getUrlBase() + "/" + servicio;
    }

    /**
     * Checks every key and the resulting address up front, so a hand-edited file fails at startup
     * with a message that names the file instead of later, deep inside a request.
     *
     * @return this configuration, for chaining
     */
    public Configuracion validar() {
        getPuerto();
        String base = getUrlBase();
        URI uri;
        try {
            uri = URI.create(base);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("La dirección del servidor no es válida en " + origen + ": " + base, e);
        }
        if (uri.getHost() == null) {
            throw new IllegalStateException("La dirección del servidor no es válida en " + origen + ": " + base);
        }
        return this;
    }

    public String getHost() {
        return valor("ws.host");
    }

    public int getPuerto() {
        String texto = valor("ws.port");
        int puerto;
        try {
            puerto = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("'ws.port' no es un número en " + origen + ": " + texto, e);
        }
        if (puerto < 1 || puerto > 65535) {
            throw new IllegalStateException("'ws.port' debe estar entre 1 y 65535 en " + origen + ": " + texto);
        }
        return puerto;
    }

    // Returns a required key, failing with a clear message that names the file it came from
    private String valor(String clave) {
        String valor = propiedades.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta '" + clave + "' en " + origen);
        }
        return valor.trim();
    }
}
