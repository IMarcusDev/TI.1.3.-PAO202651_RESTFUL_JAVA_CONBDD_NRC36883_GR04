package ec.edu.monster.controlador;

import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.servicios.SesionServicio;

public abstract class ControladorConSesion {

    public static final String MENSAJE_SESION_EXPIRADA = "Su sesión expiró o no es válida. Inicie sesión nuevamente.";

    protected final SesionServicio sesion;

    protected ControladorConSesion(SesionServicio sesion) {
        this.sesion = sesion;
    }

    protected String getToken() {
        return sesion.getToken();
    }

    public boolean haySesion() {
        return sesion.estaAutenticado();
    }

    public Sesion getSesion() {
        return sesion.getSesion();
    }

    public boolean esEmpleado() {
        Sesion actual = sesion.getSesion();
        return actual != null && actual.esEmpleado();
    }

    /** Cuenta con la que inició sesión el cliente; null para el empleado. */
    public String getCuentaSesion() {
        Sesion actual = sesion.getSesion();
        return actual != null ? actual.getCuenta() : null;
    }

    /** ECUD4 FA1: si el servidor rechazó el token, se borra la sesión local. */
    protected <T> Resultado<T> revisarSesion(Resultado<T> resultado) {
        if (resultado.isSesionExpirada()) {
            sesion.limpiar();
        }
        return resultado;
    }
}
