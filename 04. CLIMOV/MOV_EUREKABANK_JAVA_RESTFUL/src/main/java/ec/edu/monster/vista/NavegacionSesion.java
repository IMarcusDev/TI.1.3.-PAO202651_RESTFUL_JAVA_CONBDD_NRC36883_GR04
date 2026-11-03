package ec.edu.monster.vista;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import ec.edu.monster.R;

final class NavegacionSesion {

    private NavegacionSesion() { }

    /** @param aviso mensaje a mostrar antes de volver (sesión expirada); null para volver directamente. */
    static void volverAlLogin(Activity actividad, String aviso) {
        if (actividad == null || actividad.isFinishing()) {
            return;
        }
        if (aviso == null) {
            irAlLogin(actividad);
            return;
        }

        View ventana = actividad.getWindow().getDecorView();
        if (ventana.getTag(R.id.aviso_sesion_visible) != null) {
            return;
        }
        ventana.setTag(R.id.aviso_sesion_visible, Boolean.TRUE);

        new MaterialAlertDialogBuilder(actividad)
                .setTitle(R.string.sesion)
                .setMessage(aviso)
                .setCancelable(false)
                .setPositiveButton(R.string.aceptar, (dialogo, boton) -> irAlLogin(actividad))
                .show();
    }

    private static void irAlLogin(Activity actividad) {
        Intent intent = new Intent(actividad, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        actividad.startActivity(intent);
        actividad.finish();
    }
}
