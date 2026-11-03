package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.enums.EstadosEnum;
import org.json.JSONObject;

//revisado y aprovado por Marcos Escobar
public class LoginResponseDTO extends ResponseDTO {
  private String token;
  private String perfil = "";
  private String nombre = "";

  public LoginResponseDTO(EstadosEnum estado, String mensaje, String token) {
    super(estado, mensaje);
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  public String getPerfil() {
    return perfil;
  }

  public String getNombre() {
    return nombre;
  }

  public static LoginResponseDTO fromJSON(String json) {
    JSONObject obj = new JSONObject(json);

    LoginResponseDTO dto = new LoginResponseDTO(leerEstado(obj), leerTexto(obj, "mensaje"), leerTexto(obj, "token"));
    dto.perfil = leerTexto(obj, "perfil");
    dto.nombre = leerTexto(obj, "nombre");
    return dto;
  }
}
