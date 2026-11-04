package ec.edu.monster.vista;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.textfield.TextInputLayout;
import ec.edu.monster.AplicacionEureka;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ControladorConSesion;
import ec.edu.monster.controlador.MovimientosControlador;
import ec.edu.monster.modelos.data.ConsultaMovimientos;
import ec.edu.monster.modelos.data.Cuenta;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.ValidacionResultado;
import ec.edu.monster.utils.Formato;

/** ECUD2 Consultar Movimientos: cabecera con los datos de la cuenta y lista de movimientos. */
public class MovimientosFragment extends PestanaFragment {

    private MovimientosControlador controlador;
    private MovimientosAdaptador adaptador;
    private boolean cargando;
    /** Última cuenta consultada con éxito por el empleado, para recargarla al deslizar. */
    private String cuentaConsultada;

    private SwipeRefreshLayout refrescar;
    private View tarjetaBusqueda;
    private TextInputLayout tilCuenta;
    private EditText txtCuenta;
    private MaterialButton btnConsultar;
    private TextView lblError;
    private View panelCargando;
    private View panelInicial;
    private View panelResultado;
    private TextView lblNumero;
    private TextView lblEstado;
    private TextView lblTitular;
    private TextView lblSaldo;
    private TextView lblMoneda;
    private TextView lblCantidad;
    private RecyclerView listaMovimientos;
    private View lblVacio;

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
        return inflador.inflate(R.layout.fragment_movimientos, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle savedInstanceState) {
        refrescar = vista.findViewById(R.id.refrescar);
        tarjetaBusqueda = vista.findViewById(R.id.tarjetaBusqueda);
        tilCuenta = vista.findViewById(R.id.tilCuenta);
        txtCuenta = vista.findViewById(R.id.txtCuenta);
        btnConsultar = vista.findViewById(R.id.btnConsultar);
        lblError = vista.findViewById(R.id.lblError);
        panelCargando = vista.findViewById(R.id.panelCargando);
        panelInicial = vista.findViewById(R.id.panelInicial);
        panelResultado = vista.findViewById(R.id.panelResultado);
        lblNumero = vista.findViewById(R.id.lblNumero);
        lblEstado = vista.findViewById(R.id.lblEstado);
        lblTitular = vista.findViewById(R.id.lblTitular);
        lblSaldo = vista.findViewById(R.id.lblSaldo);
        lblMoneda = vista.findViewById(R.id.lblMoneda);
        lblCantidad = vista.findViewById(R.id.lblCantidad);
        listaMovimientos = vista.findViewById(R.id.listaMovimientos);
        lblVacio = vista.findViewById(R.id.lblVacio);

        adaptador = new MovimientosAdaptador();
        listaMovimientos.setLayoutManager(new LinearLayoutManager(requireContext()));
        listaMovimientos.setAdapter(adaptador);
        MaterialDividerItemDecoration divisor =
                new MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL);
        divisor.setDividerColorResource(requireContext(), R.color.borde_tarjeta);
        divisor.setDividerInsetStart(getResources().getDimensionPixelSize(R.dimen.margen_divisor));
        divisor.setDividerInsetEnd(getResources().getDimensionPixelSize(R.dimen.margen_divisor));
        divisor.setLastItemDecorated(false);
        listaMovimientos.addItemDecoration(divisor);

        refrescar.setColorSchemeResources(R.color.eureka_primario);
        refrescar.setOnRefreshListener(() -> {
            if (controlador.esEmpleado() && cuentaConsultada == null) {
                refrescar.setRefreshing(false);
            } else {
                consultar(cuentaConsultada);
            }
        });

