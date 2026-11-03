package ec.edu.monster.vista;

import ec.edu.monster.controlador.ErroresCliente;
import ec.edu.monster.controlador.LoginController;
import ec.edu.monster.modelos.dto.ResponseDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.modelos.http.ConfigHTTP;
import ec.edu.monster.vista.utils.MessagesUtils;

/** ECUD1 paso 7: menú del perfil con «Consultar movimientos», «Registrar movimiento» y «Cerrar sesión». */
//revisado y aprovado por Marcos Escobar
public class MenuView {
  public static void show() {
    while (ConfigHTTP.haySesion()) {
      MessagesUtils.printTitle("MENU PRINCIPAL");
      System.out.println(ConfigHTTP.getNombre() + " - "
        + (ConfigHTTP.esCliente() ? "Cliente, cuenta " + ConfigHTTP.getCuenta() : "Empleado") + "\n");

      System.out.println("1. Consultar movimientos");
      System.out.println("2. Registrar movimiento");
      System.out.println("3. Cerrar sesion");

      String opcion = MessagesUtils.readText("\nSeleccione una opcion");

      switch (opcion) {
        case "1" -> ConsultaView.show();
        case "2" -> MovimientoView.show();
        case "3" -> cerrarSesion();
        default -> MessagesUtils.printError("Opcion invalida");
      }

      System.out.println();
    }
  }

  /** ECUD1 alterno 5: se borra la sesión y se vuelve al inicio aunque el servidor no responda. */
  private static void cerrarSesion() {
    ResponseDTO res = new LoginController().logout();
    MessagesUtils.printMessage(res.getEstado() == EstadosEnum.OK ? res.getMensaje() : "Sesion cerrada");
  }

  /** ECUD4: con el token vencido se informa y se vuelve al inicio de sesión. */
  public static boolean revisarSesion(ResponseDTO res) {
    if (!res.sesionExpirada())
      return false;

    MessagesUtils.printError(ErroresCliente.SESION_EXPIRADA);
    ConfigHTTP.limpiar();
    return true;
  }
}
