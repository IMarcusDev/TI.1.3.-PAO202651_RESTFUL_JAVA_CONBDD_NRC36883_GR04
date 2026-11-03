package ec.edu.monster.modelos.repositorios;

import ec.edu.monster.config.RestConfig;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.enums.Perfil;
import ec.edu.monster.modelos.interfaces.LoginRepositorio;
import ec.edu.monster.modelos.interfaces.VerificadorRed;
import ec.edu.monster.modelos.rest.RestConstructorPeticion;
import ec.edu.monster.modelos.rest.RestLectorRespuesta;
import ec.edu.monster.modelos.rest.RestTransporte;

/** Recurso Login: empleado, cliente y logout (ECUD1). */
public class LoginRestRepositorio extends RepositorioRest implements LoginRepositorio {

    static final String CREDENCIALES_INCORRECTAS = "Credenciales incorrectas";

    public LoginRestRepositorio(String urlBase, RestTransporte transporte, VerificadorRed red) {
        super(urlBase, RestConfig.RECURSO_LOGIN, transporte, red);
    }

    @Override
    public Resultado<Sesion> loginEmpleado(String usuario, String clave) {
        return login(peticion("loginEmpleado")
                .parametro("usuario", usuario)
                .parametro("clave", clave), Perfil.EMPLEADO, null);
    }

    @Override
    public Resultado<Sesion> loginCliente(String cuenta, String clave) {
        return login(peticion("loginCliente")
                .parametro("cuenta", cuenta)
                .parametro("clave", clave), Perfil.CLIENTE, cuenta);
    }

    private Resultado<Sesion> login(RestConstructorPeticion peticion, Perfil perfilPedido, String cuenta) {
        if (sinRed()) {
            return Resultado.error(RestErrores.SIN_CONEXION);
        }

        try {
            RestLectorRespuesta respuesta = enviar(peticion);
            if (respuesta.estado() == null) {
                return Resultado.error(RestErrores.RESPUESTA_INCOMPLETA);
            }
            if (!respuesta.estadoOk()) {
                return Resultado.error(RestErrores.mensajeServidor(respuesta.texto("mensaje"), CREDENCIALES_INCORRECTAS));
            }

            String token = respuesta.texto("token");
            if (token == null || token.isEmpty()) {
                return Resultado.error(RestErrores.RESPUESTA_INCOMPLETA);
            }

            Perfil perfil = Perfil.desde(respuesta.texto("perfil"), perfilPedido);
            Sesion sesion = new Sesion(token, perfil, textoOVacio(respuesta.texto("nombre")), cuenta);
            return Resultado.ok(sesion, textoOVacio(respuesta.texto("mensaje")));
        } catch (Exception e) {
            return Resultado.error(RestErrores.mensaje(e));
        }
    }

    @Override
    public Resultado<Void> logout(String token) {
        if (sinRed()) {
            return Resultado.error(RestErrores.SIN_CONEXION);
        }

        try {
            RestLectorRespuesta respuesta = enviar(peticion("logout").parametro("token", token));
            return respuesta.estadoOk()
                    ? Resultado.ok(null, textoOVacio(respuesta.texto("mensaje")))
                    : Resultado.error(RestErrores.mensajeServidor(respuesta.texto("mensaje"), RestErrores.RESPUESTA_INCOMPLETA));
        } catch (Exception e) {
            return Resultado.error(RestErrores.mensaje(e));
        }
    }
}
