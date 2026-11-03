package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ec.edu.monster.modelo.Magnitudes;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MagnitudesTest {

    @Test
    void listsTheServerNamesInDisplayOrder() {
        assertEquals(List.of("Longitud", "Masa", "Volumen", "Tiempo", "Temperatura", "Area"),
                Magnitudes.TODAS);
    }

    @Test
    void showsAreaWithItsAccent() {
        assertEquals("Área", Magnitudes.nombreVisible("Area"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Longitud", "Masa", "Volumen", "Tiempo", "Temperatura", "Otra"})
    void keepsEveryOtherNameUnchanged(String magnitud) {
        assertEquals(magnitud, Magnitudes.nombreVisible(magnitud));
    }
}
