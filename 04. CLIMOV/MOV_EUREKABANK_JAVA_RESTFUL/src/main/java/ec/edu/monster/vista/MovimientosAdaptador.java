package ec.edu.monster.vista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ec.edu.monster.modelos.data.Movimiento;
import java.util.ArrayList;
import java.util.List;

class MovimientosAdaptador extends RecyclerView.Adapter<MovimientosAdaptador.Fila> {

    private final List<Movimiento> movimientos = new ArrayList<>();
    private String moneda;

    void actualizar(List<Movimiento> nuevos, String monedaCuenta) {
        movimientos.clear();
        movimientos.addAll(nuevos);
        moneda = monedaCuenta;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Fila(MovimientoVista.crear(LayoutInflater.from(padre.getContext()), padre));
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        MovimientoVista.enlazar(fila.itemView, movimientos.get(posicion), moneda);
    }

    @Override
    public int getItemCount() {
        return movimientos.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        Fila(View vista) {
            super(vista);
        }
    }
}
