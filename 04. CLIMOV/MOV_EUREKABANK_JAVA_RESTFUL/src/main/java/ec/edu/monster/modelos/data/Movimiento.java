package ec.edu.monster.modelos.data;

import java.math.BigDecimal;
import java.util.Locale;

public class Movimiento {
    public static final String ACCION_INGRESO = "INGRESO";
    public static final String ACCION_SALIDA = "SALIDA";

    private final String numero;
    private final String fecha;
    private final String tipo;
    private final String accion;
    private final BigDecimal importe;
    private final String cuentaReferencia;

    public Movimiento(String numero, String fecha, String tipo, String accion,
                      BigDecimal importe, String cuentaReferencia) {
        this.numero = numero;
        this.fecha = fecha;
        this.tipo = tipo;
        this.accion = accion;
        this.importe = importe;
        this.cuentaReferencia = cuentaReferencia;
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

    public String getCuentaReferencia() {
        return cuentaReferencia;
    }

    public boolean esIngreso() {
        return accion != null && ACCION_INGRESO.equals(accion.trim().toUpperCase(Locale.ROOT));
    }

    public boolean tieneReferencia() {
        return cuentaReferencia != null && !cuentaReferencia.trim().isEmpty();
    }
}
