package ec.edu.monster.servicios;

import android.content.Context;
import android.net.ConnectivityManager;
import ec.edu.monster.modelos.interfaces.VerificadorRed;

public class RedAndroid implements VerificadorRed {

    private final ConnectivityManager conectividad;

    public RedAndroid(Context contexto) {
        conectividad = (ConnectivityManager) contexto.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public boolean sinRed() {
        try {
            return conectividad != null && conectividad.getActiveNetwork() == null;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
