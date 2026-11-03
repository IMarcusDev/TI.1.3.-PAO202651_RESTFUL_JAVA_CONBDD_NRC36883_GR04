package ec.edu.monster.vista;

import android.content.Context;
import androidx.annotation.DrawableRes;
import ec.edu.monster.R;
import ec.edu.monster.modelos.enums.TipoOperacion;

/** Textos e íconos de cada tipo de operación. */
final class TextosOperacion {

    private TextosOperacion() { }

    static String nombre(Context contexto, TipoOperacion tipo) {
        switch (tipo) {
            case DEPOSITO:
                return contexto.getString(R.string.op_deposito);
            case RETIRO:
                return contexto.getString(R.string.op_retiro);
            default:
                return contexto.getString(R.string.op_transferencia);
        }
    }

    static String verbo(Context contexto, TipoOperacion tipo) {
        switch (tipo) {
            case DEPOSITO:
                return contexto.getString(R.string.op_depositar);
            case RETIRO:
                return contexto.getString(R.string.op_retirar);
            default:
                return contexto.getString(R.string.op_transferir);
        }
    }

    static String descripcion(Context contexto, TipoOperacion tipo) {
        switch (tipo) {
            case DEPOSITO:
                return contexto.getString(R.string.accion_deposito_detalle);
            case RETIRO:
                return contexto.getString(R.string.accion_retiro_detalle);
            default:
                return contexto.getString(R.string.accion_transferencia_detalle);
        }
    }

    @DrawableRes
    static int icono(TipoOperacion tipo) {
        switch (tipo) {
            case DEPOSITO:
                return R.drawable.ic_deposito;
            case RETIRO:
                return R.drawable.ic_retiro;
            default:
                return R.drawable.ic_transferir;
        }
    }
}
