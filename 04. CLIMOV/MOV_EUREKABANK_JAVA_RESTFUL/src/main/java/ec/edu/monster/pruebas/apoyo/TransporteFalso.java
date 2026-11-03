package ec.edu.monster.pruebas.apoyo;

import ec.edu.monster.modelos.rest.RespuestaHttp;
import ec.edu.monster.modelos.rest.RestTransporte;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransporteFalso extends RestTransporte {

    public interface Respuesta {
        RespuestaHttp para(String url, String json) throws IOException;
    }

    private final Respuesta respuesta;
    private final List<String> urls = Collections.synchronizedList(new ArrayList<>());
    private final List<String> cuerpos = Collections.synchronizedList(new ArrayList<>());

    public TransporteFalso(Respuesta respuesta) {
        this.respuesta = respuesta;
    }

    public static TransporteFalso responde(String json) {
        return responde(200, json);
    }

    public static TransporteFalso responde(int codigo, String cuerpo) {
        return new TransporteFalso((url, json) -> new RespuestaHttp(codigo, cuerpo));
    }

    public static TransporteFalso lanza(IOException error) {
        return new TransporteFalso((url, json) -> {
            throw error;
        });
    }

    @Override
    public RespuestaHttp enviar(String url, String json) throws IOException {
        urls.add(url);
        cuerpos.add(json);
        return respuesta.para(url, json);
    }

    public int peticiones() {
        return cuerpos.size();
    }

    public String ultimaUrl() {
        return urls.get(urls.size() - 1);
    }

    public String ultimoCuerpo() {
        return cuerpos.get(cuerpos.size() - 1);
    }

    public static final String HTML_PAYARA = "<!DOCTYPE html><html><head><title>Payara Server - Error report</title>"
            + "</head><body><h1>HTTP Status 404 - Not Found</h1></body></html>";
}
