package ec.edu.monster.controlador;

import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.data.ValidacionResultado;
import ec.edu.monster.modelos.enums.Campo;
import ec.edu.monster.modelos.enums.Perfil;
import ec.edu.monster.modelos.interfaces.LoginRepositorio;
import ec.edu.monster.servicios.SesionServicio;

public class LoginControlador extends ControladorConSesion {

    private final LoginRepositorio repositorio;

    public LoginControlador(LoginRepositorio repositorio, SesionServicio sesion) {
        super(sesion);
        this.repositorio = repositorio;
    }

    /** ECUD1 paso 4 y FA3: campos vacíos y, para el cliente, cuenta de 8 dígitos y clave de 6. */
    public ValidacionResultado validar(Perfil perfil, String credencialIngresada, String claveIngresada) {
        String credencial = ValidadorDatos.limpiar(credencialIngresada);
        String clave = ValidadorDatos.limpiar(claveIngresada);

        if (perfil == Perfil.CLIENTE) {
            String errorCuenta = ValidadorDatos.errorCuenta(credencial);
            if (errorCuenta != null) {
                return ValidacionResultado.error(Campo.CREDENCIAL, errorCuenta);
            }
            String errorClave = ValidadorDatos.errorClaveCuenta(clave);
            return errorClave == null ? ValidacionResultado.valido() : ValidacionResultado.error(Campo.CLAVE, errorClave);
        }

        if (credencial.isEmpty()) {
            return ValidacionResultado.error(Campo.CREDENCIAL, ValidadorDatos.INGRESE_USUARIO);
        }
        if (clave.isEmpty()) {
            return ValidacionResultado.error(Campo.CLAVE, ValidadorDatos.INGRESE_CLAVE);
        }
        return ValidacionResultado.valido();
    }

    public Resultado<Sesion> iniciarSesion(Perfil perfil, String credencialIngresada, String claveIngresada) {
        ValidacionResultado validacion = validar(perfil, credencialIngresada, claveIngresada);
        if (!validacion.isValido()) {
            return Resultado.error(validacion.getMensaje());
        }

        String credencial = ValidadorDatos.limpiar(credencialIngresada);
        String clave = ValidadorDatos.limpiar(claveIngresada);
        Resultado<Sesion> resultado = perfil == Perfil.CLIENTE
                ? repositorio.loginCliente(credencial, clave)
                : repositorio.loginEmpleado(credencial, clave);

        if (resultado.isExito()) {
            sesion.iniciar(resultado.getDatos());
        }
        return resultado;
    }

    /**
     * ECUD1 FA5: borra la sesión local de inmediato, para que el usuario vuelva al inicio de sesión
     * aunque el servidor no responda. @return el token, para avisar al servidor con {@link #notificarCierre}.
     */
    public String cerrarSesion() {
        String token = getToken();
        sesion.limpiar();
        return token;
    }

    /** Envía logout(token) al servidor. Se ejecuta en segundo plano; su resultado no cambia el flujo. */
    public Resultado<Void> notificarCierre(String token) {
        if (token == null || token.isEmpty()) {
            return Resultado.ok(null, "");
        }
        return repositorio.logout(token);
    }
}
