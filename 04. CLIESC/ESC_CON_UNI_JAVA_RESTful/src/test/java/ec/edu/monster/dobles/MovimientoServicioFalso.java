package ec.edu.monster.dobles;

import ec.edu.monster.modelo.HistorialCuenta;
import ec.edu.monster.modelo.MovimientoServicio;
import ec.edu.monster.modelo.ResultadoMovimiento;
import ec.edu.monster.modelo.ServicioException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Movement service double: records the call and returns a fixed result or the
 * configured error.
 */
public class MovimientoServicioFalso implements MovimientoServicio {

    private ServicioException error;
    private int llamadas;
    private String tokenRecibido;
    private String origenRecibido;
    private String destinoRecibido;
    private BigDecimal valorRecibido;
    private String claveRecibida;

    public void fallarCon(String mensaje) {
        this.error = new ServicioException(mensaje);
    }

    @Override
    public HistorialCuenta consultarMovimientos(String token, String cuenta) throws ServicioException {
        llamadas++;
        tokenRecibido = token;
        origenRecibido = cuenta;
        if (error != null) {
            throw error;
        }
        return new HistorialCuenta(new HistorialCuenta.CuentaConsultada(cuenta, "Titular", "USD",
                new BigDecimal("500.00"), "ACTIVO"), List.of());
    }

    @Override
    public ResultadoMovimiento registrarDeposito(String token, String cuenta, BigDecimal importe)
            throws ServicioException {
        return registrar(token, cuenta, null, importe, null);
    }

    @Override
    public ResultadoMovimiento registrarRetiro(String token, String cuenta, BigDecimal importe, String clave)
            throws ServicioException {
        return registrar(token, cuenta, null, importe, clave);
    }

    @Override
    public ResultadoMovimiento registrarTransferencia(String token, String cuentaOrigen, String cuentaDestino,
            BigDecimal importe, String clave) throws ServicioException {
        return registrar(token, cuentaOrigen, cuentaDestino, importe, clave);
    }

    private ResultadoMovimiento registrar(String token, String cuentaOrigen, String cuentaDestino,
            BigDecimal valor, String clave) throws ServicioException {
        llamadas++;
        tokenRecibido = token;
        origenRecibido = cuentaOrigen;
        destinoRecibido = cuentaDestino;
        valorRecibido = valor;
        claveRecibida = clave;
        if (error != null) {
            throw error;
        }
        return new ResultadoMovimiento(777, new BigDecimal("500.00"), "Movimiento registrado");
    }

    public int getLlamadas() {
        return llamadas;
    }

    public String getTokenRecibido() {
        return tokenRecibido;
    }

    public String getOrigenRecibido() {
        return origenRecibido;
    }

    public String getDestinoRecibido() {
        return destinoRecibido;
    }

    public BigDecimal getValorRecibido() {
        return valorRecibido;
    }

    public String getClaveRecibida() {
        return claveRecibida;
    }
}
