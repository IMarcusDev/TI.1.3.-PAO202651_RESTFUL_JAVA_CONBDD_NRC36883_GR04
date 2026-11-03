package ec.edu.monster.vista;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import ec.edu.monster.AplicacionEureka;
import ec.edu.monster.R;
import ec.edu.monster.config.RestConfig;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.data.ValidacionResultado;
import ec.edu.monster.modelos.enums.Campo;
import ec.edu.monster.modelos.enums.Perfil;
import ec.edu.monster.servicios.EjecutorTareas;

/** ECUD1 Iniciar Sesión: selector de perfil y credenciales según el perfil. */
public class LoginActivity extends AppCompatActivity {

    private LoginControlador controlador;
    private EjecutorTareas tareas;
    private Perfil perfil = Perfil.EMPLEADO;

    private MaterialButtonToggleGroup grupoPerfil;
    private TextInputLayout tilCredencial;
    private TextInputLayout tilClave;
    private EditText txtCredencial;
    private EditText txtClave;
    private MaterialButton btnIngresar;
    private CircularProgressIndicator prgCargando;
    private TextView lblError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        setContentView(R.layout.activity_login);

        AplicacionEureka app = AplicacionEureka.de(this);
        controlador = app.crearLoginControlador();
        tareas = app.getTareas();

        if (controlador.haySesion()) {
            abrirPrincipal();
            return;
        }

        grupoPerfil = findViewById(R.id.grupoPerfil);
        tilCredencial = findViewById(R.id.tilCredencial);
        tilClave = findViewById(R.id.tilClave);
        txtCredencial = findViewById(R.id.txtCredencial);
        txtClave = findViewById(R.id.txtClave);
        btnIngresar = findViewById(R.id.btnIngresar);
        prgCargando = findViewById(R.id.prgCargando);
        lblError = findViewById(R.id.lblError);

        grupoPerfil.check(R.id.btnEmpleado);
        grupoPerfil.addOnButtonCheckedListener((grupo, id, marcado) -> {
            if (marcado) {
                aplicarPerfil(id == R.id.btnCliente ? Perfil.CLIENTE : Perfil.EMPLEADO);
            }
        });
        aplicarPerfil(Perfil.EMPLEADO);

        Campos.limpiarErrorAlEscribir(tilCredencial, tilClave);
        txtCredencial.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_NEXT || esEnter(evento)) {
                txtClave.requestFocus();
                return true;
            }
            return false;
        });
        txtClave.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_GO || esEnter(evento)) {
                alIngresar();
                return true;
            }
            return false;
        });
        btnIngresar.setOnClickListener(v -> alIngresar());

        TextView lblServidor = findViewById(R.id.lblServidor);
        lblServidor.setText(getString(R.string.login_servidor, Uri.parse(RestConfig.URL_BASE).getAuthority()));

        AjusteBordes.barraSuperior(findViewById(R.id.encabezado));
        AjusteBordes.contenidoInferior(findViewById(R.id.contenido));
    }

    /** El empleado usa usuario y clave; el cliente, número de cuenta (8 dígitos) y clave (6 dígitos). */
    private void aplicarPerfil(Perfil nuevo) {
        perfil = nuevo;
        lblError.setVisibility(View.GONE);
        Campos.limpiarErrores(tilCredencial, tilClave);
        txtCredencial.setText("");
        txtClave.setText("");

        if (nuevo == Perfil.CLIENTE) {
            tilCredencial.setHint(R.string.login_cuenta);
            tilCredencial.setStartIconDrawable(R.drawable.ic_tarjeta);
            tilCredencial.setHelperText(getString(R.string.login_ayuda_cuenta));
            txtCredencial.setInputType(InputType.TYPE_CLASS_NUMBER);
            txtCredencial.setFilters(longitud(ValidadorDatos.LARGO_CUENTA));

            tilClave.setHint(R.string.login_clave_cuenta);
            tilClave.setHelperText(getString(R.string.login_ayuda_clave_cuenta));
            txtClave.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            txtClave.setFilters(longitud(ValidadorDatos.LARGO_CLAVE_CUENTA));
        } else {
            tilCredencial.setHint(R.string.login_usuario);
            tilCredencial.setStartIconDrawable(R.drawable.ic_persona);
            tilCredencial.setHelperText(null);
            txtCredencial.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            txtCredencial.setFilters(longitud(ValidadorDatos.MAX_USUARIO));

            tilClave.setHint(R.string.login_clave);
            tilClave.setHelperText(null);
            txtClave.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            txtClave.setFilters(longitud(ValidadorDatos.MAX_CLAVE_EMPLEADO));
        }
        // Al cambiar el tipo de entrada se reinicia el botón de mostrar/ocultar clave.
        tilClave.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        tilCredencial.getEditText().requestFocus();
    }

    private static InputFilter[] longitud(int maximo) {
        return new InputFilter[] { new InputFilter.LengthFilter(maximo) };
    }

    private static boolean esEnter(KeyEvent evento) {
        return evento != null && evento.getKeyCode() == KeyEvent.KEYCODE_ENTER
                && evento.getAction() == KeyEvent.ACTION_DOWN;
    }

    private void alIngresar() {
        if (!btnIngresar.isEnabled()) {
            return;
        }

        Perfil perfilElegido = perfil;
        String credencial = Campos.texto(txtCredencial);
        String clave = Campos.texto(txtClave);
        lblError.setVisibility(View.GONE);

        ValidacionResultado validacion = controlador.validar(perfilElegido, credencial, clave);
        if (!validacion.isValido()) {
            Campos.mostrarError(validacion.getCampo() == Campo.CLAVE ? tilClave : tilCredencial, validacion.getMensaje());
            return;
        }

        Campos.ocultarTeclado(btnIngresar);
        mostrarCargando(true);
        tareas.ejecutar(() -> controlador.iniciarSesion(perfilElegido, credencial, clave),
                this::alTerminarLogin,
                error -> {
                    if (sigueAbierta()) {
                        mostrarCargando(false);
                        mostrarError(getString(R.string.error_inesperado));
                    }
                });
    }

    private void alTerminarLogin(Resultado<Sesion> resultado) {
        if (!sigueAbierta()) {
            return;
        }
        mostrarCargando(false);
        if (resultado.isExito()) {
            abrirPrincipal();
        } else {
            // ECUD1 FA1: se muestra el mensaje y se borra la clave para un nuevo intento.
            txtClave.setText("");
            mostrarError(resultado.getMensaje());
        }
    }

    private void abrirPrincipal() {
        startActivity(new Intent(this, PrincipalActivity.class));
        finish();
    }

    private boolean sigueAbierta() {
        return !isFinishing() && !isDestroyed();
    }

    private void mostrarCargando(boolean cargando) {
        btnIngresar.setEnabled(!cargando);
        btnIngresar.setText(cargando ? "" : getString(R.string.login_ingresar));
        prgCargando.setVisibility(cargando ? View.VISIBLE : View.GONE);
        txtCredencial.setEnabled(!cargando);
        txtClave.setEnabled(!cargando);
        for (int i = 0; i < grupoPerfil.getChildCount(); i++) {
            grupoPerfil.getChildAt(i).setEnabled(!cargando);
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisibility(View.VISIBLE);
    }
}
