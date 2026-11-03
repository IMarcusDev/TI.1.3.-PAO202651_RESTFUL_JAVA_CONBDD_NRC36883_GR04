package ec.edu.monster.modelos.interfaces;

import ec.edu.monster.modelos.data.Comprobante;
import ec.edu.monster.modelos.data.ConsultaMovimientos;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.SolicitudOperacion;

public interface MovimientoRepositorio {
    Resultado<ConsultaMovimientos> consultarMovimientos(String token, String cuenta);

    Resultado<Comprobante> registrar(String token, SolicitudOperacion solicitud);
}
