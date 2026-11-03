package ec.edu.monster.modelos.repositorios;

import ec.edu.monster.BuildConfig;
import ec.edu.monster.modelos.excepciones.HttpEstadoException;
import ec.edu.monster.modelos.excepciones.RespuestaInvalidaException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;

public final class RestErrores {

    public static final String SIN_CONEXION = "Sin conexión de red. Revise el Wi-Fi e intente nuevamente.";
    public static final String SIN_SERVIDOR = "No se pudo conectar con el servidor. Verifique la red e intente nuevamente.";
    public static final String RESPUESTA_INCOMPLETA = "El servidor envió una respuesta vacía o incompleta.";
    public static final String SESION_EXPIRADA = "Su sesión expiró o no es válida. Inicie sesión nuevamente.";

    private RestErrores() { }

    public static String mensaje(Throwable ex) {
        String mensaje = mensajeAmigable(ex);
        if (BuildConfig.DEBUG) {
            Throwable causa = causaRaiz(ex);
            mensaje += "\n[Debug] " + causa.getClass().getSimpleName() + ": " + causa.getMessage();
        }
        return mensaje;
    }

    private static String mensajeAmigable(Throwable ex) {
        // HTTP 4xx/5xx sin respuesta estructurada: ruta inexistente o error interno del servidor.
        if (ex instanceof HttpEstadoException) {
            return "El servidor no pudo procesar la solicitud (HTTP " + ((HttpEstadoException) ex).getCodigo()
                    + "). Intente más tarde.";
        }
        if (ex instanceof RespuestaInvalidaException) {
            return RESPUESTA_INCOMPLETA;
        }
        if (ex instanceof MalformedURLException) {
            return "La dirección del servidor está mal configurada.";
        }
        // ECUD: sin respuesta en 15 s, servidor apagado o fuera de la red.
        if (ex instanceof IOException) {
            return SIN_SERVIDOR;
        }
        return "Ocurrió un error inesperado al contactar al servidor.";
    }

    private static Throwable causaRaiz(Throwable ex) {
        Throwable causa = ex;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        return causa;
    }

    public static String mensajeServidor(String mensaje, String porDefecto) {
        return mensaje == null || mensaje.trim().isEmpty() ? porDefecto : mensaje.trim();
    }

    /** ECUD4: "Token inválido o sesión expirada". */
    public static boolean esTokenInvalido(String mensaje) {
        if (mensaje == null) {
            return false;
        }
        String texto = mensaje.toLowerCase(Locale.ROOT);
        return texto.contains("token") || texto.contains("sesión expirada") || texto.contains("sesion expirada");
    }
}
