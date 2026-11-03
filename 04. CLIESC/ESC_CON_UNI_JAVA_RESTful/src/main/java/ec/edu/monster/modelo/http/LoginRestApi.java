package ec.edu.monster.modelo.http;

import ec.edu.monster.modelo.dto.LoginResponseDTO;
import ec.edu.monster.modelo.enums.EndpointsEnum;
import java.io.IOException;
import org.json.JSONObject;

/** {@link LoginApi} over HTTP: POST /login with a JSON body and no token. */
public class LoginRestApi implements LoginApi {

    private final RESTfulClient cliente;

    public LoginRestApi(RESTfulClient cliente) {
        this.cliente = cliente;
    }

    @Override
    public LoginResponseDTO login(String usuario, String clave) throws IOException {
        JSONObject cuerpo = new JSONObject()
                .put("username", usuario)
                .put("password", clave);
        return LoginResponseDTO.fromJSON(cliente.post(EndpointsEnum.login, cuerpo.toString(), null));
    }
}
