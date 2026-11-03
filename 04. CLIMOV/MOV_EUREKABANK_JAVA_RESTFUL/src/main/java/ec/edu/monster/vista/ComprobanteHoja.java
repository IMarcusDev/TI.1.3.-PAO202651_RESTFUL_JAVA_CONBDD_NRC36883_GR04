package ec.edu.monster.vista;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.StringRes;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import ec.edu.monster.R;
import ec.edu.monster.modelos.data.Comprobante;
import ec.edu.monster.modelos.data.SolicitudOperacion;
import ec.edu.monster.modelos.enums.TipoOperacion;
import ec.edu.monster.utils.Formato;

/** ECUD3 paso 14: comprobante con el tipo, las cuentas, el importe, el N.º de movimiento, el nuevo saldo y la fecha. */
final class ComprobanteHoja {

    private ComprobanteHoja() { }

    static void mostrar(Context contexto, String mensaje, Comprobante comprobante, String moneda) {
        BottomSheetDialog hoja = new BottomSheetDialog(contexto);
        LayoutInflater inflador = LayoutInflater.from(contexto);
        View vista = inflador.inflate(R.layout.hoja_comprobante, null, false);

        SolicitudOperacion solicitud = comprobante.getSolicitud();
        boolean transferencia = solicitud.getTipo() == TipoOperacion.TRANSFERENCIA;

        ((TextView) vista.findViewById(R.id.lblMensaje)).setText(mensaje == null || mensaje.isEmpty()
                ? TextosOperacion.nombre(contexto, solicitud.getTipo()) : mensaje);
        ((TextView) vista.findViewById(R.id.lblImporte)).setText(Formato.importe(solicitud.getImporte(), moneda));

        ViewGroup datos = vista.findViewById(R.id.listaDatos);
        agregar(inflador, datos, R.string.comp_operacion, TextosOperacion.nombre(contexto, solicitud.getTipo()));
        agregar(inflador, datos, transferencia ? R.string.comp_cuenta_origen : R.string.comp_cuenta, solicitud.getCuenta());
        if (transferencia) {
            agregar(inflador, datos, R.string.comp_cuenta_destino, solicitud.getCuentaDestino());
        }
        agregar(inflador, datos, R.string.comp_numero, valor(comprobante.getNumeroMovimiento()));
        agregar(inflador, datos, R.string.comp_nuevo_saldo, Formato.importe(comprobante.getNuevoSaldo(), moneda));
        agregar(inflador, datos, R.string.comp_fecha, Formato.fecha(comprobante.getFecha()));

        vista.findViewById(R.id.btnListo).setOnClickListener(v -> hoja.dismiss());

        hoja.setContentView(vista);
        hoja.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        hoja.getBehavior().setSkipCollapsed(true);
        hoja.show();
    }

    private static void agregar(LayoutInflater inflador, ViewGroup lista, @StringRes int etiqueta, String valor) {
        View fila = inflador.inflate(R.layout.item_dato, lista, false);
        ((TextView) fila.findViewById(R.id.lblEtiqueta)).setText(etiqueta);
        ((TextView) fila.findViewById(R.id.lblValor)).setText(valor);
        lista.addView(fila);
    }

    private static String valor(String texto) {
        return texto == null || texto.isEmpty() ? Formato.SIN_DATO : texto;
    }
}
