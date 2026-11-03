package ec.edu.monster.modelo;

import ec.edu.monster.modelo.dto.CatalogResponseDTO;
import ec.edu.monster.modelo.dto.ConversorResponseDTO;
import ec.edu.monster.modelo.dto.LoginResponseDTO;
import ec.edu.monster.modelo.dto.ResponseDTO;
import ec.edu.monster.modelo.enums.EstadosEnum;
import ec.edu.monster.modelo.http.ConversorApi;
import ec.edu.monster.modelo.http.ConversorRestApi;
import ec.edu.monster.modelo.http.LoginApi;
import ec.edu.monster.modelo.http.LoginRestApi;
import ec.edu.monster.modelo.http.RESTfulClient;
import java.io.IOException;
import java.util.List;
import org.json.JSONException;

/**
 * Single entry point to the RESTful server. Controllers depend on this class,
 * never on the HTTP layer.
 */
public class ConversorServicio {

    private static final String SIN_CONEXION = "No se pudo conectar con el servidor";
    private static final String SIN_SESION = "La sesión no está activa";
    private static final String SIN_RESPUESTA = "El servidor no devolvió una respuesta";
    private static final String SIN_TOKEN = "El servidor no devolvió un token de sesión";
    private static final String ERROR_GENERICO = "El servidor respondió con un error";

    private final LoginApi login;
    private final ConversorApi conversor;
    private final Sesion sesion;

    public ConversorServicio(LoginApi login, ConversorApi conversor, Sesion sesion) {
        this.login = login;
        this.conversor = conversor;
        this.sesion = sesion;
    }

    public static ConversorServicio crear(Sesion sesion) {
        RESTfulClient cliente = new RESTfulClient();
        return new ConversorServicio(new LoginRestApi(cliente), new ConversorRestApi(cliente), sesion);
    }

    public void iniciarSesion(String usuario, String clave) throws ServicioException {
        // A new login attempt replaces the previous session, even if it fails.
        sesion.cerrar();
        LoginResponseDTO respuesta = llamar(() -> login.login(usuario, clave));
        if (respuesta == null) {
            throw new ServicioException(SIN_RESPUESTA);
        }
        if (respuesta.getEstado() != EstadosEnum.OK) {
            throw new ServicioException(mensajeOGenerico(respuesta.getMensaje()));
        }
        if (respuesta.getToken() == null || respuesta.getToken().isBlank()) {
            throw new ServicioException(SIN_TOKEN);
        }
        sesion.iniciar(usuario, respuesta.getToken());
    }

    public double convertir(String magnitud, double valor, String desde, String hacia) throws ServicioException {
        String token = tokenActivo();
        ConversorResponseDTO respuesta = llamar(() -> conversor.convert(token, magnitud, valor, desde, hacia));
        validar(respuesta);
        return respuesta.getResultado();
    }

    public List<String> catalogo(String magnitud) throws ServicioException {
        String token = tokenActivo();
        CatalogResponseDTO respuesta = llamar(() -> conversor.catalog(token, magnitud));
        validar(respuesta);
        if (respuesta.getResultado() == null) {
            return List.of();
        }
        return List.copyOf(respuesta.getResultado());
    }

    public void cerrarSesion() {
        sesion.cerrar();
    }

    private String tokenActivo() throws ServicioException {
        if (!sesion.estaActiva()) {
            throw new ServicioException(SIN_SESION);
        }
        return sesion.getToken();
    }

    private static void validar(ResponseDTO respuesta) throws ServicioException {
        if (respuesta == null) {
            throw new ServicioException(SIN_RESPUESTA);
        }
        if (respuesta.getEstado() != EstadosEnum.OK) {
            throw new ServicioException(mensajeOGenerico(respuesta.getMensaje()));
        }
    }

    @FunctionalInterface
    private interface Llamada<T> {

        T ejecutar() throws IOException;
    }

    private static <T> T llamar(Llamada<T> llamada) throws ServicioException {
        try {
            return llamada.ejecutar();
        } catch (JSONException e) {
            // The server answered, but not with the expected JSON (e.g. an HTML error page).
            throw new ServicioException(SIN_RESPUESTA, e);
        } catch (IOException e) {
            throw new ServicioException(SIN_CONEXION, e);
        }
    }

    private static String mensajeOGenerico(String mensaje) {
        return mensaje == null || mensaje.isBlank() ? ERROR_GENERICO : mensaje;
    }
}
