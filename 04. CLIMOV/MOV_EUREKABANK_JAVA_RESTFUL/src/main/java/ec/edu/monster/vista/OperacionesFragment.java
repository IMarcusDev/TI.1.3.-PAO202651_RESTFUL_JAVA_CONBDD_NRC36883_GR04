package ec.edu.monster.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import ec.edu.monster.AplicacionEureka;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ControladorConSesion;
import ec.edu.monster.controlador.OperacionControlador;
import ec.edu.monster.modelos.data.Comprobante;
import ec.edu.monster.modelos.data.Cuenta;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.SolicitudOperacion;
import ec.edu.monster.modelos.data.ValidacionResultado;
import ec.edu.monster.modelos.enums.TipoOperacion;
import ec.edu.monster.utils.Formato;
import java.util.List;
import java.util.Locale;

/**
 * ECUD3 Registrar Movimiento: formulario del tipo elegido según el perfil, resumen para confirmar
 * y comprobante con el número de movimiento y el nuevo saldo.
 */
public class OperacionesFragment extends PestanaFragment {

    private OperacionControlador controlador;
    private TipoOperacion tipo;
    private boolean configurado;
    private boolean registrando;
    private boolean cargandoCuenta;

    private MaterialButtonToggleGroup grupoTipo;
    private TextView lblDescripcion;
    private View tarjetaOrigen;
    private TextView lblCuentaOrigen;
    private TextView lblSaldoOrigen;
    private View prgOrigen;
    private TextInputLayout tilCuenta;
    private TextInputLayout tilDestino;
    private TextInputLayout tilImporte;
    private TextInputLayout tilClave;
    private EditText txtCuenta;
    private EditText txtDestino;
    private EditText txtImporte;
    private EditText txtClave;
    private TextView lblError;
    private MaterialButton btnRegistrar;
    private View prgRegistrar;

