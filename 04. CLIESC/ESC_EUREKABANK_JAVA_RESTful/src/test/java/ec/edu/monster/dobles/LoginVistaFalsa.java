package ec.edu.monster.dobles;

import ec.edu.monster.vista.LoginVista;

/**
 * Login view double: records what the controller asks it to show.
 */
public class LoginVistaFalsa implements LoginVista {

    private Runnable accionIngresar = () -> { };
    private String usuario = "";
    private String clave = "";
    private String error;
    private boolean menuAbierto;
    private int erroresMostrados;
    private boolean cliente;

    public void escribir(String usuario, String clave) {
        this.usuario = usuario;
        this.clave = clave;
    }

    /** Selects the "Cliente" role in the fake login screen. */
    public void comoCliente() {
        this.cliente = true;
    }

    public void ingresar() {
        accionIngresar.run();
    }

    @Override
    public String getUsuario() {
        return usuario;
    }

    @Override
    public String getClave() {
        return clave;
    }

    @Override
    public boolean esCliente() {
        return cliente;
    }

    @Override
    public void mostrarError(String mensaje) {
        erroresMostrados++;
        this.error = mensaje;
    }

    @Override
    public void abrirMenu() {
        menuAbierto = true;
    }

    @Override
    public void alIngresar(Runnable accion) {
        this.accionIngresar = accion;
    }

    public String getError() {
        return error;
    }

    public boolean isMenuAbierto() {
        return menuAbierto;
    }

    public int getErroresMostrados() {
        return erroresMostrados;
    }
}
