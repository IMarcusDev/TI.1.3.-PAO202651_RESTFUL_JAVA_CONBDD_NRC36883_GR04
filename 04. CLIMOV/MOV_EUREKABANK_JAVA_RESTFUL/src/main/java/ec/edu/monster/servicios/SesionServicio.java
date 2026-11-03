package ec.edu.monster.servicios;

import ec.edu.monster.modelos.data.Cuenta;
import ec.edu.monster.modelos.data.Sesion;
import java.math.BigDecimal;

/** Guarda el token y el perfil mientras dura la sesión, y la última información conocida de la cuenta del cliente. */
public class SesionServicio {

    private volatile Sesion sesion;
    private volatile Cuenta cuentaCliente;

    public Sesion getSesion() {
        return sesion;
    }

    public String getToken() {
        Sesion actual = sesion;
        return actual != null ? actual.getToken() : null;
    }

    public boolean estaAutenticado() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void iniciar(Sesion nueva) {
        sesion = nueva;
        cuentaCliente = null;
    }

    public void limpiar() {
        sesion = null;
        cuentaCliente = null;
    }

    public Cuenta getCuentaCliente() {
        return cuentaCliente;
    }

    public void actualizarCuentaCliente(Cuenta cuenta) {
        cuentaCliente = cuenta;
    }

    public void actualizarSaldoCliente(BigDecimal saldo) {
        Cuenta actual = cuentaCliente;
        if (actual != null && saldo != null) {
            cuentaCliente = actual.conSaldo(saldo);
        }
    }
}
