package ec.edu.monster.modelo;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Lee la dirección del servidor RESTful de servidor.properties (en la carpeta desde la que
 * se abre la aplicación) y valida que responda. Si no responde, usa localhost.
 */
public final class Configuracion {

    public static final String URL_POR_DEFECTO = "http://localhost:8080/WS_CON_UNI_JAVA_RESTFUL/api";
    public static final String ARCHIVO = "servidor.properties";
    public static final Duration ESPERA = Duration.ofSeconds(2);

    private static String url;

    private Configuracion() {
    }

    /** Dirección validada del servidor; se calcula una sola vez. */
    public static synchronized String url() {
        if (url == null) {
            url = resolverUrl(leerUrlServidor(Path.of(ARCHIVO)), ESPERA);
            System.out.println("Servidor RESTful: " + url);
        }
        return url;
    }

    public static String leerUrlServidor(Path archivo) {
        if (Files.isReadable(archivo)) {
            try (InputStream entrada = Files.newInputStream(archivo)) {
                Properties propiedades = new Properties();
                propiedades.load(entrada);
                String valor = propiedades.getProperty("urlBase", "").trim();
                if (valor.startsWith("http://") || valor.startsWith("https://")) {
                    return valor.replaceAll("/+$", "");
                }
            } catch (IOException | IllegalArgumentException e) {
                // Archivo ilegible: se usa la dirección por defecto.
            }
        }
        return URL_POR_DEFECTO;
    }

    public static String resolverUrl(String direccion, Duration espera) {
        URI uri;
        try {
            uri = URI.create(direccion);
        } catch (IllegalArgumentException e) {
            return direccion;
        }
        String host = uri.getHost();
        if (host == null || "localhost".equalsIgnoreCase(host) || host.startsWith("127.")) {
            return direccion;
        }
        int puerto = uri.getPort() != -1 ? uri.getPort() : ("https".equals(uri.getScheme()) ? 443 : 80);
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, puerto), (int) espera.toMillis());
            return direccion;
        } catch (IOException e) {
            return direccion.replaceFirst("//" + Pattern.quote(uri.getRawAuthority()),
                    "//localhost" + (uri.getPort() != -1 ? ":" + uri.getPort() : ""));
        }
    }
}
