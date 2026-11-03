package ec.edu.monster.modelo.http;

import ec.edu.monster.modelo.dto.CatalogResponseDTO;
import ec.edu.monster.modelo.dto.ConversorResponseDTO;
import java.io.IOException;

/** Conversion and catalog operations of the server; both need the session token. */
public interface ConversorApi {

    ConversorResponseDTO convert(String token, String type, double value, String from, String to) throws IOException;

    CatalogResponseDTO catalog(String token, String type) throws IOException;
}
