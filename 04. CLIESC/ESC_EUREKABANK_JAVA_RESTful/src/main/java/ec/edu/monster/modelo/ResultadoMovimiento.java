package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * Result of a movement accepted by the server: the movement number, the new balance of the
 * account and the server message.
 */
public record ResultadoMovimiento(int numeroMovimiento, BigDecimal nuevoSaldo, String mensaje) {
}
