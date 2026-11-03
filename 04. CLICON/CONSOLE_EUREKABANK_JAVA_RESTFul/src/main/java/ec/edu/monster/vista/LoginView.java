package ec.edu.monster.vista;

import ec.edu.monster.controlador.LoginController;
import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.modelos.dto.LoginResponseDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.vista.utils.MessagesUtils;

//revisado y aprovado por Marcos Escobar
public class LoginView {
  private static final LoginController CONTROLLER = new LoginController();

  /** ECUD1: el usuario elige su perfil e ingresa sus credenciales. Devuelve false si elige salir. */
  public static boolean userHasLogin() {
    while (true) {
      MessagesUtils.printTitle("INICIAR SESION");

      System.out.println("1. Empleado");
      System.out.println("2. Cliente");
      System.out.println("3. Salir");

      String opcion = MessagesUtils.readText("\nSeleccione su perfil");

      LoginResponseDTO res;

      switch (opcion) {
        case "1" -> res = loginEmpleado();
        case "2" -> res = loginCliente();
        case "3" -> {
          return false;
        }
        default -> {
          MessagesUtils.printError("Opcion invalida");
          continue;
        }
      }

      if (res == null)
        continue;

      if (res.getEstado() == EstadosEnum.OK) {
        MessagesUtils.printMessage(res.getMensaje());
        MessagesUtils.esperar(1);
        return true;
      }

      // ECUD1 alterno 1: se muestra el mensaje y se mantiene el formulario para otro intento.
      MessagesUtils.printError(res.getMensaje());
      System.out.println();
    }
  }

  private static LoginResponseDTO loginEmpleado() {
    String usuario = MessagesUtils.readText("Usuario");
    if (usuario.isEmpty()) {
      MessagesUtils.printError(ValidadorDatos.INGRESE_USUARIO);
      return null;
    }

    String clave = MessagesUtils.readPassword("Clave");
    if (clave.isEmpty()) {
      MessagesUtils.printError(ValidadorDatos.INGRESE_CLAVE);
      return null;
    }

    MessagesUtils.printMessage("Procesando...");
    return CONTROLLER.loginEmpleado(usuario, clave);
  }

  private static LoginResponseDTO loginCliente() {
    String cuenta = MessagesUtils.readText("Numero de cuenta");
    String error = ValidadorDatos.errorCuenta(cuenta);
    if (error != null) {
      MessagesUtils.printError(error);
      return null;
    }

    String clave = MessagesUtils.readPassword("Clave");
    error = ValidadorDatos.errorClaveCuenta(clave);
    if (error != null) {
      MessagesUtils.printError(error);
      return null;
    }

    MessagesUtils.printMessage("Procesando...");
    return CONTROLLER.loginCliente(cuenta, clave);
  }
}
