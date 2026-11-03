package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import ec.edu.monster.controlador.CatalogoControlador;
import ec.edu.monster.modelo.ConversorServicio;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.vista.CatalogoVista;
import ec.edu.monster.modelo.dto.CatalogResponseDTO;
import ec.edu.monster.modelo.http.ConversorApi;
import ec.edu.monster.modelo.dto.ConversorResponseDTO;
import ec.edu.monster.modelo.enums.EstadosEnum;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CatalogoControladorTest {

    private final Sesion sesion = new Sesion();
    private final VistaCatalogoFalsa vista = new VistaCatalogoFalsa();
    private final CatalogoFalso conversor = new CatalogoFalso();

    @BeforeEach
    void iniciarSesion() {
        sesion.iniciar("user", "tok-1");
    }

    @Test
    void showsTheSixMagnitudesAndUnitsOfTheFirstOnStart() {
        conversor.unidades = List.of("m", "cm", "ft");

        crearControlador();

        assertEquals(List.of("Longitud", "Masa", "Volumen", "Tiempo", "Temperatura", "Area"), vista.magnitudes);
        assertEquals(List.of("Longitud"), conversor.pedidos);
        assertEquals(List.of("m", "cm", "ft"), vista.unidades);
    }

    @Test
    void loadsUnitsExactlyOnceOnStart() {
        conversor.unidades = List.of("m", "cm", "ft");

        crearControlador();

        assertEquals(1, conversor.pedidos.size());
    }

    @Test
    void showsUnitsOfTheNewlySelectedMagnitude() {
        crearControlador();
        conversor.unidades = List.of("s", "min", "h");

        vista.seleccionarMagnitud("Tiempo");

        assertEquals("Tiempo", conversor.pedidos.getLast());
        assertEquals(List.of("s", "min", "h"), vista.unidades);
    }

    @Test
    void clearsUnitsAndShowsServerMessageWhenCatalogFails() {
        conversor.unidades = List.of("m2", "ha");
        crearControlador();
        conversor.falla = true;

        vista.seleccionarMagnitud("Masa");

        assertEquals(List.of(), vista.unidades);
        assertEquals("Magnitud no soportada", vista.error);
    }

    @Test
    void clearsPreviousErrorWhenCatalogLoads() {
        conversor.falla = true;
        crearControlador();
        conversor.falla = false;
        conversor.unidades = List.of("g", "kg");

        vista.seleccionarMagnitud("Masa");

        assertNull(vista.error);
        assertEquals(List.of("g", "kg"), vista.unidades);
    }

    @Test
    void skipsServerWhenNoMagnitudeIsSelected() {
        conversor.unidades = List.of("m2", "ha");
        crearControlador();
        int pedidosPrevios = conversor.pedidos.size();

        vista.seleccionarMagnitud(null);

        assertEquals(pedidosPrevios, conversor.pedidos.size());
        assertEquals(List.of(), vista.unidades);
    }

    private void crearControlador() {
        new CatalogoControlador(vista, new ConversorServicio(
                (usuario, clave) -> {
                    throw new AssertionError("Catalog must not log in");
                }, conversor, sesion));
    }

    private static final class VistaCatalogoFalsa implements CatalogoVista {

        private List<String> magnitudes = List.of();
        private List<String> unidades = List.of();
        private String magnitud;
        private String error;
        private Runnable accionCambiarMagnitud;

        void seleccionarMagnitud(String nueva) {
            magnitud = nueva;
            accionCambiarMagnitud.run();
        }

        @Override
        public void mostrarMagnitudes(List<String> lista) {
            magnitudes = lista;
            magnitud = lista.isEmpty() ? null : lista.getFirst();
            // Mirrors the view contract: the first magnitude is notified like a user selection.
            if (accionCambiarMagnitud != null && !lista.isEmpty()) {
                accionCambiarMagnitud.run();
            }
        }

        @Override
        public String getMagnitud() {
            return magnitud;
        }

        @Override
        public void mostrarUnidades(List<String> lista) {
            unidades = lista;
        }

        @Override
        public void mostrarError(String mensaje) {
            error = mensaje;
        }

        @Override
        public void limpiarMensajes() {
            error = null;
        }

        @Override
        public void alCambiarMagnitud(Runnable accion) {
            accionCambiarMagnitud = accion;
        }
    }

    private static final class CatalogoFalso implements ConversorApi {

        private List<String> unidades = List.of();
        private boolean falla;
        private final List<String> pedidos = new ArrayList<>();

        @Override
        public ConversorResponseDTO convert(String token, String type, double value, String from, String to) {
            throw new AssertionError("Catalog screen must not convert");
        }

        @Override
        public CatalogResponseDTO catalog(String token, String type) {
            pedidos.add(type);
            CatalogResponseDTO dto = new CatalogResponseDTO();
            if (falla) {
                dto.setEstado(EstadosEnum.ERROR);
                dto.setMensaje("Magnitud no soportada");
                return dto;
            }
            dto.setEstado(EstadosEnum.OK);
            dto.setResultado(List.copyOf(unidades));
            return dto;
        }
    }
}
