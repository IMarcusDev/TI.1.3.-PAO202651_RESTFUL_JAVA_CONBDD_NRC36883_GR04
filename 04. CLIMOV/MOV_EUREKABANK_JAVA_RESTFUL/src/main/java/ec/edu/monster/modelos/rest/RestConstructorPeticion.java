package ec.edu.monster.modelos.rest;

import java.math.BigDecimal;
import org.json.JSONException;
import org.json.JSONObject;

/** Cuerpo JSON de una petición: un objeto plano con los parámetros de la operación. */
public class RestConstructorPeticion {

    private final String ruta;
    private final JSONObject cuerpo = new JSONObject();

    public RestConstructorPeticion(String ruta) {
        this.ruta = ruta;
    }

    public RestConstructorPeticion parametro(String nombre, String valor) {
        if (valor != null) {
            poner(nombre, valor);
        }
        return this;
    }

    /** El importe viaja como número JSON con dos decimales (120.50). */
    public RestConstructorPeticion parametro(String nombre, BigDecimal valor) {
        if (valor != null) {
            poner(nombre, valor);
        }
        return this;
    }

    public String getRuta() {
        return ruta;
    }

    public String construir() {
        return cuerpo.toString();
    }

    private void poner(String nombre, Object valor) {
        try {
            cuerpo.put(nombre, valor);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Parámetro no válido: " + nombre, e);
        }
    }
}
