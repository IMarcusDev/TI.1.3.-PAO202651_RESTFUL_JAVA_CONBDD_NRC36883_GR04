package ec.edu.monster.dobles;

import ec.edu.monster.modelo.MovimientoServicio;
import ec.edu.monster.modelo.ResultadoMovimiento;
import ec.edu.monster.modelo.ServicioException;
import java.math.BigDecimal;

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

    public void fallarCon(String mensaje) {
        this.error = new ServicioException(mensaje);
    }

    @Override
    public ResultadoMovimiento deposito(String token, String cuentaOrigen, String cuentaDestino, BigDecimal valor)
            throws ServicioException {
        return registrar(token, cuentaOrigen, cuentaDestino, valor);
    }

    @Override
    public ResultadoMovimiento retiro(String token, String cuentaOrigen, String cuentaDestino, BigDecimal valor)
            throws ServicioException {
        return registrar(token, cuentaOrigen, cuentaDestino, valor);
    }

    @Override
    public ResultadoMovimiento transferencia(String token, String cuentaOrigen, String cuentaDestino,
            BigDecimal valor) throws ServicioException {
        return registrar(token, cuentaOrigen, cuentaDestino, valor);
    }

    private ResultadoMovimiento registrar(String token, String cuentaOrigen, String cuentaDestino, BigDecimal valor)
            throws ServicioException {
        llamadas++;
        tokenRecibido = token;
        origenRecibido = cuentaOrigen;
        destinoRecibido = cuentaDestino;
        valorRecibido = valor;
        if (error != null) {
            throw error;
        }
        return new ResultadoMovimiento(777, "Movimiento registrado");
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
}
