package ec.edu.monster.modelo;

import java.util.List;

/**
 * Magnitude names exactly as the RESTful server expects them (case-sensitive),
 * in the order the screens list them.
 */
public final class Magnitudes {

    public static final List<String> TODAS =
            List.of("Longitud", "Masa", "Volumen", "Tiempo", "Temperatura", "Area");

    private Magnitudes() {
    }

    /** Text shown to the user for a server magnitude name. */
    public static String nombreVisible(String magnitud) {
        return "Area".equals(magnitud) ? "Área" : magnitud;
    }
}
