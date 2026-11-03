package ec.edu.monster.modelo.dto;

import ec.edu.monster.modelo.enums.EstadosEnum;
import org.json.JSONObject;

/** Response of POST /converter. */
public class ConversorResponseDTO extends ResponseDTO {

    private double resultado;

    public ConversorResponseDTO() {
    }

    public ConversorResponseDTO(EstadosEnum estado, String mensaje, double resultado) {
        super(estado, mensaje);
        this.resultado = resultado;
    }

    public double getResultado() {
        return resultado;
    }

    public void setResultado(double resultado) {
        this.resultado = resultado;
    }

    /** @throws org.json.JSONException when the text is not a JSON object */
    public static ConversorResponseDTO fromJSON(String texto) {
        JSONObject json = new JSONObject(texto);
        return new ConversorResponseDTO(leerEstado(json), leerMensaje(json), json.optDouble("resultado", 0));
    }
}
