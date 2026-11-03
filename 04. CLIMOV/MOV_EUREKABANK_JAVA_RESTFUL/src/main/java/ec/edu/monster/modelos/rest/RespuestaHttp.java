package ec.edu.monster.modelos.rest;

public class RespuestaHttp {
    private final int codigo;
    private final String cuerpo;

    public RespuestaHttp(int codigo, String cuerpo) {
        this.codigo = codigo;
        this.cuerpo = cuerpo != null ? cuerpo : "";
    }

    public int getCodigo() {
        return codigo;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public boolean esExitosa() {
        return codigo >= 200 && codigo <= 299;
    }
}
