package ec.edu.monster.modelo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class FormatoResultado {

    private static final DecimalFormatSymbols SYMBOLS = DecimalFormatSymbols.getInstance(Locale.ROOT);

    static {
        SYMBOLS.setDecimalSeparator(',');
    }

    private FormatoResultado() {
    }

    public static String formatear(double valor, String desde, double resultado, String hacia) {
        DecimalFormat entrada = new DecimalFormat("0.####", SYMBOLS);
        DecimalFormat salida = new DecimalFormat("0.0000", SYMBOLS);
        return entrada.format(valor) + " " + desde + " = " + salida.format(resultado) + " " + hacia;
    }
}