package ec.edu.monster.modelos.data;

import ec.edu.monster.modelos.enums.Perfil;

public class Sesion {
    private final String token;
    private final Perfil perfil;
    private final String nombre;
    private final String cuenta;

    /** @param cuenta número de la cuenta con la que inició sesión el cliente; null para el empleado. */
    public Sesion(String token, Perfil perfil, String nombre, String cuenta) {
        this.token = token;
        this.perfil = perfil;
        this.nombre = nombre;
        this.cuenta = cuenta;
    }

    public String getToken() {
        return token;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCuenta() {
        return cuenta;
    }

    public boolean esEmpleado() {
        return perfil == Perfil.EMPLEADO;
    }
}
