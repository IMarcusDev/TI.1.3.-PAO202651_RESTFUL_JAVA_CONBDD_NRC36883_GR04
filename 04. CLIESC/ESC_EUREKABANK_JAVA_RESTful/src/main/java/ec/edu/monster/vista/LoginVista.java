package ec.edu.monster.vista;

/**
 * Contract between the login screen and its controller. The Swing frame
 * implements it; tests use a fake, so the controller never depends on Swing.
 */
public interface LoginVista {

    String getUsuario();

    String getClave();

    /** True when the user picked the "Cliente" role (account number + key). */
    boolean esCliente();

    void mostrarError(String mensaje);

    void abrirMenu();

    void alIngresar(Runnable accion);
}
