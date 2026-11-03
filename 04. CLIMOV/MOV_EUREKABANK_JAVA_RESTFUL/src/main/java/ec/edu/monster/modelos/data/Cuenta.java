package ec.edu.monster.modelos.data;

import java.math.BigDecimal;
import java.util.Locale;

public class Cuenta {
    public static final String ESTADO_ACTIVO = "ACTIVO";

    private final String numero;
    private final String titular;
    private final String moneda;
    private final BigDecimal saldo;
    private final String estado;

    public Cuenta(String numero, String titular, String moneda, BigDecimal saldo, String estado) {
        this.numero = numero;
        this.titular = titular;
        this.moneda = moneda;
        this.saldo = saldo;
        this.estado = estado;
    }

    public Cuenta conSaldo(BigDecimal nuevoSaldo) {
        return new Cuenta(numero, titular, moneda, nuevoSaldo, estado);
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

    public boolean estaActiva() {
        return estado == null || ESTADO_ACTIVO.equals(estado.trim().toUpperCase(Locale.ROOT));
    }
}
