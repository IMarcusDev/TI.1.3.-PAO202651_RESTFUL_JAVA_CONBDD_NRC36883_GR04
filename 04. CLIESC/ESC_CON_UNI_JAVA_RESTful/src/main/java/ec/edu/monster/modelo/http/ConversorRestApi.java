package ec.edu.monster.modelo.http;

import ec.edu.monster.modelo.dto.CatalogResponseDTO;
import ec.edu.monster.modelo.dto.ConversorResponseDTO;
import ec.edu.monster.modelo.enums.EndpointsEnum;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * {@link ConversorApi} over HTTP: POST /converter with a JSON body and
 * GET /catalog/{type}; the token goes in the {@code Authorization} header.
 */
public class ConversorRestApi implements ConversorApi {

    private final RESTfulClient cliente;

    public ConversorRestApi(RESTfulClient cliente) {
        this.cliente = cliente;
    }

    @Override
    public ConversorResponseDTO convert(String token, String type, double value, String from, String to)
            throws IOException {
        JSONObject cuerpo = new JSONObject()
                .put("type", type)
                .put("value", value)
                .put("from", from)
                .put("to", to);
        return ConversorResponseDTO.fromJSON(cliente.post(EndpointsEnum.converter, cuerpo.toString(), token));
    }

    @Override
    public CatalogResponseDTO catalog(String token, String type) throws IOException {
        String recurso = EndpointsEnum.catalog + "/" + URLEncoder.encode(type, StandardCharsets.UTF_8);
        return CatalogResponseDTO.fromJSON(cliente.get(recurso, token));
    }
}
