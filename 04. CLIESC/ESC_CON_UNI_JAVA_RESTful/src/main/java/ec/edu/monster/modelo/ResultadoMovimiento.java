package ec.edu.monster.modelo;

/**
 * Result of a movement accepted by the server: the generated id and its message.
 */
public record ResultadoMovimiento(int idMovimiento, String mensaje) {
}
