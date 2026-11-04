package ec.edu.monster.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads the RESTful server location from config.properties (classpath)
 * and builds the base URL used by the client services.
 */
public class WsConfig {
  // File copied by Maven from src/main/resources to WEB-INF/classes
  private static final String CONFIG_FILE = "config.properties";

  // Loaded once, when the class is first used
  private static final Properties PROPS = load();

  private WsConfig() {
    // Utility class: only static methods, no instances
  }

  /** e.g. http://localhost:8080/WS_EUREKABANK_JAVA_RESTFul */
  public static String getBaseUrl() {
    return "http://" + get("ws.host") + ":" + get("ws.port") + "/" + get("ws.context");
  }

  /** Full address of one route, e.g. getEndpoint("api/Login/loginEmpleado") */
  public static String getEndpoint(String route) {
    return getBaseUrl() + "/" + route;
  }

  // Returns a required key, failing with a clear message when it is missing or empty
  private static String get(String key) {
    String value = PROPS.getProperty(key);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Missing '" + key + "' in " + CONFIG_FILE);
    }
    return value.trim();
  }

  // Reads the file from the classpath, so it works inside the deployed .war
  private static Properties load() {
    Properties props = new Properties();
    try (InputStream in = WsConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
      if (in == null) {
        throw new IllegalStateException(CONFIG_FILE + " not found in the classpath");
      }
      props.load(in);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read " + CONFIG_FILE, e);
    }
    return props;
  }
}
