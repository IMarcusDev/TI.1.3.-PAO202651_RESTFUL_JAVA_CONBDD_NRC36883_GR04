package ec.edu.monster.modelo;
/*
Clase para guardar el estado de la sesión: inicio, estado activo y cierre
*/
public class Sesion {
    
    private String usuario;
    private String token;

    public void iniciar(String usuario, String token) {
        this.usuario = usuario;
        this.token = token;
    }

    public void cerrar() {
        usuario = null;
        token = null;
    }

    public boolean estaActiva() {
        return token != null;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getToken() {
        return token;
    }
}
