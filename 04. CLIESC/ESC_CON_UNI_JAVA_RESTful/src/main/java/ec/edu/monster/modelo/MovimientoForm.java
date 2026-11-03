package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validation of the withdrawal / deposit / transfer forms. Same rules and
 * messages as the web client. Errors are kept per field name (cuenta, empleado,
 * origen, destino, importe) so the view can show each message under its input.
 */
public final class MovimientoForm {

    // Account ids are varchar(30) in the database
    static final int MAX_CUENTA = 30;

    // Up to 10 integer digits and at most 2 decimals, "." as separator (after normalizing ",")
    private static final Pattern IMPORTE = Pattern.compile("\\d{1,10}(\\.\\d{1,2})?");

    private static final String IMPORTE_VACIO = "Ingrese el importe";
    private static final String IMPORTE_INVALIDO =
            "El importe debe ser mayor que cero y tener como máximo dos decimales";
    private static final String CUENTA_LARGA = "La cuenta debe tener como máximo 30 caracteres";
    private static final String CUENTAS_IGUALES =
            "La cuenta de origen y la de destino deben ser diferentes";

    private final Map<String, String> errores = new LinkedHashMap<>();
    private String cuenta;
    private String empleado;
    private String origen;
    private String destino;
    private BigDecimal importe;

    private MovimientoForm() {
    }

    /** Withdrawal: one account (the one the money leaves) and the amount. */
    public static MovimientoForm retiro(String cuenta, String importe) {
        MovimientoForm form = new MovimientoForm();
        form.cuenta = form.validarCuenta("cuenta", cuenta, "Ingrese el número de cuenta");
        form.importe = form.validarImporte(importe);
        return form;
    }

    /** Deposit: employee code (sent as the source id), target account and the amount. */
    public static MovimientoForm deposito(String empleado, String cuenta, String importe) {
        MovimientoForm form = new MovimientoForm();
        form.empleado = form.validarCuenta("empleado", empleado, "Ingrese el código de empleado");
        form.cuenta = form.validarCuenta("cuenta", cuenta, "Ingrese la cuenta de destino");
        form.importe = form.validarImporte(importe);
        return form;
    }

    /** Transfer: source and target accounts (must differ) and the amount. */
    public static MovimientoForm transferencia(String origen, String destino, String importe) {
        MovimientoForm form = new MovimientoForm();
        form.origen = form.validarCuenta("origen", origen, "Ingrese la cuenta de origen");
        form.destino = form.validarCuenta("destino", destino, "Ingrese la cuenta de destino");
        if (!form.errores.containsKey("origen") && !form.errores.containsKey("destino")
                && form.origen.equals(form.destino)) {
            form.errores.put("destino", CUENTAS_IGUALES);
        }
        form.importe = form.validarImporte(importe);
        return form;
    }

    /** Formats an amount as "$ 1,250.00" (US style, always 2 decimals). */
    public static String formatearImporte(BigDecimal valor) {
        // DecimalFormat is not thread-safe: a new instance per call
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "$ " + formato.format(valor);
    }

    public boolean isValido() {
        return errores.isEmpty();
    }

    public Map<String, String> getErrores() {
        return Collections.unmodifiableMap(errores);
    }

    public String getCuenta() {
        return cuenta;
    }

    public String getEmpleado() {
        return empleado;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    /** Parsed amount, or null when it is invalid. */
    public BigDecimal getImporte() {
        return importe;
    }

    public String getImporteFormateado() {
        return importe == null ? null : formatearImporte(importe);
    }

    // Returns the trimmed value (empty string when missing) and records the error, if any
    private String validarCuenta(String campo, String valor, String mensajeVacio) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty()) {
            errores.put(campo, mensajeVacio);
        } else if (limpio.length() > MAX_CUENTA) {
            errores.put(campo, CUENTA_LARGA);
        }
        return limpio;
    }

    private BigDecimal validarImporte(String valor) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty()) {
            errores.put("importe", IMPORTE_VACIO);
            return null;
        }
        // Accept "," as decimal separator; the regex rejects mixed or repeated separators
        String normalizado = limpio.replace(',', '.');
        if (!IMPORTE.matcher(normalizado).matches()) {
            errores.put("importe", IMPORTE_INVALIDO);
            return null;
        }
        BigDecimal numero = new BigDecimal(normalizado);
        if (numero.signum() <= 0) {
            errores.put("importe", IMPORTE_INVALIDO);
            return null;
        }
        return numero;
    }
}
