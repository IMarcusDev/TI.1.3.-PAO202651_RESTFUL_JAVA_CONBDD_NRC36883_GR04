package ec.edu.monster.modelos.repositorios;

import ec.edu.monster.config.RestConfig;
import ec.edu.monster.modelos.data.Comprobante;
import ec.edu.monster.modelos.data.ConsultaMovimientos;
import ec.edu.monster.modelos.data.Cuenta;
import ec.edu.monster.modelos.data.Movimiento;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.SolicitudOperacion;
import ec.edu.monster.modelos.enums.TipoOperacion;
import ec.edu.monster.modelos.interfaces.MovimientoRepositorio;
import ec.edu.monster.modelos.interfaces.VerificadorRed;
import ec.edu.monster.modelos.rest.RestConstructorPeticion;
import ec.edu.monster.modelos.rest.RestLectorRespuesta;
import ec.edu.monster.modelos.rest.RestTransporte;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** Recurso Movimiento: consultar (ECUD2) y deposito/retiro/transferencia (ECUD3). */
public class MovimientoRestRepositorio extends RepositorioRest implements MovimientoRepositorio {

    static final String NO_SE_PUDO_REGISTRAR = "No se pudo registrar la operación, intente nuevamente";

    public MovimientoRestRepositorio(String urlBase, RestTransporte transporte, VerificadorRed red) {
        super(urlBase, RestConfig.RECURSO_MOVIMIENTO, transporte, red);
    }

    @Override
    public Resultado<ConsultaMovimientos> consultarMovimientos(String token, String cuenta) {
        if (sinRed()) {
            return Resultado.error(RestErrores.SIN_CONEXION);
        }

        try {
            RestLectorRespuesta respuesta = enviar(peticion("consultarMovimientos")
                    .parametro("token", token)
                    .parametro("cuenta", cuenta));
            if (respuesta.estado() == null) {
                return Resultado.error(RestErrores.RESPUESTA_INCOMPLETA);
            }
            if (!respuesta.estadoOk()) {
                return fallo(respuesta, "La cuenta no existe");
            }

            String numero = respuesta.texto("numeroCuenta");
            Cuenta datosCuenta = new Cuenta(
                    numero != null && !numero.isEmpty() ? numero : cuenta,
                    textoOVacio(respuesta.texto("titular")),
                    textoOVacio(respuesta.texto("moneda")),
                    decimal(respuesta.texto("saldo")),
                    textoOVacio(respuesta.texto("estadoCuenta")));

            List<Movimiento> movimientos = new ArrayList<>();
            for (Map<String, String> registro : respuesta.registros("numeroMovimiento")) {
                movimientos.add(new Movimiento(
                        textoOVacio(registro.get("numeroMovimiento")),
                        textoOVacio(registro.get("fecha")),
                        textoOVacio(registro.get("tipoMovimiento")),
                        textoOVacio(registro.get("accion")),
                        decimal(registro.get("importe")),
                        textoOVacio(registro.get("cuentaReferencia"))));
            }

            return Resultado.ok(new ConsultaMovimientos(datosCuenta, movimientos),
                    textoOVacio(respuesta.texto("mensaje")));
        } catch (Exception e) {
            return Resultado.error(RestErrores.mensaje(e));
        }
    }

    @Override
    public Resultado<Comprobante> registrar(String token, SolicitudOperacion solicitud) {
        if (sinRed()) {
            return Resultado.error(RestErrores.SIN_CONEXION);
        }

        try {
            RestLectorRespuesta respuesta = enviar(peticionDe(token, solicitud));
            if (respuesta.estado() == null) {
                return Resultado.error(RestErrores.RESPUESTA_INCOMPLETA);
            }
            if (!respuesta.estadoOk()) {
                return fallo(respuesta, NO_SE_PUDO_REGISTRAR);
            }

            Comprobante comprobante = new Comprobante(solicitud,
                    textoOVacio(respuesta.texto("numeroMovimiento")),
                    decimal(respuesta.texto("nuevoSaldo")),
                    new Date());
            return Resultado.ok(comprobante, textoOVacio(respuesta.texto("mensaje")));
        } catch (Exception e) {
            // ECUD3 FA10: no se reintenta automáticamente para no duplicar movimientos.
            return Resultado.error(RestErrores.mensaje(e));
        }
    }

    private RestConstructorPeticion peticionDe(String token, SolicitudOperacion solicitud) {
        TipoOperacion tipo = solicitud.getTipo();
        RestConstructorPeticion peticion = peticion(tipo.getRuta()).parametro("token", token);

        switch (tipo) {
            case DEPOSITO:
                return peticion.parametro("cuenta", solicitud.getCuenta())
                               .parametro("importe", solicitud.getImporte());
            case RETIRO:
                return peticion.parametro("cuenta", solicitud.getCuenta())
                               .parametro("importe", solicitud.getImporte())
                               .parametro("clave", solicitud.getClave());
            case TRANSFERENCIA:
            default:
                return peticion.parametro("cuentaOrigen", solicitud.getCuenta())
                               .parametro("cuentaDestino", solicitud.getCuentaDestino())
                               .parametro("importe", solicitud.getImporte())
                               .parametro("clave", solicitud.getClave());
        }
    }
}
