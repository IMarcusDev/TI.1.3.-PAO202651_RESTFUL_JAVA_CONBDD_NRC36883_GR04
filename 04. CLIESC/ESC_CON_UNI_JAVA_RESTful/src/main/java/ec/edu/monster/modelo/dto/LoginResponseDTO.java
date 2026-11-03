package ec.edu.monster.modelo.dto;

import ec.edu.monster.modelo.enums.EstadosEnum;
import org.json.JSONObject;

/** Response of POST /login. */
public class LoginResponseDTO extends ResponseDTO {

    private String token;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(EstadosEnum estado, String mensaje, String token) {
        super(estado, mensaje);
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    /** @throws org.json.JSONException when the text is not a JSON object */
    public static LoginResponseDTO fromJSON(String texto) {
        JSONObject json = new JSONObject(texto);
        return new LoginResponseDTO(leerEstado(json), leerMensaje(json),
                json.isNull("token") ? null : json.optString("token", null));
    }
}
