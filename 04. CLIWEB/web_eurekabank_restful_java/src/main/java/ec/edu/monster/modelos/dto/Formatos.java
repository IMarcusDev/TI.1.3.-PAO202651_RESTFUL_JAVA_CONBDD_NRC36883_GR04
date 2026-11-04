package ec.edu.monster.modelos.dto;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Display formats of the ECUD general parameters: dates as dd/MM/yyyy HH:mm, amounts with two
 * decimals and a dollar sign.
 */
public final class Formatos {

  private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
  private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private Formatos() {
  }

  /** Server date (ISO, e.g. 2026-11-03T09:15:00) as 03/11/2026 09:15; anything else unchanged. */
  public static String fecha(String iso) {
    if (iso == null || iso.isBlank()) {
      return "";
    }
    try {
      return LocalDateTime.parse(iso).format(FECHA);
    } catch (DateTimeParseException e) {
      return iso;
    }
  }

  /** Today's date as dd/MM/yyyy, used on the receipt. */
  public static String hoy() {
    return LocalDateTime.now().format(FECHA_CORTA);
  }

  /** Dollar amount as in the UI template, e.g. $ 1,250.50 (EurekaBank works only in USD). */
  public static String dinero(BigDecimal valor) {
    if (valor == null) {
      return "";
    }
    // DecimalFormat is not thread-safe: a new instance per call
    DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
    return "$ " + formato.format(valor);
  }

  /** Reference account of a movement, or "-" when it has none (UI template). */
  public static String referencia(String cuenta) {
    return cuenta == null || cuenta.isBlank() ? "-" : cuenta;
  }

  /** Movement number with six digits, e.g. 000006. */
  public static String numeroMovimiento(int numero) {
    return String.format("%06d", numero);
  }
}
