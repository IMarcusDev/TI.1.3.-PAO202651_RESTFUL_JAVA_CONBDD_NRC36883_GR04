package ec.edu.monster.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import ec.edu.monster.AplicacionEureka;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ControladorConSesion;
import ec.edu.monster.controlador.MovimientosControlador;
import ec.edu.monster.modelos.data.ConsultaMovimientos;
import ec.edu.monster.modelos.data.Cuenta;
import ec.edu.monster.modelos.data.Movimiento;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.enums.TipoOperacion;
import ec.edu.monster.utils.Formato;
import java.util.List;

/** Inicio: saludo, cuenta del cliente (o ventanilla del empleado) y accesos a las opciones del perfil. */
public class InicioFragment extends PestanaFragment {

    private static final int ULTIMOS_MOVIMIENTOS = 3;

    private MovimientosControlador controlador;
    private boolean cargando;
    private boolean accionesCreadas;

    private SwipeRefreshLayout refrescar;
    private TextView lblNombre;
    private TextView lblPerfil;
    private View tarjetaCuenta;
    private View datosCuenta;
    private View estadoCargaCuenta;
    private View prgCuenta;
    private TextView lblEstadoCarga;
    private View btnReintentarCuenta;
    private TextView lblEstadoCuenta;
    private TextView lblNumeroCuenta;
    private TextView lblSaldo;
    private TextView lblTitular;
    private View tarjetaVentanilla;
    private LinearLayout listaAcciones;
    private View seccionUltimos;
    private LinearLayout listaUltimos;
    private View lblSinMovimientos;

