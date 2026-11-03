package ec.edu.monster.controlador;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/** Validación de formato en el cliente (ERS 9.1.1 y ECUD). El servidor repite todas las reglas. */
//revisado y aprovado por Marcos Escobar
public final class ValidadorDatos {

    public static final int LARGO_CUENTA = 8;
    public static final int LARGO_CLAVE_CUENTA = 6;
    public static final int MAX_USUARIO = 50;
    public static final int MAX_CLAVE_EMPLEADO = 64;
    public static final int MAX_ENTEROS_IMPORTE = 10;

    public static final String INGRESE_USUARIO = "Ingrese el usuario";
    public static final String INGRESE_CUENTA = "Ingrese el número de cuenta";
    public static final String INGRESE_CLAVE = "Ingrese la clave";
    public static final String INGRESE_IMPORTE = "Ingrese el importe";
    public static final String CUENTA_INVALIDA = "El número de cuenta debe tener 8 dígitos";
    public static final String CLAVE_INVALIDA = "La clave de la cuenta debe tener 6 dígitos";
    public static final String IMPORTE_INVALIDO = "El importe debe ser mayor que cero y tener como máximo dos decimales";
    public static final String CUENTAS_IGUALES = "La cuenta de origen y la de destino deben ser diferentes";

    private static final Pattern CUENTA = Pattern.compile("^\\d{" + LARGO_CUENTA + "}$");
    private static final Pattern CLAVE_CUENTA = Pattern.compile("^\\d{" + LARGO_CLAVE_CUENTA + "}$");
    private static final Pattern IMPORTE = Pattern.compile("^\\d{1," + MAX_ENTEROS_IMPORTE + "}([.,]\\d{0,2})?$");

    private ValidadorDatos() { }

    public static String limpiar(String texto) {
        return texto != null ? texto.trim() : "";
    }

    /** @return el mensaje de error, o null si la cuenta es válida. */
    public static String errorCuenta(String cuenta) {
        if (cuenta.isEmpty()) {
            return INGRESE_CUENTA;
        }
        return CUENTA.matcher(cuenta).matches() ? null : CUENTA_INVALIDA;
    }

    public static String errorClaveCuenta(String clave) {
        if (clave.isEmpty()) {
            return INGRESE_CLAVE;
        }
        return CLAVE_CUENTA.matcher(clave).matches() ? null : CLAVE_INVALIDA;
    }

    public static String errorImporte(String importe) {
        if (importe.isEmpty()) {
            return INGRESE_IMPORTE;
        }
        return convertirImporte(importe) != null ? null : IMPORTE_INVALIDO;
    }

    /** Acepta punto o coma como separador decimal. @return el importe con dos decimales, o null si no es válido. */
    public static BigDecimal convertirImporte(String texto) {
        String limpio = limpiar(texto);
        if (!IMPORTE.matcher(limpio).matches()) {
            return null;
        }
        BigDecimal valor = new BigDecimal(limpio.replace(',', '.')).setScale(2);
        return valor.signum() > 0 ? valor : null;
    }
}
