package ec.edu.monster.modelos.rest;

import ec.edu.monster.modelos.excepciones.HttpEstadoException;
import ec.edu.monster.modelos.excepciones.RespuestaInvalidaException;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Lee el JSON de la respuesta por nombre de campo, sin importar mayúsculas, el nivel de anidamiento
 * ni si el estado llega como texto ("OK") o como número (0 = OK, 1 = ERROR, como un enum de .NET).
 */
public class RestLectorRespuesta {

    public static final String ESTADO_OK = "OK";
    public static final String ESTADO_ERROR = "ERROR";

    private final JSONObject raiz;

    public RestLectorRespuesta(RespuestaHttp respuesta, String url)
            throws RespuestaInvalidaException, HttpEstadoException {
        this.raiz = parsear(respuesta, url);

        // Un 404 o un 500 sin la respuesta estructurada (por ejemplo, la página de error del servidor).
        if (!respuesta.esExitosa() && valor("estado") == null) {
            throw new HttpEstadoException(respuesta.getCodigo(), url);
        }
    }

    public String estado() {
        Object valor = valor("estado");
        if (valor instanceof Number) {
            int numero = ((Number) valor).intValue();
            return numero == 0 ? ESTADO_OK : numero == 1 ? ESTADO_ERROR : String.valueOf(numero);
        }
        return valor != null ? valor.toString().trim().toUpperCase(Locale.ROOT) : null;
    }

    public boolean estadoOk() {
        return ESTADO_OK.equals(estado());
    }

    public String texto(String nombre) {
        return comoTexto(valor(nombre));
    }

    /**
     * Devuelve, como mapas nombre-valor, los objetos que contienen el campo indicado.
     * Así se leen las listas (por ejemplo, los movimientos) sin depender de cómo se llame el arreglo.
     */
    public List<Map<String, String>> registros(String campoClave) {
        List<Map<String, String>> registros = new ArrayList<>();
        for (JSONObject objeto : objetos()) {
            if (clave(objeto, campoClave) == null) {
                continue;
            }
            Map<String, String> registro = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            Iterator<String> claves = objeto.keys();
            while (claves.hasNext()) {
                String nombre = claves.next();
                Object valor = objeto.opt(nombre);
                if (!(valor instanceof JSONObject) && !(valor instanceof JSONArray)) {
                    registro.put(nombre, comoTexto(valor));
                }
            }
            registros.add(registro);
        }
        return registros;
    }

    /** Primer valor simple con ese nombre, buscando nivel por nivel desde la raíz. */
    private Object valor(String nombre) {
        for (JSONObject objeto : objetos()) {
            String clave = clave(objeto, nombre);
            if (clave != null) {
                Object valor = objeto.opt(clave);
                if (!(valor instanceof JSONObject) && !(valor instanceof JSONArray)) {
                    return valor == JSONObject.NULL ? null : valor;
                }
            }
        }
        return null;
    }

    /** Todos los objetos del documento en orden de anchura, respetando el orden de los arreglos. */
    private List<JSONObject> objetos() {
        List<JSONObject> objetos = new ArrayList<>();
        Deque<Object> pendientes = new ArrayDeque<>();
        pendientes.add(raiz);
        while (!pendientes.isEmpty()) {
            Object actual = pendientes.poll();
            if (actual instanceof JSONObject) {
                JSONObject objeto = (JSONObject) actual;
                objetos.add(objeto);
                Iterator<String> claves = objeto.keys();
                while (claves.hasNext()) {
                    Object hijo = objeto.opt(claves.next());
                    if (hijo instanceof JSONObject || hijo instanceof JSONArray) {
                        pendientes.add(hijo);
                    }
                }
            } else if (actual instanceof JSONArray) {
                JSONArray arreglo = (JSONArray) actual;
                for (int i = 0; i < arreglo.length(); i++) {
                    Object hijo = arreglo.opt(i);
                    if (hijo instanceof JSONObject || hijo instanceof JSONArray) {
                        pendientes.add(hijo);
                    }
                }
            }
        }
        return objetos;
    }

    private static String clave(JSONObject objeto, String nombre) {
        if (objeto.has(nombre)) {
            return nombre;
        }
        Iterator<String> claves = objeto.keys();
        while (claves.hasNext()) {
            String clave = claves.next();
            if (clave.equalsIgnoreCase(nombre)) {
                return clave;
            }
        }
        return null;
    }

    private static String comoTexto(Object valor) {
        if (valor == null || valor == JSONObject.NULL) {
            return null;
        }
        if (valor instanceof Double || valor instanceof Float) {
            return new BigDecimal(valor.toString()).toPlainString();
        }
        if (valor instanceof BigDecimal) {
            return ((BigDecimal) valor).toPlainString();
        }
        return valor.toString().trim();
    }

    private static JSONObject parsear(RespuestaHttp respuesta, String url)
            throws RespuestaInvalidaException, HttpEstadoException {
        String cuerpo = respuesta.getCuerpo().trim();
        try {
            if (cuerpo.isEmpty()) {
                throw new RespuestaInvalidaException("Respuesta vacía", null);
            }
            return new JSONObject(cuerpo);
        } catch (JSONException | RespuestaInvalidaException e) {
            if (!respuesta.esExitosa()) {
                throw new HttpEstadoException(respuesta.getCodigo(), url);
            }
            throw e instanceof RespuestaInvalidaException
                    ? (RespuestaInvalidaException) e
                    : new RespuestaInvalidaException("La respuesta no es un JSON válido", e);
        }
    }
}
