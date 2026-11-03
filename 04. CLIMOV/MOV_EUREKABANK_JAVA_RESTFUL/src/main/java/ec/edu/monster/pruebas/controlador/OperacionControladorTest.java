package ec.edu.monster.pruebas.controlador;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.controlador.OperacionControlador;
import ec.edu.monster.modelos.data.Comprobante;
import ec.edu.monster.modelos.data.ConsultaMovimientos;
import ec.edu.monster.modelos.data.Cuenta;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.data.SolicitudOperacion;
import ec.edu.monster.modelos.data.ValidacionResultado;
import ec.edu.monster.modelos.enums.Campo;
import ec.edu.monster.modelos.enums.Perfil;
import ec.edu.monster.modelos.enums.TipoOperacion;
import ec.edu.monster.modelos.interfaces.MovimientoRepositorio;
import ec.edu.monster.servicios.SesionServicio;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import org.junit.Test;

public class OperacionControladorTest {

    /** Repositorio en memoria que recuerda la última solicitud y responde lo que se le indique. */
    private static class RepositorioFalso implements MovimientoRepositorio {
        SolicitudOperacion ultima;
        Resultado<Comprobante> respuesta;

        @Override
        public Resultado<ConsultaMovimientos> consultarMovimientos(String token, String cuenta) {
            Cuenta datos = new Cuenta(cuenta, "Ana Pérez", "USD", new BigDecimal("500.00"), "ACTIVO");
            return Resultado.ok(new ConsultaMovimientos(datos, Collections.emptyList()), "ok");
        }

        @Override
        public Resultado<Comprobante> registrar(String token, SolicitudOperacion solicitud) {
            ultima = solicitud;
            return respuesta != null ? respuesta
                    : Resultado.ok(new Comprobante(solicitud, "7", new BigDecimal("400.00"), new Date()), "ok");
        }
    }

    private static SesionServicio sesion(Perfil perfil) {
        SesionServicio sesion = new SesionServicio();
        sesion.iniciar(new Sesion("tok", perfil, "Usuario", perfil == Perfil.CLIENTE ? "00100001" : null));
        return sesion;
    }

    @Test
    public void cliente_soloPuedeRetirarOTransferir_ySinClave() {
        OperacionControlador controlador = new OperacionControlador(new RepositorioFalso(), sesion(Perfil.CLIENTE));

        assertFalse(controlador.tiposDisponibles().contains(TipoOperacion.DEPOSITO));
        assertFalse(controlador.pideCuenta());
        assertFalse(controlador.pideClave(TipoOperacion.RETIRO));
        assertEquals(Campo.NINGUNO, controlador.validar(TipoOperacion.DEPOSITO, "", "", "10", "").getCampo());
    }

    @Test
    public void cliente_operaSobreLaCuentaDeSuSesionConClaveVacia() {
        RepositorioFalso repositorio = new RepositorioFalso();
        SesionServicio sesion = sesion(Perfil.CLIENTE);
        OperacionControlador controlador = new OperacionControlador(repositorio, sesion);
        controlador.cargarCuentaCliente();

        SolicitudOperacion solicitud = controlador.preparar(TipoOperacion.RETIRO, "99999999", "", "100", "123456");
        assertTrue(controlador.registrar(solicitud).isExito());

        assertEquals("00100001", repositorio.ultima.getCuenta());
        assertEquals("", repositorio.ultima.getClave());
        assertEquals(new BigDecimal("400.00"), sesion.getCuentaCliente().getSaldo());
    }

    @Test
    public void empleado_debeIngresarLaClaveEnRetiroYTransferencia() {
        OperacionControlador controlador = new OperacionControlador(new RepositorioFalso(), sesion(Perfil.EMPLEADO));

        assertTrue(controlador.pideClave(TipoOperacion.RETIRO));
        assertFalse(controlador.pideClave(TipoOperacion.DEPOSITO));
        ValidacionResultado sinClave = controlador.validar(TipoOperacion.RETIRO, "00100001", "", "50", "");
        assertEquals(Campo.CLAVE, sinClave.getCampo());
        assertTrue(controlador.validar(TipoOperacion.DEPOSITO, "00100001", "", "50", "").isValido());
    }

    @Test
    public void transferencia_rechazaCuentasIguales() {
        OperacionControlador controlador = new OperacionControlador(new RepositorioFalso(), sesion(Perfil.EMPLEADO));

        ValidacionResultado resultado = controlador.validar(TipoOperacion.TRANSFERENCIA, "00100001", "00100001", "10", "123456");

        assertEquals(Campo.DESTINO, resultado.getCampo());
        assertEquals("La cuenta de origen y la de destino deben ser diferentes", resultado.getMensaje());
    }

    @Test
    public void registrar_conTokenInvalido_borraLaSesion() {
        RepositorioFalso repositorio = new RepositorioFalso();
        repositorio.respuesta = Resultado.sesionExpirada("expiró");
        SesionServicio sesion = sesion(Perfil.EMPLEADO);
        OperacionControlador controlador = new OperacionControlador(repositorio, sesion);

        SolicitudOperacion solicitud = controlador.preparar(TipoOperacion.DEPOSITO, "00100001", "", "10", "");
        assertTrue(controlador.registrar(solicitud).isSesionExpirada());
        assertNull(sesion.getSesion());
    }
}
