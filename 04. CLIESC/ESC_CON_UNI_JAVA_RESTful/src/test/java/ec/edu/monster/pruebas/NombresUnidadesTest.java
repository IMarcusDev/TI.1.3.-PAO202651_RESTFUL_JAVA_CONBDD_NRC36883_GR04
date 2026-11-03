package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import ec.edu.monster.modelo.NombresUnidades;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class NombresUnidadesTest {

    @ParameterizedTest
    @CsvSource({
        "m, Metro", "cm, Centímetro", "ft, Pie", "yd, Yarda", "mi, Milla",
        "g, Gramo", "kg, Kilogramo", "oz, Onza", "lb, Libra", "qq, Quintal",
        "°C, Celsius", "°F, Fahrenheit", "K, Kelvin", "°N, Newton", "°Ré, Réaumur",
        "mL, Mililitro", "L, Litro", "m3, Metro cúbico", "ft3, Pie cúbico", "gal, Galón",
        "ms, Milisegundo", "s, Segundo", "min, Minuto", "h, Hora", "d, Día", "w, Semana",
        "cm2, Centímetro cuadrado", "m2, Metro cuadrado", "km2, Kilómetro cuadrado",
        "ha, Hectárea", "ft2, Pie cuadrado", "ac, Acre"
    })
    void returnsTheSpanishNameOfEveryServerSymbol(String simbolo, String nombre) {
        assertEquals(nombre, NombresUnidades.nombre(simbolo));
    }

    @ParameterizedTest
    @ValueSource(strings = {"xyz", "M", ""})
    void fallsBackToTheSymbolWhenUnknown(String simbolo) {
        assertEquals(simbolo, NombresUnidades.nombre(simbolo));
    }

    @Test
    void returnsNullForANullSymbol() {
        assertNull( NombresUnidades.nombre(null));
    }
}