        Campos.limpiarErrorAlEscribir(tilCuenta);
        btnConsultar.setOnClickListener(v -> consultarDesdeFormulario());
        txtCuenta.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_SEARCH || esEnter(evento)) {
                consultarDesdeFormulario();
                return true;
            }
            return false;
        });
    }

    private static boolean esEnter(KeyEvent evento) {
        return evento != null && evento.getKeyCode() == KeyEvent.KEYCODE_ENTER
                && evento.getAction() == KeyEvent.ACTION_DOWN;
    }

    @Override
    protected void alMostrar() {
        if (!vistaActiva()) {
            return;
        }
        boolean empleado = controlador.esEmpleado();
        tarjetaBusqueda.setVisibility(empleado ? View.VISIBLE : View.GONE);

        if (empleado) {
            if (cuentaConsultada == null && !cargando && panelResultado.getVisibility() != View.VISIBLE) {
                panelInicial.setVisibility(View.VISIBLE);
            }
        } else {
            // El cliente siempre ve su propia cuenta; se recarga para reflejar sus últimas operaciones,
            // en segundo plano si ya hay datos en pantalla.
            if (!cargando && panelResultado.getVisibility() == View.VISIBLE) {
                refrescar.setRefreshing(true);
            }
            consultar(null);
        }
    }

    private void consultarDesdeFormulario() {
        String cuenta = Campos.texto(txtCuenta);
        ValidacionResultado validacion = controlador.validar(cuenta);
        if (!validacion.isValido()) {
            Campos.mostrarError(tilCuenta, validacion.getMensaje());
            return;
        }
        Campos.ocultarTeclado(txtCuenta);
        consultar(cuenta.trim());
    }

    private void consultar(String cuenta) {
        if (cargando) {
            return;
        }
        cargando = true;
        mostrarCargando(true);

        tareas().ejecutar(() -> controlador.consultar(cuenta), resultado -> alConsultar(resultado, cuenta), error -> {
            cargando = false;
            if (vistaActiva()) {
                mostrarCargando(false);
                mostrarError(getString(R.string.error_inesperado));
            }
        });
    }

    private void alConsultar(Resultado<ConsultaMovimientos> resultado, String cuenta) {
        cargando = false;
        if (!vistaActiva()) {
            return;
        }
        mostrarCargando(false);

        if (resultado.isSesionExpirada()) {
            volverAlLogin(resultado.getMensaje());
            return;
        }
        if (!resultado.isExito()) {
            // ECUD2 FA3: se muestra el mensaje y se conserva el número ingresado para corregirlo.
            panelResultado.setVisibility(View.GONE);
            mostrarError(resultado.getMensaje());
            return;
        }

        cuentaConsultada = cuenta;
        mostrarResultado(resultado.getDatos());
    }

    private void mostrarResultado(ConsultaMovimientos consulta) {
        Cuenta cuenta = consulta.getCuenta();
        lblError.setVisibility(View.GONE);
        panelInicial.setVisibility(View.GONE);
        panelResultado.setVisibility(View.VISIBLE);

        lblNumero.setText(cuenta.getNumero());
        MovimientoVista.enlazarEstado(lblEstado, cuenta);
        lblTitular.setText(cuenta.getTitular().isEmpty() ? Formato.SIN_DATO : cuenta.getTitular());
        lblSaldo.setText(Formato.importe(cuenta.getSaldo()));
        lblMoneda.setText(cuenta.getMoneda().isEmpty() ? Formato.SIN_DATO : cuenta.getMoneda());

        int cantidad = consulta.getMovimientos().size();
        lblCantidad.setText(getResources().getQuantityString(R.plurals.mov_cantidad, cantidad, cantidad));
        adaptador.actualizar(consulta.getMovimientosRecientes(), cuenta.getMoneda());
        listaMovimientos.setVisibility(cantidad == 0 ? View.GONE : View.VISIBLE);
        lblVacio.setVisibility(cantidad == 0 ? View.VISIBLE : View.GONE);
    }

    private void mostrarCargando(boolean activo) {
        if (!refrescar.isRefreshing()) {
            panelCargando.setVisibility(activo ? View.VISIBLE : View.GONE);
            if (activo) {
                panelInicial.setVisibility(View.GONE);
                panelResultado.setVisibility(View.GONE);
                lblError.setVisibility(View.GONE);
            }
        }
        if (!activo) {
            refrescar.setRefreshing(false);
        }
        btnConsultar.setEnabled(!activo);
        txtCuenta.setEnabled(!activo);
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisibility(View.VISIBLE);
    }
}
