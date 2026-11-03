package ec.edu.monster.vista;

import androidx.fragment.app.Fragment;
import ec.edu.monster.AplicacionEureka;
import ec.edu.monster.controlador.ControladorConSesion;
import ec.edu.monster.servicios.EjecutorTareas;

/** Pestaña de la pantalla principal: verifica la sesión cada vez que se muestra. */
public abstract class PestanaFragment extends Fragment {

    protected abstract ControladorConSesion controlador();

    /** Se llama cada vez que la pestaña se vuelve visible con una sesión vigente. */
    protected abstract void alMostrar();

    protected EjecutorTareas tareas() {
        return AplicacionEureka.de(requireContext()).getTareas();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) {
            verificarSesionYMostrar();
        }
    }

    @Override
    public void onHiddenChanged(boolean oculto) {
        super.onHiddenChanged(oculto);
        if (!oculto && isResumed()) {
            verificarSesionYMostrar();
        }
    }

    private void verificarSesionYMostrar() {
        if (!controlador().haySesion()) {
            volverAlLogin(ControladorConSesion.MENSAJE_SESION_EXPIRADA);
            return;
        }
        alMostrar();
    }

    protected void volverAlLogin(String aviso) {
        NavegacionSesion.volverAlLogin(getActivity(), aviso);
    }

    protected boolean vistaActiva() {
        return isAdded() && getView() != null;
    }

    protected PrincipalActivity principal() {
        return (PrincipalActivity) requireActivity();
    }
}
