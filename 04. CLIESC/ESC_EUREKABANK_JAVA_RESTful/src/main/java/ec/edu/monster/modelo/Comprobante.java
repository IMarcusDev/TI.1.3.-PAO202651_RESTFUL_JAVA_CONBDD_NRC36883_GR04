package ec.edu.monster.modelo;

/**
 * Everything the receipt shows after a deposit, withdrawal or transfer.
 */
public record Comprobante(String operacion, String cuentaOrigen, String cuentaDestino,
                          String importe, int idMovimiento, String fecha, String mensaje) {
}
