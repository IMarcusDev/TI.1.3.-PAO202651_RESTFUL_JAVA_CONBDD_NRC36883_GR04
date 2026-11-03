package ec.edu.monster.modelo;

import org.json.JSONObject;

/**
 * Real implementation of {@link LoginServicio}: calls {@code POST api/login} and turns its
 * answer (or a connection failure) into a session or an exception.
 */
public class LoginServicioRest implements LoginServicio {

    private static final String SIN_TOKEN = "El servidor no devolvió un token de sesión";

    private final String endpoint;
    private final ClienteRest cliente =
            new ClienteRest(ClienteRest.ESPERA_RESPUESTA, "El servidor no respondió a tiempo");

    public LoginServicioRest(Configuracion configuracion) {
        this.endpoint = configuracion.getEndpoint(RutasApi.LOGIN);
    }

    @Override
    public DatosSesion iniciarSesion(String usuario, String clave) throws ServicioException {
        JSONObject credenciales = new JSONObject()
                .put("username", usuario)
                .put("password", clave);

        JSONObject respuesta = cliente.post(endpoint, null, credenciales);

        String token = respuesta.optString("token", "");
        if (token.isBlank()) {
            throw new ServicioException(SIN_TOKEN);
        }
        return new DatosSesion(respuesta.optString("nombre", ""), respuesta.optString("tipoUsuario", ""), token);
    }
}
