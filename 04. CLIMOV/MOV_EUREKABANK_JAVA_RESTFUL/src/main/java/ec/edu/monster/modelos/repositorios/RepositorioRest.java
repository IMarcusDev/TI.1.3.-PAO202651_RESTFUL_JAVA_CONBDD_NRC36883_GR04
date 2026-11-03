package ec.edu.monster.modelos.repositorios;

import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.excepciones.HttpEstadoException;
import ec.edu.monster.modelos.excepciones.RespuestaInvalidaException;
import ec.edu.monster.modelos.interfaces.VerificadorRed;
import ec.edu.monster.modelos.rest.RestConstructorPeticion;
import ec.edu.monster.modelos.rest.RestLectorRespuesta;
import ec.edu.monster.modelos.rest.RestTransporte;
import java.io.IOException;
import java.math.BigDecimal;

/** Base de los repositorios REST: envía la petición y traduce la respuesta estructurada del servidor. */
abstract class RepositorioRest {

    private final String urlRecurso;
    private final RestTransporte transporte;
    private final VerificadorRed red;

    protected RepositorioRest(String urlBase, String recurso, RestTransporte transporte, VerificadorRed red) {
        this.urlRecurso = (urlBase.endsWith("/") ? urlBase : urlBase + "/") + recurso + "/";
        this.transporte = transporte;
        this.red = red;
    }

    protected RestConstructorPeticion peticion(String operacion) {
        return new RestConstructorPeticion(operacion);
    }

    protected boolean sinRed() {
        return red.sinRed();
    }

    protected RestLectorRespuesta enviar(RestConstructorPeticion peticion)
            throws IOException, RespuestaInvalidaException, HttpEstadoException {
        String url = urlRecurso + peticion.getRuta();
        return new RestLectorRespuesta(transporte.enviar(url, peticion.construir()), url);
    }

    /** Convierte una respuesta con estado ERROR en un resultado, distinguiendo el token inválido. */
    protected static <T> Resultado<T> fallo(RestLectorRespuesta respuesta, String porDefecto) {
        String mensaje = RestErrores.mensajeServidor(respuesta.texto("mensaje"), porDefecto);
        return RestErrores.esTokenInvalido(mensaje)
                ? Resultado.sesionExpirada(RestErrores.SESION_EXPIRADA)
                : Resultado.error(mensaje);
    }

    protected static BigDecimal decimal(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected static String textoOVacio(String texto) {
        return texto != null ? texto : "";
    }
}
