package ec.edu.monster.vista;

import ec.edu.monster.controlador.MovimientoController;
import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.modelos.dto.ConsultaResponseDTO;
import ec.edu.monster.modelos.dto.MovimientoDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.modelos.http.ConfigHTTP;
import ec.edu.monster.vista.utils.Formato;
import ec.edu.monster.vista.utils.MessagesUtils;
import java.util.ArrayList;
import java.util.List;

/** ECUD2: cabecera de la cuenta y tabla con N.º Mov., Fecha, Tipo, Acción, Importe y Cuenta de referencia. */
//revisado y aprovado por Marcos Escobar
public class ConsultaView {
  private static final MovimientoController CONTROLLER = new MovimientoController();

  public static void show() {
    MessagesUtils.printTitle("CONSULTAR MOVIMIENTOS");

    // ECUD2 paso 2: el cliente consulta la cuenta de su sesión.
    String cuenta = ConfigHTTP.getCuenta();

    if (!ConfigHTTP.esCliente()) {
      cuenta = MessagesUtils.readText("Numero de cuenta");
      String error = ValidadorDatos.errorCuenta(cuenta);
      if (error != null) {
        MessagesUtils.printError(error);
        return;
      }
    }

    MessagesUtils.printMessage("Procesando...");
    ConsultaResponseDTO res = CONTROLLER.consultar(cuenta);

    if (MenuView.revisarSesion(res))
      return;

    if (res.getEstado() != EstadosEnum.OK) {
      MessagesUtils.printError(res.getMensaje());
      return;
    }

    System.out.println();
    System.out.println("Cuenta:  " + res.getNumeroCuenta());
    System.out.println("Titular: " + res.getTitular());
    System.out.println("Moneda:  " + res.getMoneda());
    System.out.println("Saldo:   " + Formato.importe(res.getSaldo(), res.getMoneda()));
    System.out.println("Estado:  " + res.getEstadoCuenta());
    System.out.println();

    if (res.getMovimientos().isEmpty()) {
      MessagesUtils.printMessage("La cuenta no registra movimientos");
      return;
    }

    List<String[]> filas = new ArrayList<>();
    for (MovimientoDTO m : res.getMovimientos()) {
      filas.add(new String[] {
        String.valueOf(m.getNumeroMovimiento()),
        Formato.fecha(m.getFecha()),
        m.getTipoMovimiento(),
        m.getAccion(),
        (m.esIngreso() ? "+" : "-") + Formato.importe(m.getImporte()),
        m.getCuentaReferencia().isBlank() ? Formato.SIN_DATO : m.getCuentaReferencia()
      });
    }

    MessagesUtils.printTable(
      new String[] { "N.º Mov.", "Fecha", "Tipo", "Acción", "Importe", "Cuenta ref." },
      filas,
      new boolean[] { true, false, false, false, true, false }
    );
  }
}
