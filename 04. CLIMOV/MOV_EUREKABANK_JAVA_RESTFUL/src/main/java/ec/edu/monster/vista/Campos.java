package ec.edu.monster.vista;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.google.android.material.textfield.TextInputLayout;

/** Utilidades para los campos de texto de los formularios. */
final class Campos {

    private Campos() { }

    static String texto(EditText campo) {
        return campo.getText() != null ? campo.getText().toString() : "";
    }

    static void limpiarErrorAlEscribir(TextInputLayout... campos) {
        for (TextInputLayout campo : campos) {
            EditText editor = campo.getEditText();
            if (editor == null) {
                continue;
            }
            editor.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int inicio, int cantidad, int despues) { }

                @Override
                public void onTextChanged(CharSequence s, int inicio, int antes, int cantidad) {
                    if (campo.getError() != null) {
                        campo.setError(null);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }
    }

    static void limpiarErrores(TextInputLayout... campos) {
        for (TextInputLayout campo : campos) {
            campo.setError(null);
        }
    }

    static void mostrarError(TextInputLayout campo, String mensaje) {
        campo.setError(mensaje);
        if (campo.getEditText() != null) {
            campo.getEditText().requestFocus();
        }
    }

    static void ocultarTeclado(View vista) {
        InputMethodManager teclado = (InputMethodManager) vista.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (teclado != null) {
            teclado.hideSoftInputFromWindow(vista.getWindowToken(), 0);
        }
    }
}
