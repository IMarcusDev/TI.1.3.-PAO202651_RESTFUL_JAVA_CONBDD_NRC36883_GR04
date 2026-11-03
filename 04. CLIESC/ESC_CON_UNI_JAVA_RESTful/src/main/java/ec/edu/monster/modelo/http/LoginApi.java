package ec.edu.monster.modelo.http;

import ec.edu.monster.modelo.dto.LoginResponseDTO;
import java.io.IOException;

/** Login operation of the server. */
public interface LoginApi {

    LoginResponseDTO login(String usuario, String clave) throws IOException;
}
