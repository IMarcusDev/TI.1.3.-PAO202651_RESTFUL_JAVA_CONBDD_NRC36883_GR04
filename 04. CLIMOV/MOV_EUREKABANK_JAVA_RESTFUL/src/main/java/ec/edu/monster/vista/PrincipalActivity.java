package ec.edu.monster.vista;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import ec.edu.monster.AplicacionEureka;
import ec.edu.monster.R;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.enums.TipoOperacion;

/** Menú del perfil (ERS 8.1.1): Inicio, Consultar movimientos, Registrar movimiento y Cerrar sesión. */
public class PrincipalActivity extends AppCompatActivity {

    private static final String PESTANA_INICIO = "inicio";
    private static final String PESTANA_MOVIMIENTOS = "movimientos";
    private static final String PESTANA_OPERACIONES = "operaciones";

    private LoginControlador controlador;
    private MaterialToolbar barra;
    private BottomNavigationView barraPestanas;
    private TipoOperacion tipoSolicitado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.light(Color.WHITE, Color.WHITE));
        setContentView(R.layout.activity_principal);

        controlador = AplicacionEureka.de(this).crearLoginControlador();
        Sesion sesion = controlador.getSesion();
        if (sesion == null) {
            NavegacionSesion.volverAlLogin(this, null);
            return;
        }

        barra = findViewById(R.id.barraSuperior);
        barra.setSubtitle(subtitulo(sesion));
        barra.inflateMenu(R.menu.menu_sesion);
        barra.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.accion_cerrar_sesion) {
                confirmarCierreSesion();
                return true;
            }
            return false;
        });
        AjusteBordes.barraSuperior(barra);

        barraPestanas = findViewById(R.id.barraPestanas);
        barraPestanas.setOnItemSelectedListener(item -> {
            mostrarPestana(item.getItemId());
            return true;
        });
        barraPestanas.setOnItemReselectedListener(item -> { });

        if (savedInstanceState == null) {
            mostrarPestana(R.id.pestana_inicio);
        } else {
            barra.setTitle(titulo(barraPestanas.getSelectedItemId()));
        }

        View raiz = findViewById(R.id.raizPrincipal);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (vista, bordes) -> {
            boolean teclado = bordes.isVisible(WindowInsetsCompat.Type.ime());
            int alto = bordes.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            barraPestanas.setVisibility(teclado ? View.GONE : View.VISIBLE);
            vista.setPadding(0, 0, 0, teclado ? alto : 0);
            return bordes;
        });
    }

    private String subtitulo(Sesion sesion) {
        String nombre = sesion.getNombre();
        String perfil = sesion.getPerfil().getEtiqueta();
        return nombre == null || nombre.isEmpty() ? perfil : nombre + " · " + perfil;
    }

    void irAMovimientos() {
        barraPestanas.setSelectedItemId(R.id.pestana_movimientos);
    }

    /** Abre la pestaña de operaciones con el tipo ya elegido (acciones rápidas del inicio). */
    void irAOperacion(TipoOperacion tipo) {
        tipoSolicitado = tipo;
        if (barraPestanas.getSelectedItemId() == R.id.pestana_operaciones) {
            Fragment actual = getSupportFragmentManager().findFragmentByTag(PESTANA_OPERACIONES);
            if (actual instanceof OperacionesFragment) {
                ((OperacionesFragment) actual).alMostrar();
            }
        } else {
            barraPestanas.setSelectedItemId(R.id.pestana_operaciones);
        }
    }

    /** Devuelve y olvida el tipo de operación pedido desde el inicio, si lo hay. */
    TipoOperacion tomarTipoSolicitado() {
        TipoOperacion tipo = tipoSolicitado;
        tipoSolicitado = null;
        return tipo;
    }

    private String titulo(@IdRes int pestana) {
        if (pestana == R.id.pestana_movimientos) {
            return getString(R.string.pestana_movimientos);
        }
        if (pestana == R.id.pestana_operaciones) {
            return getString(R.string.pestana_operaciones);
        }
        return getString(R.string.app_name);
    }

    private void mostrarPestana(@IdRes int pestana) {
        String etiqueta = pestana == R.id.pestana_movimientos ? PESTANA_MOVIMIENTOS
                : pestana == R.id.pestana_operaciones ? PESTANA_OPERACIONES
                : PESTANA_INICIO;
        barra.setTitle(titulo(pestana));

        FragmentManager gestor = getSupportFragmentManager();
        FragmentTransaction transaccion = gestor.beginTransaction().setReorderingAllowed(true);

        Fragment destino = gestor.findFragmentByTag(etiqueta);
        if (destino == null) {
            destino = crearPestana(etiqueta);
            transaccion.add(R.id.contenedorPestanas, destino, etiqueta);
        }
        for (Fragment pestanaAbierta : gestor.getFragments()) {
            if (pestanaAbierta != destino) {
                transaccion.hide(pestanaAbierta);
            }
        }
        transaccion.show(destino).commit();
    }

    private static Fragment crearPestana(String etiqueta) {
        switch (etiqueta) {
            case PESTANA_MOVIMIENTOS:
                return new MovimientosFragment();
            case PESTANA_OPERACIONES:
                return new OperacionesFragment();
            default:
                return new InicioFragment();
        }
    }

    private void confirmarCierreSesion() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cerrar_sesion)
                .setMessage(R.string.cerrar_sesion_pregunta)
                .setPositiveButton(R.string.si, (dialogo, boton) -> cerrarSesion())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void cerrarSesion() {
        String token = controlador.cerrarSesion();
        AplicacionEureka.de(this).getTareas().ejecutar(
                () -> controlador.notificarCierre(token), resultado -> { }, error -> { });
        NavegacionSesion.volverAlLogin(this, null);
    }
}
