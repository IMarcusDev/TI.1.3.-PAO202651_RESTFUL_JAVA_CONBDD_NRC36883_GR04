package ec.edu.monster.modelos.excepciones;

import java.io.IOException;

public class HttpEstadoException extends IOException {
    private final int codigo;

    public HttpEstadoException(int codigo, String url) {
        super("HTTP " + codigo + " en " + url);
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }
}
