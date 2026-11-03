package ec.edu.monster.modelos.data;

import java.math.BigDecimal;
import java.util.Date;

public class Comprobante {
    private final SolicitudOperacion solicitud;
    private final String numeroMovimiento;
    private final BigDecimal nuevoSaldo;
    private final Date fecha;

    public Comprobante(SolicitudOperacion solicitud, String numeroMovimiento, BigDecimal nuevoSaldo, Date fecha) {
        this.solicitud = solicitud;
        this.numeroMovimiento = numeroMovimiento;
        this.nuevoSaldo = nuevoSaldo;
        this.fecha = fecha;
    }

    public SolicitudOperacion getSolicitud() {
        return solicitud;
    }

    public String getNumeroMovimiento() {
        return numeroMovimiento;
    }

    /** Nuevo saldo de la cuenta, o de la cuenta de origen en una transferencia. */
    public BigDecimal getNuevoSaldo() {
        return nuevoSaldo;
    }

    public Date getFecha() {
        return fecha;
    }
}
