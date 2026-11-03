package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelo.ConversorServicio;
import ec.edu.monster.modelo.ServicioException;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.modelo.dto.CatalogResponseDTO;
import ec.edu.monster.modelo.http.ConversorApi;
import ec.edu.monster.modelo.dto.ConversorResponseDTO;
import ec.edu.monster.modelo.dto.LoginResponseDTO;
import ec.edu.monster.modelo.http.LoginApi;
import java.io.IOException;
import java.util.List;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

class ConversorServicioTest {

    private final Sesion sesion = new Sesion();

    @Test
    void storesTokenWhenLoginSucceeds() throws ServicioException {
        ConversorServicio servicio = new ConversorServicio(
                (usuario, clave) -> loginOk("tok-1"), new ConversorFalso(), sesion);

        servicio.iniciarSesion("user", "1234");

        assertTrue(sesion.estaActiva());
        assertEquals("user", sesion.getUsuario());
        assertEquals("tok-1", sesion.getToken());
    }

    @Test
    void throwsServerMessageWhenLoginFails() {
        ConversorServicio servicio = new ConversorServicio(
                (usuario, clave) -> loginError("Credenciales incorrectas"), new ConversorFalso(), sesion);

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.iniciarSesion("user", "mala"));

        assertEquals("Credenciales incorrectas", error.getMessage());
        assertFalse(sesion.estaActiva());
    }

    @Test
    void convertsUsingSessionToken() throws ServicioException {
        ConversorFalso conversor = new ConversorFalso();
        conversor.respuestaConversion = conversionOk(328.084);
        ConversorServicio servicio = servicioConSesion(conversor);

        double resultado = servicio.convertir("Longitud", 100, "m", "ft");

        assertEquals(328.084, resultado);
        assertEquals("tok-1", conversor.ultimoToken);
    }

    @Test
    void throwsServerMessageWhenConversionFails() {
        ConversorFalso conversor = new ConversorFalso();
        conversor.respuestaConversion = conversionError("Unidad no soportada");
        ConversorServicio servicio = servicioConSesion(conversor);

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.convertir("Longitud", 1, "m", "xx"));

        assertEquals("Unidad no soportada", error.getMessage());
    }

    @Test
    void rejectsConversionWithoutSession() {
        ConversorServicio servicio = new ConversorServicio(
                (usuario, clave) -> loginOk("tok-1"), new ConversorFalso(), sesion);

        assertThrows(ServicioException.class, () -> servicio.convertir("Longitud", 1, "m", "ft"));
    }

    @Test
    void returnsCatalogUnits() throws ServicioException {
        ConversorFalso conversor = new ConversorFalso();
        conversor.respuestaCatalogo = catalogoOk("m", "ft", "km");
        ConversorServicio servicio = servicioConSesion(conversor);

        assertEquals(List.of("m", "ft", "km"), servicio.catalogo("Longitud"));
    }

    @Test
    void translatesConnectionFailures() {
        ConversorServicio servicio = new ConversorServicio(
                (usuario, clave) -> {
                    throw new IOException("connection refused");
                }, new ConversorFalso(), sesion);

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.iniciarSesion("user", "1234"));

        assertEquals("No se pudo conectar con el servidor", error.getMessage());
    }

    @Test
    void rejectsSuccessfulLoginWithoutToken() {
        ConversorServicio servicio = new ConversorServicio(
                (usuario, clave) -> loginOk(null), new ConversorFalso(), sesion);

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.iniciarSesion("user", "1234"));

        assertEquals("El servidor no devolvió un token de sesión", error.getMessage());
        assertFalse(sesion.estaActiva());
    }

    @Test
    void reportsMalformedResponseInsteadOfConnectionError() {
        ConversorServicio servicio = new ConversorServicio(
                (usuario, clave) -> {
                    throw new JSONException("not json");
                }, new ConversorFalso(), sesion);

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.iniciarSesion("user", "1234"));

        assertEquals("El servidor no devolvió una respuesta", error.getMessage());
    }

    @Test
    void usesGenericMessageWhenLoginErrorHasNoMessage() {
        ConversorServicio servicio = new ConversorServicio(
                (usuario, clave) -> loginError(null), new ConversorFalso(), sesion);

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.iniciarSesion("user", "1234"));

        assertEquals("El servidor respondió con un error", error.getMessage());
    }

    @Test
    void usesGenericMessageWhenConversionErrorHasNoMessage() {
        ConversorFalso conversor = new ConversorFalso();
        conversor.respuestaConversion = conversionError(" ");
        ConversorServicio servicio = servicioConSesion(conversor);

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.convertir("Longitud", 1, "m", "ft"));

        assertEquals("El servidor respondió con un error", error.getMessage());
    }

    @Test
    void rejectsNullLoginResponse() {
        ConversorServicio servicio = new ConversorServicio(
                (usuario, clave) -> null, new ConversorFalso(), sesion);

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.iniciarSesion("user", "1234"));

        assertEquals("El servidor no devolvió una respuesta", error.getMessage());
    }

    @Test
    void failedReloginClearsPreviousSession() {
        sesion.iniciar("anterior", "tok-viejo");
        ConversorServicio servicio = new ConversorServicio(
                (usuario, clave) -> loginError("Credenciales incorrectas"), new ConversorFalso(), sesion);

        assertThrows(ServicioException.class, () -> servicio.iniciarSesion("otro", "mala"));

        assertFalse(sesion.estaActiva());
    }

    @Test
    void returnsEmptyCatalogWhenServerSendsNoUnits() throws ServicioException {
        ConversorFalso conversor = new ConversorFalso();
        CatalogResponseDTO vacio = new CatalogResponseDTO();
        vacio.setEstado(ec.edu.monster.modelo.enums.EstadosEnum.OK);
        conversor.respuestaCatalogo = vacio;
        ConversorServicio servicio = servicioConSesion(conversor);

        assertEquals(List.of(), servicio.catalogo("Longitud"));
    }

    @Test
    void throwsServerMessageWhenCatalogFails() {
        ConversorFalso conversor = new ConversorFalso();
        CatalogResponseDTO fallido = new CatalogResponseDTO();
        fallido.setEstado(ec.edu.monster.modelo.enums.EstadosEnum.ERROR);
        fallido.setMensaje("Magnitud no soportada");
        conversor.respuestaCatalogo = fallido;
        ConversorServicio servicio = servicioConSesion(conversor);

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.catalogo("Energia"));

        assertEquals("Magnitud no soportada", error.getMessage());
    }

    @Test
    void closesSession() throws ServicioException {
        ConversorServicio servicio = servicioConSesion(new ConversorFalso());

        servicio.cerrarSesion();

        assertFalse(sesion.estaActiva());
    }

    private ConversorServicio servicioConSesion(ConversorApi conversor) {
        sesion.iniciar("user", "tok-1");
        return new ConversorServicio((usuario, clave) -> loginOk("tok-1"), conversor, sesion);
    }

    private static LoginResponseDTO loginOk(String token) {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setEstado(ec.edu.monster.modelo.enums.EstadosEnum.OK);
        dto.setToken(token);
        return dto;
    }

    private static LoginResponseDTO loginError(String mensaje) {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setEstado(ec.edu.monster.modelo.enums.EstadosEnum.ERROR);
        dto.setMensaje(mensaje);
        return dto;
    }

    private static ConversorResponseDTO conversionOk(double resultado) {
        ConversorResponseDTO dto = new ConversorResponseDTO();
        dto.setEstado(ec.edu.monster.modelo.enums.EstadosEnum.OK);
        dto.setResultado(resultado);
        return dto;
    }

    private static ConversorResponseDTO conversionError(String mensaje) {
        ConversorResponseDTO dto = new ConversorResponseDTO();
        dto.setEstado(ec.edu.monster.modelo.enums.EstadosEnum.ERROR);
        dto.setMensaje(mensaje);
        return dto;
    }

    private static CatalogResponseDTO catalogoOk(String... unidades) {
        CatalogResponseDTO dto = new CatalogResponseDTO();
        dto.setEstado(ec.edu.monster.modelo.enums.EstadosEnum.OK);
        dto.setResultado(List.of(unidades));
        return dto;
    }

    private static final class ConversorFalso implements ConversorApi {

        private ConversorResponseDTO respuestaConversion = conversionOk(0);
        private CatalogResponseDTO respuestaCatalogo = catalogoOk();
        private String ultimoToken;

        @Override
        public ConversorResponseDTO convert(String token, String type, double value, String from, String to) {
            ultimoToken = token;
            return respuestaConversion;
        }

        @Override
        public CatalogResponseDTO catalog(String token, String type) {
            ultimoToken = token;
            return respuestaCatalogo;
        }
    }
}
