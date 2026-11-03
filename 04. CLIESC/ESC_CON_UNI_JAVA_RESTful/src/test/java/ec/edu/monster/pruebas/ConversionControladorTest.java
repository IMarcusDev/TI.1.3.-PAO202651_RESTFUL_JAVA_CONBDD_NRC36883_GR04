package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import ec.edu.monster.controlador.ConversionControlador;
import ec.edu.monster.modelo.ConversorServicio;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.vista.ConversionVista;
import ec.edu.monster.modelo.dto.CatalogResponseDTO;
import ec.edu.monster.modelo.http.ConversorApi;
import ec.edu.monster.modelo.dto.ConversorResponseDTO;
import ec.edu.monster.modelo.enums.EstadosEnum;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConversionControladorTest {

    private final Sesion sesion = new Sesion();
    private final VistaConversionFalsa vista = new VistaConversionFalsa();
    private final ConversorFalso conversor = new ConversorFalso();

    @BeforeEach
    void iniciarSesion() {
        sesion.iniciar("user", "tok-1");
    }

    @Test
    void showsTheSixMagnitudesAndLoadsUnitsOfTheFirstOnStart() {
        conversor.unidades = List.of("m", "ft");

        crearControlador();

        assertEquals(List.of("Longitud", "Masa", "Volumen", "Tiempo", "Temperatura", "Area"), vista.magnitudes);
        assertEquals(List.of("Longitud"), conversor.catalogosPedidos);
        assertEquals(List.of("m", "ft"), vista.unidades);
    }

    @Test
    void loadsUnitsExactlyOnceOnStart() {
        conversor.unidades = List.of("m", "ft");

        crearControlador();

        assertEquals(1, conversor.catalogosPedidos.size());
    }

    @Test
    void reloadsUnitsWhenMagnitudeChanges() {
        crearControlador();
        conversor.unidades = List.of("g", "kg");

        vista.seleccionarMagnitud("Masa");

        assertEquals("Masa", conversor.catalogosPedidos.getLast());
        assertEquals(List.of("g", "kg"), vista.unidades);
    }

    @Test
    void convertsAndShowsFormattedResultAcceptingDecimalComma() {
        crearControlador();
        vista.seleccionarMagnitud("Masa");
        conversor.resultado = conversionOk(2500);
        vista.valor = "2,5";
        vista.origen = "kg";
        vista.destino = "g";

        vista.presionarConvertir();

        assertEquals(2.5, conversor.ultimoValor);
        assertEquals("Masa", conversor.ultimaMagnitud);
        assertEquals("2,5 kg = 2500,0000 g", vista.resultado);
        assertNull(vista.error);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "", "   ", "NaN", "Infinity", "2d", "2f", "0x1p3", "1e3", "1,2,3", "1.2.3", "5."})
    void rejectsInvalidValuesWithoutCallingServer(String entrada) {
        crearControlador();
        vista.valor = entrada;
        vista.origen = "m2";
        vista.destino = "ha";

        vista.presionarConvertir();

        assertEquals("Ingrese un valor numérico válido", vista.error);
        assertEquals(0, conversor.conversiones);
    }

    @ParameterizedTest
    @ValueSource(strings = {"10", "-3", "+4", "2.5", "2,5", " 7 ", ".5"})
    void acceptsPlainDecimalNumbers(String entrada) {
        crearControlador();
        vista.valor = entrada;
        vista.origen = "m2";
        vista.destino = "ha";

        vista.presionarConvertir();

        assertNull(vista.error);
        assertEquals(1, conversor.conversiones);
    }

    @Test
    void rejectsNumbersThatOverflowToInfinity() {
        crearControlador();
        vista.valor = "1" + "0".repeat(400);
        vista.origen = "m2";
        vista.destino = "ha";

        vista.presionarConvertir();

        assertEquals("Ingrese un valor numérico válido", vista.error);
        assertEquals(0, conversor.conversiones);
    }

    @Test
    void asksForMagnitudeWhenNoneSelected() {
        crearControlador();
        vista.magnitud = null;
        vista.valor = "10";
        vista.origen = "m2";
        vista.destino = "ha";

        vista.presionarConvertir();

        assertEquals("Seleccione una magnitud", vista.error);
        assertEquals(0, conversor.conversiones);
    }

    @Test
    void asksForUnitsWhenOnlyOriginIsSelected() {
        crearControlador();
        vista.valor = "10";
        vista.origen = "m2";

        vista.presionarConvertir();

        assertEquals("Seleccione la unidad de origen y de destino", vista.error);
        assertEquals(0, conversor.conversiones);
    }

    @Test
    void failedConversionClearsPreviousResult() {
        crearControlador();
        vista.valor = "1";
        vista.origen = "m2";
        vista.destino = "ha";
        vista.presionarConvertir();
        conversor.resultado = conversionError("Unidad no soportada");

        vista.presionarConvertir();

        assertNull(vista.resultado);
        assertEquals("Unidad no soportada", vista.error);
    }

    @Test
    void successfulConversionClearsPreviousError() {
        crearControlador();
        vista.valor = "abc";
        vista.presionarConvertir();
        vista.valor = "1";
        vista.origen = "m2";
        vista.destino = "ha";

        vista.presionarConvertir();

        assertNull(vista.error);
        assertEquals("1 m2 = 0,0000 ha", vista.resultado);
    }

    @Test
    void changingMagnitudeClearsPreviousResult() {
        crearControlador();
        vista.valor = "1";
        vista.origen = "m2";
        vista.destino = "ha";
        vista.presionarConvertir();

        vista.seleccionarMagnitud("Longitud");

        assertNull(vista.resultado);
        assertNull(vista.error);
    }

    @Test
    void asksForUnitsWhenNoneSelected() {
        crearControlador();
        vista.valor = "10";

        vista.presionarConvertir();

        assertEquals("Seleccione la unidad de origen y de destino", vista.error);
        assertEquals(0, conversor.conversiones);
    }

    @Test
    void showsServerMessageWhenConversionFails() {
        crearControlador();
        conversor.resultado = conversionError("Unidad no soportada");
        vista.valor = "1";
        vista.origen = "m2";
        vista.destino = "xx";

        vista.presionarConvertir();

        assertEquals("Unidad no soportada", vista.error);
        assertNull(vista.resultado);
    }

    @Test
    void showsServerMessageWhenCatalogFails() {
        conversor.catalogoFalla = true;

        crearControlador();

        assertEquals("Magnitud no soportada", vista.error);
    }

    @Test
    void clearsPreviousUnitsWhenCatalogFailsAfterMagnitudeChange() {
        conversor.unidades = List.of("m2", "ha");
        crearControlador();
        conversor.catalogoFalla = true;

        vista.seleccionarMagnitud("Longitud");

        assertEquals(List.of(), vista.unidades);
        assertEquals("Magnitud no soportada", vista.error);
    }

    @Test
    void clearsUnitsWhenMagnitudeBecomesNull() {
        conversor.unidades = List.of("m2", "ha");
        crearControlador();
        int pedidosPrevios = conversor.catalogosPedidos.size();

        vista.seleccionarMagnitud(null);

        assertEquals(List.of(), vista.unidades);
        assertEquals(pedidosPrevios, conversor.catalogosPedidos.size());
    }

    private void crearControlador() {
        new ConversionControlador(vista, new ConversorServicio(
                (usuario, clave) -> {
                    throw new AssertionError("Conversion must not log in");
                }, conversor, sesion));
    }

    private static ConversorResponseDTO conversionOk(double resultado) {
        ConversorResponseDTO dto = new ConversorResponseDTO();
        dto.setEstado(EstadosEnum.OK);
        dto.setResultado(resultado);
        return dto;
    }

    private static ConversorResponseDTO conversionError(String mensaje) {
        ConversorResponseDTO dto = new ConversorResponseDTO();
        dto.setEstado(EstadosEnum.ERROR);
        dto.setMensaje(mensaje);
        return dto;
    }

    private static final class VistaConversionFalsa implements ConversionVista {

        private List<String> magnitudes = List.of();
        private List<String> unidades = List.of();
        private String magnitud;
        private String valor = "";
        private String origen;
        private String destino;
        private String resultado;
        private String error;
        private Runnable accionConvertir;
        private Runnable accionCambiarMagnitud;

        void presionarConvertir() {
            accionConvertir.run();
        }

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
        public void mostrarUnidades(List<String> lista) {
            unidades = lista;
        }

        @Override
        public String getMagnitud() {
            return magnitud;
        }

        @Override
        public String getValor() {
            return valor;
        }

        @Override
        public String getUnidadOrigen() {
            return origen;
        }

        @Override
        public String getUnidadDestino() {
            return destino;
        }

        @Override
        public void mostrarResultado(String texto) {
            resultado = texto;
        }

        @Override
        public void mostrarError(String mensaje) {
            error = mensaje;
        }

        @Override
        public void limpiarMensajes() {
            resultado = null;
            error = null;
        }

        @Override
        public void alConvertir(Runnable accion) {
            accionConvertir = accion;
        }

        @Override
        public void alCambiarMagnitud(Runnable accion) {
            accionCambiarMagnitud = accion;
        }
    }

    private static final class ConversorFalso implements ConversorApi {

        private List<String> unidades = List.of();
        private boolean catalogoFalla;
        private final List<String> catalogosPedidos = new ArrayList<>();
        private ConversorResponseDTO resultado = conversionOk(0);
        private int conversiones;
        private double ultimoValor;
        private String ultimaMagnitud;

        @Override
        public ConversorResponseDTO convert(String token, String type, double value, String from, String to) {
            conversiones++;
            ultimoValor = value;
            ultimaMagnitud = type;
            return resultado;
        }

        @Override
        public CatalogResponseDTO catalog(String token, String type) {
            catalogosPedidos.add(type);
            CatalogResponseDTO dto = new CatalogResponseDTO();
            if (catalogoFalla) {
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
