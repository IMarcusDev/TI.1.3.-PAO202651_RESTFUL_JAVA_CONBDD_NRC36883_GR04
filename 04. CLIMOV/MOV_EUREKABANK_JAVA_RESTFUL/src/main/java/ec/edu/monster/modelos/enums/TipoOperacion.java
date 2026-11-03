package ec.edu.monster.modelos.enums;

public enum TipoOperacion {
    DEPOSITO("registrarDeposito"),
    RETIRO("registrarRetiro"),
    TRANSFERENCIA("registrarTransferencia");

    private final String ruta;

    TipoOperacion(String ruta) {
        this.ruta = ruta;
    }

    /** Ruta de la operación dentro del recurso Movimiento (por ejemplo, Movimiento/registrarDeposito). */
    public String getRuta() {
        return ruta;
    }
}
