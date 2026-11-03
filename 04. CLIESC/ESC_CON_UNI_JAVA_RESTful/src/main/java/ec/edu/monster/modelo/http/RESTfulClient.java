package ec.edu.monster.modelo.http;

import ec.edu.monster.modelo.Configuracion;
import ec.edu.monster.modelo.enums.EndpointsEnum;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Minimal HTTP transport for the RESTful server. The session token travels in
 * the {@code Authorization} header; it is omitted when the token is null
 * (login). The body is returned as text whatever the status code, because the
 * server reports its errors inside a JSON body.
 */
public class RESTfulClient {

    public static final String URL_BASE = "http://localhost:8080/WS_CON_UNI_JAVA_RESTFUL/api/";

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final String urlBase;
    private final HttpClient http;

    public RESTfulClient() {
        this(Configuracion.url());
    }

    public RESTfulClient(String urlBase) {
        this.urlBase = urlBase.endsWith("/") ? urlBase : urlBase + "/";
        // The server is shared by several clients and may stall: fail instead of waiting forever.
        this.http = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
    }

    public String get(String recurso, String token) throws IOException {
        return enviar(constructor(recurso, token).GET().build());
    }

    public String post(EndpointsEnum endpoint, String json, String token) throws IOException {
        return enviar(constructor(endpoint.toString(), token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build());
    }

    private HttpRequest.Builder constructor(String recurso, String token) {
        HttpRequest.Builder constructor = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + recurso))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json");
        if (token != null) {
            constructor.header("Authorization", token);
        }
        return constructor;
    }

    private String enviar(HttpRequest solicitud) throws IOException {
        try {
            return http.send(solicitud, HttpResponse.BodyHandlers.ofString()).body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Solicitud interrumpida");
        }
    }
}
