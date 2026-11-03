package ec.edu.monster.modelos.data;

/** Respuesta estructurada (RQF-006) ya interpretada por el modelo: estado, mensaje y datos. */
public class Resultado<T> {
    private final boolean exito;
    private final String mensaje;
    private final T datos;
    private final boolean sesionExpirada;

    private Resultado(boolean exito, String mensaje, T datos, boolean sesionExpirada) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.datos = datos;
        this.sesionExpirada = sesionExpirada;
    }

    public static <T> Resultado<T> ok(T datos, String mensaje) {
        return new Resultado<>(true, mensaje, datos, false);
    }

    public static <T> Resultado<T> error(String mensaje) {
        return new Resultado<>(false, mensaje, null, false);
    }

    public static <T> Resultado<T> sesionExpirada(String mensaje) {
        return new Resultado<>(false, mensaje, null, true);
    }

    public boolean isExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public T getDatos() {
        return datos;
    }

    public boolean isSesionExpirada() {
        return sesionExpirada;
    }
}
