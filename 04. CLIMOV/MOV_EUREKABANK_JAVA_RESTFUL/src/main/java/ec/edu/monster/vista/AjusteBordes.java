package ec.edu.monster.vista;

import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

final class AjusteBordes {

    private AjusteBordes() { }

    static void barraSuperior(View barra) {
        int arribaBase = barra.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(barra, (vista, bordes) -> {
            Insets sistema = bordes.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            vista.setPadding(vista.getPaddingLeft(), arribaBase + sistema.top,
                    vista.getPaddingRight(), vista.getPaddingBottom());
            return bordes;
        });
        ViewCompat.requestApplyInsets(barra);
    }

    static void contenidoInferior(View contenido) {
        int abajoBase = contenido.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(contenido, (vista, bordes) -> {
            Insets navegacion = bordes.getInsets(WindowInsetsCompat.Type.navigationBars());
            Insets teclado = bordes.getInsets(WindowInsetsCompat.Type.ime());
            vista.setPadding(vista.getPaddingLeft(), vista.getPaddingTop(), vista.getPaddingRight(),
                    abajoBase + Math.max(navegacion.bottom, teclado.bottom));
            return bordes;
        });
        ViewCompat.requestApplyInsets(contenido);
    }
}
