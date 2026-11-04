package ec.edu.monster.modelos.http;

import java.time.Duration;

/** Datos de la sesión y del servidor. El perfil decide el menú y la cuenta del cliente es la única que puede operar. */
//revisado y aprovado por Marcos Escobar
public final class ConfigHTTP {
  public static final String EMPLEADO = "EMPLEADO";
  public static final String CLIENTE = "CLIENTE";
  public static final Duration TIMEOUT = Duration.ofSeconds(15);

  private static String baseUrl = "http://localhost:8080/WS_EUREKABANK_JAVA_RESTFul/api";
  private static String token = "";
  private static String perfil = "";
  private static String nombre = "";
  private static String cuenta = "";
  private static Transporte transporte;

  private ConfigHTTP() {
  }

  public static String getBaseUrl() {
    return baseUrl;
  }

  public static void setBaseUrl(String baseUrl) {
    ConfigHTTP.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  public static String getToken() {
    return token;
  }

  public static void setToken(String token) {
    ConfigHTTP.token = token == null ? "" : token;
  }

  public static String getPerfil() {
    return perfil;
  }

  public static void setPerfil(String perfil) {
    ConfigHTTP.perfil = perfil == null ? "" : perfil;
  }

  public static String getNombre() {
    return nombre;
  }

  public static void setNombre(String nombre) {
    ConfigHTTP.nombre = nombre == null ? "" : nombre;
  }

  public static String getCuenta() {
    return cuenta;
  }

  public static void setCuenta(String cuenta) {
    ConfigHTTP.cuenta = cuenta == null ? "" : cuenta;
  }

  public static Transporte getTransporte() {
    if (transporte == null)
      transporte = new TransporteHttp();
    return transporte;
  }

  public static void setTransporte(Transporte transporte) {
    ConfigHTTP.transporte = transporte;
  }

  public static boolean haySesion() {
    return !token.isEmpty();
  }

  public static boolean esCliente() {
    return CLIENTE.equals(perfil);
  }

  public static void limpiar() {
    token = "";
    perfil = "";
    nombre = "";
    cuenta = "";
  }
}
