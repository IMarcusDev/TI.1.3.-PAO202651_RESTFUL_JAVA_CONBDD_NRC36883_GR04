package ec.edu.monster.modelos.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Answer of "Consultar movimientos": account details and transaction history.
 */
public record HistorialCuenta(
    EstadosEnum estado,
    String mensaje,
    CuentaConsultada cuenta,
    List<MovimientoConsultado> movimientos) {

  public HistorialCuenta {
    movimientos = movimientos == null ? List.of() : List.copyOf(movimientos);
  }

  public boolean sinMovimientos() {
    return movimientos.isEmpty();
  }

  public static HistorialCuenta ok(
      String mensaje, CuentaConsultada cuenta, List<MovimientoConsultado> movimientos) {
    return new HistorialCuenta(EstadosEnum.OK, mensaje, cuenta, movimientos);
  }

  public static HistorialCuenta fallo(EstadosEnum estado, String mensaje) {
    return new HistorialCuenta(estado, mensaje, null, List.of());
  }

  public static HistorialCuenta error(String mensaje) {
    return fallo(EstadosEnum.ERROR, mensaje);
  }

  /** Account data shown above the table. */
  public record CuentaConsultada(
      String numero, String titular, String moneda, BigDecimal saldo, String estado) {

    public String saldoFormateado() {
      return Formatos.dinero(saldo);
    }

    public String getNumero() {
      return numero;
    }

    public String getTitular() {
      return titular;
    }

    public String getMoneda() {
      return moneda;
    }

    public BigDecimal getSaldo() {
      return saldo;
    }

    public String getEstado() {
      return estado;
    }

    public String getSaldoFormateado() {
      return saldoFormateado();
    }
  }

  /** One movement row: number and date already formatted, amount as a BigDecimal. */
  public record MovimientoConsultado(
      String numero,
      String fecha,
      String tipo,
      String accion,
      BigDecimal importe,
      String referencia) {

    public String importeFormateado() {
      return Formatos.dinero(importe);
    }

    public boolean esIngreso() {
      return "INGRESO".equals(accion);
    }

    public String getNumero() {
      return numero;
    }

    public String getFecha() {
      return fecha;
    }

    public String getTipo() {
      return tipo;
    }

    public String getAccion() {
      return accion;
    }

    public BigDecimal getImporte() {
      return importe;
    }

    public String getReferencia() {
      return referencia;
    }

    public String getImporteFormateado() {
      return importeFormateado();
    }

    public boolean isIngreso() {
      return esIngreso();
    }
  }
}
