package ec.edu.monster.modelos.enums;

import java.util.Locale;

public enum Perfil {
    EMPLEADO("Empleado"),
    CLIENTE("Cliente");

    private final String etiqueta;

    Perfil(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Interpreta el perfil que devuelve el servidor; si no lo reconoce, usa el que se pidió al iniciar sesión. */
    public static Perfil desde(String texto, Perfil porDefecto) {
        if (texto == null) {
            return porDefecto;
        }
        String normalizado = texto.trim().toUpperCase(Locale.ROOT);
        if (normalizado.contains("EMPLEADO")) {
            return EMPLEADO;
        }
        if (normalizado.contains("CLIENTE")) {
            return CLIENTE;
        }
        return porDefecto;
    }
}
