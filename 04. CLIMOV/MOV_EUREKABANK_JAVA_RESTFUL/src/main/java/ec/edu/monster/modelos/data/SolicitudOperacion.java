package ec.edu.monster.modelos.data;

import ec.edu.monster.modelos.enums.TipoOperacion;
import java.math.BigDecimal;

/** Datos ya validados de un depósito, retiro o transferencia, listos para enviar al servidor. */
public class SolicitudOperacion {
    private final TipoOperacion tipo;
    private final String cuenta;
    private final String cuentaDestino;
    private final BigDecimal importe;
    private final String clave;

    public SolicitudOperacion(TipoOperacion tipo, String cuenta, String cuentaDestino,
                              BigDecimal importe, String clave) {
        this.tipo = tipo;
        this.cuenta = cuenta;
        this.cuentaDestino = cuentaDestino;
        this.importe = importe;
        this.clave = clave;
    }

    public TipoOperacion getTipo() {
        return tipo;
    }

    /** Cuenta del depósito o del retiro, o cuenta de origen de la transferencia. */
    public String getCuenta() {
        return cuenta;
    }

    public String getCuentaDestino() {
        return cuentaDestino;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    /** Clave de la cuenta; vacía cuando opera el propio cliente o en un depósito. */
    public String getClave() {
        return clave;
    }
}
