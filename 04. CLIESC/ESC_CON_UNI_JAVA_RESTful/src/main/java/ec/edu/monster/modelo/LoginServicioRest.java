package ec.edu.monster.modelo;

import org.json.JSONObject;

/**
 * Real implementation of {@link LoginServicio}: calls {@code POST api/Login/...} and turns its
 * answer (or a connection failure) into session data or an exception.
 */
public class LoginServicioRest implements LoginServicio {

    private static final String SIN_TOKEN = "El servidor no devolvió un token de sesión";

    private final Configuracion configuracion;
    // A login or logout that times out did nothing that matters: same text as a dead server.
    // The login sends no token, so only the logout treats a 401 as an expired session.
    private final ClienteRest cliente =
            new ClienteRest(ClienteRest.ESPERA_RESPUESTA, ServicioException.SIN_CONEXION, false);
    private final ClienteRest clienteConSesion =
            new ClienteRest(ClienteRest.ESPERA_RESPUESTA, ServicioException.SIN_CONEXION, true);

    public LoginServicioRest(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    @Override
    public DatosSesion loginEmpleado(String usuario, String clave) throws ServicioException {
        return iniciar(RutasApi.LOGIN_EMPLEADO, new JSONObject().put("usuario", usuario).put("clave", clave));
    }

    @Override
    public DatosSesion loginCliente(String cuenta, String clave) throws ServicioException {
        return iniciar(RutasApi.LOGIN_CLIENTE, new JSONObject().put("cuenta", cuenta).put("clave", clave));
    }

    @Override
    public void logout(String token) throws ServicioException {
        clienteConSesion.post(configuracion.getEndpoint(RutasApi.LOGOUT), new JSONObject().put("token", token));
    }

    private DatosSesion iniciar(String ruta, JSONObject credenciales) throws ServicioException {
        JSONObject respuesta = cliente.post(configuracion.getEndpoint(ruta), credenciales);

        String token = respuesta.optString("token", "");
        if (token.isBlank()) {
            throw new ServicioException(SIN_TOKEN);
        }
        String tipoUsuario = respuesta.optString("tipoUsuario", "");
        return new DatosSesion(respuesta.optString("nombre", ""), tipoUsuario,
                perfil(respuesta.optString("perfil", ""), tipoUsuario), token);
    }

    // Server profile; derived from the user type when the field comes empty
    private static String perfil(String perfil, String tipoUsuario) {
        String valor = perfil.isBlank() ? tipoUsuario.trim() : perfil.trim();
        return DatosSesion.CLIENTE.equalsIgnoreCase(valor) ? DatosSesion.CLIENTE : DatosSesion.EMPLEADO;
    }
}
