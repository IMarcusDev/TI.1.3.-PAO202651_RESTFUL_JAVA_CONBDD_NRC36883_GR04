package ec.edu.monster.controlador;

import ec.edu.monster.modelos.data.Comprobante;
import ec.edu.monster.modelos.data.Cuenta;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.SolicitudOperacion;
import ec.edu.monster.modelos.data.ValidacionResultado;
import ec.edu.monster.modelos.enums.Campo;
import ec.edu.monster.modelos.enums.TipoOperacion;
import ec.edu.monster.modelos.interfaces.MovimientoRepositorio;
import ec.edu.monster.servicios.SesionServicio;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ECUD3 Registrar Movimiento. El empleado registra depósitos, retiros y transferencias e ingresa la
 * clave de la cuenta; el cliente solo retira o transfiere desde la cuenta de su sesión, sin clave.
 */
public class OperacionControlador extends ControladorConSesion {

    private final MovimientoRepositorio repositorio;
    private final MovimientosControlador consulta;

    public OperacionControlador(MovimientoRepositorio repositorio, SesionServicio sesion) {
        super(sesion);
        this.repositorio = repositorio;
        this.consulta = new MovimientosControlador(repositorio, sesion);
    }

    public List<TipoOperacion> tiposDisponibles() {
        return esEmpleado()
                ? Arrays.asList(TipoOperacion.values())
                : Collections.unmodifiableList(Arrays.asList(TipoOperacion.RETIRO, TipoOperacion.TRANSFERENCIA));
    }

    public boolean pideCuenta() {
        return esEmpleado();
    }

    public boolean pideDestino(TipoOperacion tipo) {
        return tipo == TipoOperacion.TRANSFERENCIA;
    }

    public boolean pideClave(TipoOperacion tipo) {
        return esEmpleado() && tipo != TipoOperacion.DEPOSITO;
    }

    /** ECUD3 paso 4 y FA7: valida el formato en el orden de los campos del formulario. */
    public ValidacionResultado validar(TipoOperacion tipo, String cuentaIngresada, String destinoIngresado,
                                       String importeIngresado, String claveIngresada) {
        if (!tiposDisponibles().contains(tipo)) {
            return ValidacionResultado.error(Campo.NINGUNO, "Operación no permitida para su perfil");
        }

        String cuenta = cuentaOrigen(cuentaIngresada);
        if (pideCuenta()) {
            String error = ValidadorDatos.errorCuenta(cuenta);
            if (error != null) {
                return ValidacionResultado.error(Campo.CUENTA, error);
            }
        }

        if (pideDestino(tipo)) {
            String destino = ValidadorDatos.limpiar(destinoIngresado);
            String error = ValidadorDatos.errorCuenta(destino);
            if (error != null) {
                return ValidacionResultado.error(Campo.DESTINO, error);
            }
            if (destino.equals(cuenta)) {
                return ValidacionResultado.error(Campo.DESTINO, ValidadorDatos.CUENTAS_IGUALES);
            }
        }

        String errorImporte = ValidadorDatos.errorImporte(ValidadorDatos.limpiar(importeIngresado));
        if (errorImporte != null) {
            return ValidacionResultado.error(Campo.IMPORTE, errorImporte);
        }

        if (pideClave(tipo)) {
            String error = ValidadorDatos.errorClaveCuenta(ValidadorDatos.limpiar(claveIngresada));
            if (error != null) {
                return ValidacionResultado.error(Campo.CLAVE, error);
            }
        }
        return ValidacionResultado.valido();
    }

    /** Construye la solicitud a partir de datos que ya pasaron por {@link #validar}. */
    public SolicitudOperacion preparar(TipoOperacion tipo, String cuentaIngresada, String destinoIngresado,
                                       String importeIngresado, String claveIngresada) {
        ValidacionResultado validacion = validar(tipo, cuentaIngresada, destinoIngresado, importeIngresado, claveIngresada);
        if (!validacion.isValido()) {
            throw new IllegalArgumentException(validacion.getMensaje());
        }
        return new SolicitudOperacion(tipo,
                cuentaOrigen(cuentaIngresada),
                pideDestino(tipo) ? ValidadorDatos.limpiar(destinoIngresado) : null,
                ValidadorDatos.convertirImporte(importeIngresado),
                pideClave(tipo) ? ValidadorDatos.limpiar(claveIngresada) : "");
    }

    public Resultado<Comprobante> registrar(SolicitudOperacion solicitud) {
        if (!haySesion()) {
            return Resultado.sesionExpirada(MENSAJE_SESION_EXPIRADA);
        }

        Resultado<Comprobante> resultado = revisarSesion(repositorio.registrar(getToken(), solicitud));
        if (resultado.isExito() && !esEmpleado()) {
            sesion.actualizarSaldoCliente(resultado.getDatos().getNuevoSaldo());
        }
        return resultado;
    }

    /** Cuenta del cliente para mostrar el número y el saldo junto al formulario. */
    public Cuenta cuentaClienteGuardada() {
        return sesion.getCuentaCliente();
    }

    public Resultado<Cuenta> cargarCuentaCliente() {
        Resultado<?> resultado = consulta.consultar(null);
        if (resultado.isExito()) {
            return Resultado.ok(sesion.getCuentaCliente(), resultado.getMensaje());
        }
        return resultado.isSesionExpirada()
                ? Resultado.sesionExpirada(resultado.getMensaje())
                : Resultado.error(resultado.getMensaje());
    }

    private String cuentaOrigen(String cuentaIngresada) {
        return pideCuenta() ? ValidadorDatos.limpiar(cuentaIngresada) : getCuentaSesion();
    }
}
