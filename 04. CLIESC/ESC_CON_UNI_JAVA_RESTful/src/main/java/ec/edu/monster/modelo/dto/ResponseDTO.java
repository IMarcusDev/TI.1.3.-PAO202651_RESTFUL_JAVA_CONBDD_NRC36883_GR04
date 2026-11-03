package ec.edu.monster.modelo.dto;

import ec.edu.monster.modelo.enums.EstadosEnum;
import org.json.JSONObject;

/** Common envelope of every server response: a status and a message. */
public class ResponseDTO {

    private EstadosEnum estado;
    private String mensaje;

    public ResponseDTO() {
    }

    public ResponseDTO(EstadosEnum estado, String mensaje) {
        this.estado = estado;
        this.mensaje = mensaje;
    }

    public EstadosEnum getEstado() {
        return estado;
    }

    public void setEstado(EstadosEnum estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    /** Reads the status; an unknown or missing status is treated as an error. */
    static EstadosEnum leerEstado(JSONObject json) {
        try {
            return EstadosEnum.valueOf(json.optString("estado", ""));
        } catch (IllegalArgumentException e) {
            return EstadosEnum.ERROR;
        }
    }

    static String leerMensaje(JSONObject json) {
        return json.isNull("mensaje") ? null : json.optString("mensaje", null);
    }
}
