package ec.edu.monster.controlador;

import ec.edu.monster.modelos.data.ConsultaMovimientos;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.ValidacionResultado;
import ec.edu.monster.modelos.enums.Campo;
import ec.edu.monster.modelos.interfaces.MovimientoRepositorio;
import ec.edu.monster.servicios.SesionServicio;

/** ECUD2 Consultar Movimientos: el empleado consulta cualquier cuenta; el cliente, solo la suya. */
public class MovimientosControlador extends ControladorConSesion {

    private final MovimientoRepositorio repositorio;

    public MovimientosControlador(MovimientoRepositorio repositorio, SesionServicio sesion) {
        super(sesion);
        this.repositorio = repositorio;
    }

    public ValidacionResultado validar(String cuentaIngresada) {
        if (!esEmpleado()) {
            return ValidacionResultado.valido();
        }
        String error = ValidadorDatos.errorCuenta(ValidadorDatos.limpiar(cuentaIngresada));
        return error == null ? ValidacionResultado.valido() : ValidacionResultado.error(Campo.CUENTA, error);
    }

    /** @param cuentaIngresada se ignora para el cliente, que siempre consulta la cuenta de su sesión. */
    public Resultado<ConsultaMovimientos> consultar(String cuentaIngresada) {
        if (!haySesion()) {
            return Resultado.sesionExpirada(MENSAJE_SESION_EXPIRADA);
        }

        ValidacionResultado validacion = validar(cuentaIngresada);
        if (!validacion.isValido()) {
            return Resultado.error(validacion.getMensaje());
        }

        String cuenta = esEmpleado() ? ValidadorDatos.limpiar(cuentaIngresada) : getCuentaSesion();
        Resultado<ConsultaMovimientos> resultado = revisarSesion(repositorio.consultarMovimientos(getToken(), cuenta));

        if (resultado.isExito() && !esEmpleado()) {
            sesion.actualizarCuentaCliente(resultado.getDatos().getCuenta());
        }
        return resultado;
    }
}
