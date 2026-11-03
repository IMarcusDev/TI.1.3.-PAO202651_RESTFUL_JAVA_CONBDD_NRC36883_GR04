package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * Client-side access to the RESTful movements service. Every operation needs the
 * token returned by {@link LoginServicio#iniciarSesion(String, String)}.
 */
public interface MovimientoServicio {

    ResultadoMovimiento deposito(String token, String cuentaOrigen, String cuentaDestino, BigDecimal valor)
            throws ServicioException;

    ResultadoMovimiento retiro(String token, String cuentaOrigen, String cuentaDestino, BigDecimal valor)
            throws ServicioException;

    ResultadoMovimiento transferencia(String token, String cuentaOrigen, String cuentaDestino, BigDecimal valor)
            throws ServicioException;
}