    @Override
    protected ControladorConSesion controlador() {
        return controlador;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controlador = AplicacionEureka.de(requireContext()).crearOperacionControlador();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor,
                             @Nullable Bundle savedInstanceState) {
        return inflador.inflate(R.layout.fragment_operaciones, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle savedInstanceState) {
        grupoTipo = vista.findViewById(R.id.grupoTipo);
        lblDescripcion = vista.findViewById(R.id.lblDescripcion);
        tarjetaOrigen = vista.findViewById(R.id.tarjetaOrigen);
        lblCuentaOrigen = vista.findViewById(R.id.lblCuentaOrigen);
        lblSaldoOrigen = vista.findViewById(R.id.lblSaldoOrigen);
        prgOrigen = vista.findViewById(R.id.prgOrigen);
        tilCuenta = vista.findViewById(R.id.tilCuenta);
        tilDestino = vista.findViewById(R.id.tilDestino);
        tilImporte = vista.findViewById(R.id.tilImporte);
        tilClave = vista.findViewById(R.id.tilClave);
        txtCuenta = vista.findViewById(R.id.txtCuenta);
        txtDestino = vista.findViewById(R.id.txtDestino);
        txtImporte = vista.findViewById(R.id.txtImporte);
        txtClave = vista.findViewById(R.id.txtClave);
        lblError = vista.findViewById(R.id.lblError);
        btnRegistrar = vista.findViewById(R.id.btnRegistrar);
        prgRegistrar = vista.findViewById(R.id.prgRegistrar);

        grupoTipo.addOnButtonCheckedListener((grupo, id, marcado) -> {
            if (marcado) {
                aplicarTipo(tipoDe(id));
            }
        });
        Campos.limpiarErrorAlEscribir(tilCuenta, tilDestino, tilImporte, tilClave);
        txtClave.setOnEditorActionListener((v, accion, evento) -> {
            alRegistrar();
            return true;
        });
        btnRegistrar.setOnClickListener(v -> alRegistrar());
    }

    @Override
    protected void alMostrar() {
        if (!vistaActiva()) {
            return;
        }
        if (!configurado) {
            configurarPerfil();
            configurado = true;
        }

        TipoOperacion pedido = principal().tomarTipoSolicitado();
        if (pedido != null && controlador.tiposDisponibles().contains(pedido)) {
            seleccionar(pedido);
        }

        if (!controlador.pideCuenta()) {
            mostrarCuentaOrigen(controlador.cuentaClienteGuardada());
            cargarCuentaCliente();
        }
    }

    /** ECUD3 paso 1: al cliente solo se le muestran Retiro y Transferencia. */
    private void configurarPerfil() {
        List<TipoOperacion> tipos = controlador.tiposDisponibles();
        for (TipoOperacion disponible : TipoOperacion.values()) {
            grupoTipo.findViewById(idDe(disponible))
                     .setVisibility(tipos.contains(disponible) ? View.VISIBLE : View.GONE);
        }
        tarjetaOrigen.setVisibility(controlador.pideCuenta() ? View.GONE : View.VISIBLE);
        seleccionar(tipos.get(0));
    }

    private void seleccionar(TipoOperacion nuevo) {
        if (grupoTipo.getCheckedButtonId() == idDe(nuevo)) {
            aplicarTipo(nuevo);
        } else {
            grupoTipo.check(idDe(nuevo));
        }
    }

    /** ECUD3 paso 2: muestra los campos del tipo elegido según el perfil. */
    private void aplicarTipo(TipoOperacion nuevo) {
        tipo = nuevo;
        boolean transferencia = nuevo == TipoOperacion.TRANSFERENCIA;

        lblError.setVisibility(View.GONE);
        Campos.limpiarErrores(tilCuenta, tilDestino, tilImporte, tilClave);
        lblDescripcion.setText(TextosOperacion.descripcion(requireContext(), nuevo));

        tilCuenta.setVisibility(controlador.pideCuenta() ? View.VISIBLE : View.GONE);
        tilCuenta.setHint(transferencia ? R.string.op_cuenta_origen : R.string.op_cuenta);
        tilDestino.setVisibility(controlador.pideDestino(nuevo) ? View.VISIBLE : View.GONE);
        tilClave.setVisibility(controlador.pideClave(nuevo) ? View.VISIBLE : View.GONE);
        tilClave.setHint(transferencia ? R.string.op_clave_cuenta_origen : R.string.op_clave_cuenta);
        txtImporte.setImeOptions(controlador.pideClave(nuevo)
                ? EditorInfo.IME_ACTION_NEXT
                : EditorInfo.IME_ACTION_DONE);

        btnRegistrar.setText(TextosOperacion.verbo(requireContext(), nuevo));
        btnRegistrar.setIconResource(TextosOperacion.icono(nuevo));
    }

    private void cargarCuentaCliente() {
        if (cargandoCuenta) {
            return;
        }
        cargandoCuenta = true;
        prgOrigen.setVisibility(View.VISIBLE);
        tareas().ejecutar(controlador::cargarCuentaCliente, resultado -> {
            cargandoCuenta = false;
            if (!vistaActiva()) {
                return;
            }
            prgOrigen.setVisibility(View.GONE);
            if (resultado.isSesionExpirada()) {
                volverAlLogin(resultado.getMensaje());
            } else if (resultado.isExito()) {
                mostrarCuentaOrigen(resultado.getDatos());
            }
        }, error -> {
            cargandoCuenta = false;
            if (vistaActiva()) {
                prgOrigen.setVisibility(View.GONE);
            }
        });
    }

    private void mostrarCuentaOrigen(Cuenta cuenta) {
        lblCuentaOrigen.setText(cuenta != null ? cuenta.getNumero() : controlador.getCuentaSesion());
        lblSaldoOrigen.setText(cuenta != null
                ? getString(R.string.op_saldo_disponible, Formato.importe(cuenta.getSaldo(), cuenta.getMoneda()))
                : "");
        lblSaldoOrigen.setVisibility(cuenta != null ? View.VISIBLE : View.GONE);
    }

    private void alRegistrar() {
        if (registrando || tipo == null) {
            return;
        }
        lblError.setVisibility(View.GONE);

        String cuenta = Campos.texto(txtCuenta);
        String destino = Campos.texto(txtDestino);
        String importe = Campos.texto(txtImporte);
        String clave = Campos.texto(txtClave);

        ValidacionResultado validacion = controlador.validar(tipo, cuenta, destino, importe, clave);
        if (!validacion.isValido()) {
            mostrarErrorCampo(validacion);
            return;
        }

        Campos.ocultarTeclado(btnRegistrar);
        confirmar(controlador.preparar(tipo, cuenta, destino, importe, clave));
    }

    private void mostrarErrorCampo(ValidacionResultado validacion) {
        switch (validacion.getCampo()) {
            case CUENTA:
                Campos.mostrarError(tilCuenta, validacion.getMensaje());
                break;
            case DESTINO:
                Campos.mostrarError(tilDestino, validacion.getMensaje());
                break;
            case IMPORTE:
                Campos.mostrarError(tilImporte, validacion.getMensaje());
                break;
            case CLAVE:
                Campos.mostrarError(tilClave, validacion.getMensaje());
                break;
            default:
                mostrarError(validacion.getMensaje());
        }
    }

    /** ECUD3 pasos 4 y 5: resumen de la operación para que el usuario la confirme. */
    private void confirmar(SolicitudOperacion solicitud) {
        String importe = Formato.importe(solicitud.getImporte(), monedaConocida());
        String resumen;
        switch (solicitud.getTipo()) {
            case DEPOSITO:
                resumen = getString(R.string.op_confirmar_deposito, importe, solicitud.getCuenta());
                break;
            case RETIRO:
                resumen = getString(R.string.op_confirmar_retiro, importe, solicitud.getCuenta());
                break;
            default:
                resumen = getString(R.string.op_confirmar_transferencia, importe,
                        solicitud.getCuenta(), solicitud.getCuentaDestino());
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.op_confirmar_titulo)
                .setIcon(TextosOperacion.icono(solicitud.getTipo()))
                .setMessage(resumen)
                .setPositiveButton(R.string.op_confirmar, (dialogo, boton) -> enviar(solicitud))
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void enviar(SolicitudOperacion solicitud) {
        registrando = true;
        mostrarCargando(true);
        tareas().ejecutar(() -> controlador.registrar(solicitud), this::alTerminar, error -> {
            registrando = false;
            if (vistaActiva()) {
                mostrarCargando(false);
                mostrarError(getString(R.string.error_inesperado));
            }
        });
    }

    private void alTerminar(Resultado<Comprobante> resultado) {
        registrando = false;
        if (!vistaActiva()) {
            return;
        }
        mostrarCargando(false);

        if (resultado.isSesionExpirada()) {
            volverAlLogin(resultado.getMensaje());
            return;
        }
        if (!resultado.isExito()) {
            // ECUD3 FA3: ante una clave incorrecta se borra el campo de clave; el resto se conserva.
            String mensaje = resultado.getMensaje();
            if (mensaje != null && mensaje.toLowerCase(Locale.ROOT).contains("clave")) {
                txtClave.setText("");
            }
            mostrarError(mensaje);
            return;
        }

        ComprobanteHoja.mostrar(requireContext(), resultado.getMensaje(), resultado.getDatos(), monedaConocida());
        limpiarFormulario();
        if (!controlador.pideCuenta()) {
            mostrarCuentaOrigen(controlador.cuentaClienteGuardada());
        }
    }

    private String monedaConocida() {
        Cuenta cuenta = controlador.pideCuenta() ? null : controlador.cuentaClienteGuardada();
        return cuenta != null ? cuenta.getMoneda() : null;
    }

    private void limpiarFormulario() {
        txtCuenta.setText("");
        txtDestino.setText("");
        txtImporte.setText("");
        txtClave.setText("");
        Campos.limpiarErrores(tilCuenta, tilDestino, tilImporte, tilClave);
    }

    /** RNF-013: bloquea los controles mientras se procesa la solicitud. */
    private void mostrarCargando(boolean activo) {
        btnRegistrar.setEnabled(!activo);
        btnRegistrar.setText(activo ? "" : TextosOperacion.verbo(requireContext(), tipo));
        if (activo) {
            btnRegistrar.setIcon(null);
        } else {
            btnRegistrar.setIconResource(TextosOperacion.icono(tipo));
        }
        prgRegistrar.setVisibility(activo ? View.VISIBLE : View.GONE);
        for (int i = 0; i < grupoTipo.getChildCount(); i++) {
            grupoTipo.getChildAt(i).setEnabled(!activo);
        }
        txtCuenta.setEnabled(!activo);
        txtDestino.setEnabled(!activo);
        txtImporte.setEnabled(!activo);
        txtClave.setEnabled(!activo);
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisibility(View.VISIBLE);
    }

    @IdRes
    private static int idDe(TipoOperacion tipo) {
        switch (tipo) {
            case DEPOSITO:
                return R.id.btnDeposito;
            case RETIRO:
                return R.id.btnRetiro;
            default:
                return R.id.btnTransferencia;
        }
    }

    private static TipoOperacion tipoDe(@IdRes int id) {
        if (id == R.id.btnDeposito) {
            return TipoOperacion.DEPOSITO;
        }
        return id == R.id.btnRetiro ? TipoOperacion.RETIRO : TipoOperacion.TRANSFERENCIA;
    }
}
