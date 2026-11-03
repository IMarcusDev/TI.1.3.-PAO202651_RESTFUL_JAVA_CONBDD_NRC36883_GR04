package ec.edu.monster.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** RNF-009: importes con dos decimales y la moneda de la cuenta; fechas con el formato dd/MM/yyyy HH:mm. */
public final class Formato {

    public static final String SIN_DATO = "—";

    private static final Pattern FECHA_ISO =
            Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})(?:[T ](\\d{2}):(\\d{2}))?.*$");

    private Formato() { }

    public static String importe(BigDecimal valor) {
        if (valor == null) {
            return SIN_DATO;
        }
        DecimalFormat formato = new DecimalFormat("#,##0.00", simbolos());
        formato.setRoundingMode(RoundingMode.HALF_UP);
        return formato.format(valor);
    }

    public static String importe(BigDecimal valor, String moneda) {
        String texto = importe(valor);
        return valor == null || moneda == null || moneda.trim().isEmpty() ? texto : texto + " " + moneda.trim();
    }

    /** Muestra la fecha del servidor con el formato del ERS; si no la reconoce, la deja como llegó. */
    public static String fecha(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return SIN_DATO;
        }
        Matcher iso = FECHA_ISO.matcher(texto.trim());
        if (!iso.matches()) {
            return texto.trim();
        }
        String fecha = iso.group(3) + "/" + iso.group(2) + "/" + iso.group(1);
        return iso.group(4) != null ? fecha + " " + iso.group(4) + ":" + iso.group(5) : fecha;
    }

    public static String fecha(Date fecha) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ROOT).format(fecha);
    }

    private static DecimalFormatSymbols simbolos() {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setDecimalSeparator(',');
        simbolos.setGroupingSeparator('.');
        simbolos.setMinusSign('-');
        return simbolos;
    }
}
