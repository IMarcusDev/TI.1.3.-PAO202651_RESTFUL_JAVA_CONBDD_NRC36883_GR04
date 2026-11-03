package ec.edu.monster.servicios;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class EjecutorTareas {

    private final ExecutorService ejecutor;
    private final Handler hiloPrincipal = new Handler(Looper.getMainLooper());

    public EjecutorTareas(ExecutorService ejecutor) {
        this.ejecutor = ejecutor;
    }

    public <T> void ejecutar(Callable<T> tarea, Consumer<T> alTerminar, Consumer<Throwable> alFallar) {
        ejecutor.execute(() -> {
            try {
                T resultado = tarea.call();
                hiloPrincipal.post(() -> alTerminar.accept(resultado));
            } catch (Throwable e) {
                hiloPrincipal.post(() -> alFallar.accept(e));
            }
        });
    }
}
