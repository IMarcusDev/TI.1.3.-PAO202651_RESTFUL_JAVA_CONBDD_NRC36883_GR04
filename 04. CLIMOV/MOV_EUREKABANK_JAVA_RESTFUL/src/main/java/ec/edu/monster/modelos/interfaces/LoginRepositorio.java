package ec.edu.monster.modelos.interfaces;

import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.Sesion;

public interface LoginRepositorio {
    Resultado<Sesion> loginEmpleado(String usuario, String clave);

    Resultado<Sesion> loginCliente(String cuenta, String clave);

    Resultado<Void> logout(String token);
}
