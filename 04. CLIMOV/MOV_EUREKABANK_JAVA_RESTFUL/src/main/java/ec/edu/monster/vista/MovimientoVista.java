package ec.edu.monster.vista;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import ec.edu.monster.R;
import ec.edu.monster.modelos.data.Cuenta;
import ec.edu.monster.modelos.data.Movimiento;
import ec.edu.monster.utils.Formato;

/** Fila de un movimiento: N.º, fecha, tipo, acción, importe y cuenta de referencia (ECUD2 paso 9). */
final class MovimientoVista {

    private MovimientoVista() { }

    static View crear(LayoutInflater inflador, ViewGroup padre) {
        return inflador.inflate(R.layout.item_movimiento, padre, false);
    }

    static void enlazar(View fila, Movimiento movimiento, String moneda) {
        Context contexto = fila.getContext();
        boolean ingreso = movimiento.esIngreso();
        int color = ContextCompat.getColor(contexto, ingreso ? R.color.ingreso : R.color.salida);
        int fondo = ContextCompat.getColor(contexto, ingreso ? R.color.ingreso_fondo : R.color.salida_fondo);

        ImageView icono = fila.findViewById(R.id.imgMovimiento);
        icono.setImageResource(ingreso ? R.drawable.ic_ingreso : R.drawable.ic_salida);
        icono.setImageTintList(ColorStateList.valueOf(color));
        icono.setBackgroundTintList(ColorStateList.valueOf(fondo));

        ((TextView) fila.findViewById(R.id.lblTipo)).setText(movimiento.getTipo());
        ((TextView) fila.findViewById(R.id.lblDetalle)).setText(contexto.getString(R.string.mov_numero_fecha,
                movimiento.getNumero(), Formato.fecha(movimiento.getFecha())));

        TextView referencia = fila.findViewById(R.id.lblReferencia);
        if (movimiento.tieneReferencia()) {
            referencia.setText(contexto.getString(R.string.mov_referencia, movimiento.getCuentaReferencia()));
            referencia.setVisibility(View.VISIBLE);
        } else {
            referencia.setVisibility(View.GONE);
        }

        TextView importe = fila.findViewById(R.id.lblImporte);
        importe.setText((ingreso ? "+" : "-") + Formato.importe(movimiento.getImporte(), moneda));
        importe.setTextColor(color);

        TextView accion = fila.findViewById(R.id.lblAccion);
        accion.setText(ingreso ? R.string.mov_ingreso : R.string.mov_salida);
        accion.setTextColor(color);
    }

    /** Pastilla de estado de la cuenta: verde si está activa, roja si no. */
    static void enlazarEstado(TextView pastilla, Cuenta cuenta) {
        Context contexto = pastilla.getContext();
        boolean activa = cuenta.estaActiva();
        String estado = cuenta.getEstado();
        pastilla.setText(estado == null || estado.isEmpty() ? Cuenta.ESTADO_ACTIVO : estado);
        pastilla.setTextColor(ContextCompat.getColor(contexto, activa ? R.color.ingreso : R.color.salida));
        pastilla.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(contexto, activa ? R.color.ingreso_fondo : R.color.salida_fondo)));
    }
}
