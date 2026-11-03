package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ec.edu.monster.modelo.FormatoResultado;
import org.junit.jupiter.api.Test;

class FormatoResultadoTest {

    @Test
    void formatsResultWithFourDecimalsAndComma() {
        assertEquals("100 m = 328,0840 ft",
                FormatoResultado.formatear(100, "m", 328.083989, "ft"));
    }

    @Test
    void formatsInputWithoutTrailingZeros() {
        assertEquals("2,5 kg = 2500,0000 g",
                FormatoResultado.formatear(2.5, "kg", 2500, "g"));
    }
}