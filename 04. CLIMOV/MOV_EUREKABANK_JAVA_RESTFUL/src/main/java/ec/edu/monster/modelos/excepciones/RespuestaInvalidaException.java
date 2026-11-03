package ec.edu.monster.modelos.excepciones;

public class RespuestaInvalidaException extends Exception {
    public RespuestaInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
