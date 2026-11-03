package ec.edu.monster.modelos.data;

import ec.edu.monster.modelos.enums.Campo;

public class ValidacionResultado {
    private static final ValidacionResultado VALIDO = new ValidacionResultado(true, Campo.NINGUNO, null);

    private final boolean valido;
    private final Campo campo;
    private final String mensaje;

    private ValidacionResultado(boolean valido, Campo campo, String mensaje) {
        this.valido = valido;
        this.campo = campo;
        this.mensaje = mensaje;
    }

    public static ValidacionResultado valido() {
        return VALIDO;
    }

    public static ValidacionResultado error(Campo campo, String mensaje) {
        return new ValidacionResultado(false, campo, mensaje);
    }

    public boolean isValido() {
        return valido;
    }

    public Campo getCampo() {
        return campo;
    }

    public String getMensaje() {
        return mensaje;
    }
}
