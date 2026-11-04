package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * Client-side access to the RESTful movements service. Every operation needs the token returned
 * by {@link LoginServicio}.
 */
public interface MovimientoServicio {

    /** Account data and its movements (ECUD2). */
    HistorialCuenta consultarMovimientos(String token, String cuenta) throws ServicioException;

    /** Deposit (employee only): the server takes the employee from the token. */
    ResultadoMovimiento registrarDeposito(String token, String cuenta, BigDecimal importe)
            throws ServicioException;

    /** Withdrawal: an employee sends the account key, a client sends an empty key. */
    ResultadoMovimiento registrarRetiro(String token, String cuenta, BigDecimal importe, String clave)
            throws ServicioException;

    /** Transfer: an employee sends the key of the source account, a client sends an empty key. */
    ResultadoMovimiento registrarTransferencia(String token, String cuentaOrigen, String cuentaDestino,
            BigDecimal importe, String clave) throws ServicioException;
}
