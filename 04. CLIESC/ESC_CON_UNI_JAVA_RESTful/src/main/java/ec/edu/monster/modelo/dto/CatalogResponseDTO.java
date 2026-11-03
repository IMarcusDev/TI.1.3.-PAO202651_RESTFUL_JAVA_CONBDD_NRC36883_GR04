package ec.edu.monster.modelo.dto;

import ec.edu.monster.modelo.enums.EstadosEnum;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** Response of GET /catalog/{type}: the unit symbols in the order the server sent them. */
public class CatalogResponseDTO extends ResponseDTO {

    private List<String> resultado;

    public CatalogResponseDTO() {
    }

    public CatalogResponseDTO(EstadosEnum estado, String mensaje, List<String> resultado) {
        super(estado, mensaje);
        this.resultado = resultado;
    }

    /** Units, or null when the server sent none. */
    public List<String> getResultado() {
        return resultado;
    }

    public void setResultado(List<String> resultado) {
        this.resultado = resultado;
    }

    /** @throws org.json.JSONException when the text is not a JSON object */
    public static CatalogResponseDTO fromJSON(String texto) {
        JSONObject json = new JSONObject(texto);
        JSONArray arreglo = json.optJSONArray("resultado");
        List<String> unidades = null;
        if (arreglo != null) {
            unidades = new ArrayList<>();
            for (int i = 0; i < arreglo.length(); i++) {
                unidades.add(arreglo.getString(i));
            }
        }
        return new CatalogResponseDTO(leerEstado(json), leerMensaje(json), unidades);
    }
}
