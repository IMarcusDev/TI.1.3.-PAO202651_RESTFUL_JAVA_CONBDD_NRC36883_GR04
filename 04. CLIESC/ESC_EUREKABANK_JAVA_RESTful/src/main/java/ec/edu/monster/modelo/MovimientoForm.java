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
 * messages as the web client. Errors are kept per field name (cuenta, origen,
 * destino, importe, clave) so the view can show each message under its input.
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
    private static final String CLAVE_VACIA = "Ingrese la clave de la cuenta";

    private final Map<String, String> errores = new LinkedHashMap<>();
    private String cuenta;
    private String origen;
    private String destino;
    private String clave = "";
    private BigDecimal importe;

    private MovimientoForm() {
    }

    /**
     * Withdrawal of a Cliente: only their own account and the amount; the server
     * ties the token to the account, so no key is sent.
     */
    public static MovimientoForm retiro(String cuenta, String importe) {
        return retiro(cuenta, "", importe, false);
    }

    /** Withdrawal of an Empleado: any account, its key and the amount. */
    public static MovimientoForm retiroEmpleado(String cuenta, String clave, String importe) {
        return retiro(cuenta, clave, importe, true);
    }

    /** Deposit: the ERS server takes the employee from the token; only account and amount. */
    public static MovimientoForm deposito(String cuenta, String importe) {
        MovimientoForm form = new MovimientoForm();
        form.cuenta = form.validarCuenta("cuenta", cuenta, "Ingrese la cuenta de destino");
        form.importe = form.validarImporte(importe);
        return form;
    }

    /**
     * Transfer of a Cliente: only the target account (the source is their own) and
     * the amount; the server ties the token to the source account, so no key is sent.
     */
    public static MovimientoForm transferencia(String origen, String destino, String importe) {
        return transferencia(origen, destino, "", importe, false);
    }

    /** Transfer of an Empleado: both accounts, the key of the origin and the amount. */
    public static MovimientoForm transferenciaEmpleado(String origen, String destino, String clave,
            String importe) {
        return transferencia(origen, destino, clave, importe, true);
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

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    /** Account key of an Empleado, or "" for a Cliente. */
    public String getClave() {
        return clave;
    }

    /** Parsed amount, or null when it is invalid. */
    public BigDecimal getImporte() {
        return importe;
    }

    public String getImporteFormateado() {
        return importe == null ? null : formatearImporte(importe);
    }

    private static MovimientoForm retiro(String cuenta, String clave, String importe, boolean requiereClave) {
        MovimientoForm form = new MovimientoForm();
        form.cuenta = form.validarCuenta("cuenta", cuenta, "Ingrese el número de cuenta");
        form.clave = form.validarClave(clave, requiereClave);
        form.importe = form.validarImporte(importe);
        return form;
    }

    private static MovimientoForm transferencia(String origen, String destino, String clave,
            String importe, boolean requiereClave) {
        MovimientoForm form = new MovimientoForm();
        form.origen = form.validarCuenta("origen", origen, "Ingrese la cuenta de origen");
        form.destino = form.validarCuenta("destino", destino, "Ingrese la cuenta de destino");
        if (!form.errores.containsKey("origen") && !form.errores.containsKey("destino")
                && form.origen.equals(form.destino)) {
            form.errores.put("destino", CUENTAS_IGUALES);
        }
        form.clave = form.validarClave(clave, requiereClave);
        form.importe = form.validarImporte(importe);
        return form;
    }

    // The key is sent as typed (the server compares it with the stored one): no trim
    private String validarClave(String valor, boolean requerida) {
        if (!requerida) {
            return "";
        }
        String tecleada = valor == null ? "" : valor;
        if (tecleada.isBlank()) {
            errores.put("clave", CLAVE_VACIA);
        }
        return tecleada;
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
