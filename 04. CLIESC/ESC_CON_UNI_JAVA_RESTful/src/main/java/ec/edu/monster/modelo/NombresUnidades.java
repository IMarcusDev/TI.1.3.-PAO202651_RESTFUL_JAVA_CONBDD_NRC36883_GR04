package ec.edu.monster.modelo;

import java.util.Map;

/**
 * Spanish display names for the unit symbols returned by the RESTful server.
 */
public final class NombresUnidades {

    private static final Map<String, String> NOMBRES = Map.ofEntries(
            Map.entry("m", "Metro"),
            Map.entry("cm", "Centímetro"),
            Map.entry("ft", "Pie"),
            Map.entry("yd", "Yarda"),
            Map.entry("mi", "Milla"),
            Map.entry("g", "Gramo"),
            Map.entry("kg", "Kilogramo"),
            Map.entry("oz", "Onza"),
            Map.entry("lb", "Libra"),
            Map.entry("qq", "Quintal"),
            Map.entry("°C", "Celsius"),
            Map.entry("°F", "Fahrenheit"),
            Map.entry("K", "Kelvin"),
            Map.entry("°N", "Newton"),
            Map.entry("°Ré", "Réaumur"),
            Map.entry("mL", "Mililitro"),
            Map.entry("L", "Litro"),
            Map.entry("m3", "Metro cúbico"),
            Map.entry("ft3", "Pie cúbico"),
            Map.entry("gal", "Galón"),
            Map.entry("ms", "Milisegundo"),
            Map.entry("s", "Segundo"),
            Map.entry("min", "Minuto"),
            Map.entry("h", "Hora"),
            Map.entry("d", "Día"),
            Map.entry("w", "Semana"),
            Map.entry("cm2", "Centímetro cuadrado"),
            Map.entry("m2", "Metro cuadrado"),
            Map.entry("km2", "Kilómetro cuadrado"),
            Map.entry("ha", "Hectárea"),
            Map.entry("ft2", "Pie cuadrado"),
            Map.entry("ac", "Acre"));

    private NombresUnidades() {
    }

    /** Returns the Spanish name of the symbol, or the symbol itself when unknown. */
    public static String nombre(String simbolo) {
        return simbolo == null ? null : NOMBRES.getOrDefault(simbolo, simbolo);
    }
}
