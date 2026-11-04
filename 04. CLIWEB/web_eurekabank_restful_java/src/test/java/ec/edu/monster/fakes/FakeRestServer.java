package ec.edu.monster.fakes;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/**
 * Minimal stand-in for the RESTful server: records every request and answers with the
 * status and body set by the test. Listens on a free port of localhost.
 */
public final class FakeRestServer implements AutoCloseable {
  public static final String CONTEXT = "/eurekabank-test";

  private final HttpServer server;
  private final List<String> paths = new ArrayList<>();
  private final List<String> bodies = new ArrayList<>();
  private volatile int status = 200;
  private volatile String body = "{\"estado\":\"OK\",\"mensaje\":\"OK\"}";
  private volatile long delayMillis;

  private FakeRestServer(HttpServer server) {
    this.server = server;
    server.createContext("/", this::handle);
    server.start();
  }

  public static FakeRestServer start() throws IOException {
    return new FakeRestServer(HttpServer.create(new InetSocketAddress("localhost", 0), 0));
  }

  /** Base URL of a server that is not running, to test connection failures. */
  public static String unreachableBaseUrl() throws IOException {
    // Reserve a free port and release it: nothing listens there afterwards, so connecting is refused
    int port;
    try (ServerSocket probe = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
      port = probe.getLocalPort();
    }
    return "http://localhost:" + port + CONTEXT;
  }

  public String baseUrl() {
    return "http://localhost:" + server.getAddress().getPort() + CONTEXT;
  }

  public void answer(int status, String body) {
    this.status = status;
    this.body = body;
  }

  /** Waits this long before answering, to test response timeouts. */
  public void delay(long millis) {
    this.delayMillis = millis;
  }

  public synchronized int calls() {
    return paths.size();
  }

  public synchronized String lastPath() {
    return paths.get(paths.size() - 1);
  }

  public synchronized JSONObject lastBody() {
    return new JSONObject(bodies.get(bodies.size() - 1));
  }

  private void handle(HttpExchange exchange) throws IOException {
    String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    synchronized (this) {
      paths.add(exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath());
      bodies.add(request);
    }
    if (delayMillis > 0) {
      try {
        Thread.sleep(delayMillis);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    byte[] answer = body.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
    exchange.sendResponseHeaders(status, answer.length == 0 ? -1 : answer.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(answer);
    } catch (IOException e) {
      // The client gave up waiting (timeout tests): nothing to do
    }
  }

  @Override
  public void close() {
    server.stop(0);
  }
}