    @Override
    protected ControladorConSesion controlador() {
        return controlador;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controlador = AplicacionEureka.de(requireContext()).crearMovimientosControlador();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor,
                             @Nullable Bundle savedInstanceState) {
        return inflador.inflate(R.layout.fragment_inicio, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle savedInstanceState) {
        refrescar = vista.findViewById(R.id.refrescar);
        lblNombre = vista.findViewById(R.id.lblNombre);
        lblPerfil = vista.findViewById(R.id.lblPerfil);
        tarjetaCuenta = vista.findViewById(R.id.tarjetaCuenta);
        datosCuenta = vista.findViewById(R.id.datosCuenta);
        estadoCargaCuenta = vista.findViewById(R.id.estadoCargaCuenta);
        prgCuenta = vista.findViewById(R.id.prgCuenta);
        lblEstadoCarga = vista.findViewById(R.id.lblEstadoCarga);
        btnReintentarCuenta = vista.findViewById(R.id.btnReintentarCuenta);
        lblEstadoCuenta = vista.findViewById(R.id.lblEstadoCuenta);
        lblNumeroCuenta = vista.findViewById(R.id.lblNumeroCuenta);
        lblSaldo = vista.findViewById(R.id.lblSaldo);
        lblTitular = vista.findViewById(R.id.lblTitular);
        tarjetaVentanilla = vista.findViewById(R.id.tarjetaVentanilla);
        listaAcciones = vista.findViewById(R.id.listaAcciones);
        seccionUltimos = vista.findViewById(R.id.seccionUltimos);
        listaUltimos = vista.findViewById(R.id.listaUltimos);
        lblSinMovimientos = vista.findViewById(R.id.lblSinMovimientos);

        refrescar.setColorSchemeResources(R.color.eureka_primario);
        refrescar.setOnRefreshListener(this::cargarCuenta);
        btnReintentarCuenta.setOnClickListener(v -> cargarCuenta());
        vista.findViewById(R.id.btnVerTodos).setOnClickListener(v -> principal().irAMovimientos());
    }

    @Override
    protected void alMostrar() {
        Sesion sesion = controlador.getSesion();
        if (sesion == null || !vistaActiva()) {
            return;
        }

        lblNombre.setText(sesion.getNombre() == null || sesion.getNombre().isEmpty()
                ? Formato.SIN_DATO : sesion.getNombre());
        lblPerfil.setText(getString(R.string.inicio_perfil, sesion.getPerfil().getEtiqueta()));

        boolean empleado = sesion.esEmpleado();
        tarjetaVentanilla.setVisibility(empleado ? View.VISIBLE : View.GONE);
        tarjetaCuenta.setVisibility(empleado ? View.GONE : View.VISIBLE);
        seccionUltimos.setVisibility(empleado ? View.GONE : View.VISIBLE);
        refrescar.setEnabled(!empleado);

        if (!accionesCreadas) {
            crearAcciones(empleado);
            accionesCreadas = true;
        }
        if (!empleado) {
            cargarCuenta();
        }
    }

    private void crearAcciones(boolean empleado) {
        LayoutInflater inflador = getLayoutInflater();
        agregarAccion(inflador, R.drawable.ic_lista, getString(R.string.accion_consultar),
                getString(empleado ? R.string.accion_consultar_detalle_empleado : R.string.accion_consultar_detalle_cliente),
                v -> principal().irAMovimientos());
        for (TipoOperacion tipo : AplicacionEureka.de(requireContext()).crearOperacionControlador().tiposDisponibles()) {
            agregarAccion(inflador, TextosOperacion.icono(tipo), TextosOperacion.nombre(requireContext(), tipo),
                    TextosOperacion.descripcion(requireContext(), tipo), v -> principal().irAOperacion(tipo));
        }
    }

    private void agregarAccion(LayoutInflater inflador, @DrawableRes int icono, String titulo, String detalle,
                               View.OnClickListener alPulsar) {
        View fila = inflador.inflate(R.layout.item_accion, listaAcciones, false);
        ((ImageView) fila.findViewById(R.id.imgAccion)).setImageResource(icono);
        ((TextView) fila.findViewById(R.id.lblAccion)).setText(titulo);
        ((TextView) fila.findViewById(R.id.lblAccionDetalle)).setText(detalle);
        fila.setOnClickListener(alPulsar);
        listaAcciones.addView(fila);
    }

    private void cargarCuenta() {
        if (cargando) {
            return;
        }
        cargando = true;
        if (lblNumeroCuenta.getText().length() == 0) {
            mostrarEstadoCarga(getString(R.string.inicio_cargando_cuenta), false);
        }

        tareas().ejecutar(() -> controlador.consultar(null), this::alCargarCuenta, error -> {
            cargando = false;
            if (vistaActiva()) {
                refrescar.setRefreshing(false);
                mostrarEstadoCarga(getString(R.string.error_inesperado), true);
            }
        });
    }

    private void alCargarCuenta(Resultado<ConsultaMovimientos> resultado) {
        cargando = false;
        if (!vistaActiva()) {
            return;
        }
        refrescar.setRefreshing(false);

        if (resultado.isSesionExpirada()) {
            volverAlLogin(resultado.getMensaje());
            return;
        }
        if (!resultado.isExito()) {
            if (lblNumeroCuenta.getText().length() == 0) {
                mostrarEstadoCarga(resultado.getMensaje(), true);
            }
            return;
        }

        ConsultaMovimientos consulta = resultado.getDatos();
        mostrarCuenta(consulta.getCuenta());
        mostrarUltimos(consulta.getMovimientos(), consulta.getCuenta().getMoneda());
    }

    private void mostrarEstadoCarga(String mensaje, boolean conError) {
        datosCuenta.setVisibility(View.INVISIBLE);
        estadoCargaCuenta.setVisibility(View.VISIBLE);
        prgCuenta.setVisibility(conError ? View.GONE : View.VISIBLE);
        btnReintentarCuenta.setVisibility(conError ? View.VISIBLE : View.GONE);
        lblEstadoCarga.setText(mensaje);
    }

    private void mostrarCuenta(Cuenta cuenta) {
        estadoCargaCuenta.setVisibility(View.GONE);
        datosCuenta.setVisibility(View.VISIBLE);
        lblNumeroCuenta.setText(getString(R.string.inicio_numero_cuenta, cuenta.getNumero()));
        lblSaldo.setText(Formato.importe(cuenta.getSaldo(), cuenta.getMoneda()));
        lblTitular.setText(cuenta.getTitular());
        String estado = cuenta.getEstado();
        lblEstadoCuenta.setText(estado == null || estado.isEmpty() ? Cuenta.ESTADO_ACTIVO : estado);
    }

    /** Muestra los últimos movimientos, del más reciente al más antiguo. */
    private void mostrarUltimos(List<Movimiento> movimientos, String moneda) {
        listaUltimos.removeAllViews();
        LayoutInflater inflador = getLayoutInflater();
        int desde = Math.max(0, movimientos.size() - ULTIMOS_MOVIMIENTOS);
        for (int i = movimientos.size() - 1; i >= desde; i--) {
            View fila = MovimientoVista.crear(inflador, listaUltimos);
            MovimientoVista.enlazar(fila, movimientos.get(i), moneda);
            listaUltimos.addView(fila);
        }
        lblSinMovimientos.setVisibility(movimientos.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
