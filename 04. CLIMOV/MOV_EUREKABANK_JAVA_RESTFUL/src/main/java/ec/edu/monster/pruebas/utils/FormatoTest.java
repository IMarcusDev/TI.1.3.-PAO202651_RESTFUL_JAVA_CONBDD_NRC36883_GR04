package ec.edu.monster.pruebas.utils;

import static org.junit.Assert.assertEquals;

import ec.edu.monster.utils.Formato;
import java.math.BigDecimal;
import org.junit.Test;

public class FormatoTest {

    @Test
    public void importe_conDosDecimalesYMoneda() {
        assertEquals("1.250,50 USD", Formato.importe(new BigDecimal("1250.5"), "USD"));
        assertEquals("0,00", Formato.importe(BigDecimal.ZERO));
        assertEquals(Formato.SIN_DATO, Formato.importe(null, "USD"));
    }

    @Test
    public void fecha_delServidorSeMuestraComoDiaMesAnioHoraMinuto() {
        assertEquals("31/10/2026 16:40", Formato.fecha("2026-10-31T16:40:12.345-05:00"));
        assertEquals("31/10/2026 16:40", Formato.fecha("2026-10-31 16:40:12"));
        assertEquals("31/10/2026", Formato.fecha("2026-10-31"));
        assertEquals("31/10/2026 16:40", Formato.fecha("31/10/2026 16:40"));
        assertEquals(Formato.SIN_DATO, Formato.fecha(""));
    }
}
