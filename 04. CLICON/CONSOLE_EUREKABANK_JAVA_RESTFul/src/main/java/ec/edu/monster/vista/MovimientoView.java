package ec.edu.monster.vista;

import ec.edu.monster.controlador.MovimientoController;
import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.modelos.dto.ConsultaResponseDTO;
import ec.edu.monster.modelos.dto.MovimientoResponseDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.modelos.http.ConfigHTTP;
import ec.edu.monster.vista.utils.Formato;
import ec.edu.monster.vista.utils.MessagesUtils;
import java.math.BigDecimal;
import java.util.Date;

//revisado y aprovado por Marcos Escobar
public class MovimientoView {
  private static final MovimientoController CONTROLLER = new MovimientoController();

  /** ECUD3 paso 1: el empleado elige Depósito, Retiro o Transferencia; al cliente solo se le muestran Retiro y Transferencia. */
  public static void show() {
    String[] opciones = ConfigHTTP.esCliente()
      ? new String[] { "Retiro", "Transferencia" }
      : new String[] { "Deposito", "Retiro", "Transferencia" };

    while (ConfigHTTP.haySesion()) {
      MessagesUtils.printTitle("MENU MOVIMIENTOS");

      for (int i = 0; i < opciones.length; i++)
        System.out.println((i + 1) + ". " + opciones[i]);
      System.out.println((opciones.length + 1) + ". Regresar");

      int opcion;
      try {
        opcion = Integer.parseInt(MessagesUtils.readText("\nSeleccione una opcion"));
      } catch (NumberFormatException e) {
        opcion = 0;
      }

      if (opcion < 1 || opcion > opciones.length + 1) {
        MessagesUtils.printError("Opcion invalida");
        continue;
      }

      if (opcion == opciones.length + 1) {
        MessagesUtils.printMessage("Regresando...");
        return;
      }

      switch (opciones[opcion - 1]) {
        case "Deposito" -> deposito();
        case "Retiro" -> retiro();
        default -> transferencia();
      }

      System.out.println();
    }
  }

  private static void deposito() {
    MessagesUtils.printTitle("DEPOSITO");

    String cuenta = pedirCuenta("Numero de cuenta");
    if (cuenta == null)
      return;

    BigDecimal importe = pedirImporte();
    if (importe == null)
      return;

    if (!MessagesUtils.confirm("¿Depositar " + Formato.importe(importe, "USD") + " en la cuenta " + cuenta + "?")) {
      MessagesUtils.printMessage("Operacion cancelada");
      return;
    }

    MessagesUtils.printMessage("Procesando...");
    printResponse(CONTROLLER.deposito(cuenta, importe));
  }

  private static void retiro() {
    MessagesUtils.printTitle("RETIRO");

    String cuenta;
    String clave = "";

    if (ConfigHTTP.esCliente()) {
      cuenta = ConfigHTTP.getCuenta();
      mostrarCuentaPropia();
    } else {
      cuenta = pedirCuenta("Numero de cuenta");
      if (cuenta == null)
        return;
    }

    BigDecimal importe = pedirImporte();
    if (importe == null)
      return;

    if (!ConfigHTTP.esCliente()) {
      clave = pedirClaveCuenta("Clave de la cuenta");
      if (clave == null)
        return;
    }

    if (!MessagesUtils.confirm("¿Retirar " + Formato.importe(importe, "USD") + " de la cuenta " + cuenta + "?")) {
      MessagesUtils.printMessage("Operacion cancelada");
      return;
    }

    MessagesUtils.printMessage("Procesando...");
    printResponse(CONTROLLER.retiro(cuenta, importe, clave));
  }

  private static void transferencia() {
    MessagesUtils.printTitle("TRANSFERENCIA");

    String cuentaOrigen;
    String clave = "";

    if (ConfigHTTP.esCliente()) {
      cuentaOrigen = ConfigHTTP.getCuenta();
      mostrarCuentaPropia();
    } else {
      cuentaOrigen = pedirCuenta("Cuenta de origen");
      if (cuentaOrigen == null)
        return;
    }

    String cuentaDestino = pedirCuenta("Cuenta de destino");
    if (cuentaDestino == null)
      return;

    if (cuentaDestino.equals(cuentaOrigen)) {
      MessagesUtils.printError(ValidadorDatos.CUENTAS_IGUALES);
      return;
    }

    BigDecimal importe = pedirImporte();
    if (importe == null)
      return;

    if (!ConfigHTTP.esCliente()) {
      clave = pedirClaveCuenta("Clave de la cuenta de origen");
      if (clave == null)
        return;
    }

    if (!MessagesUtils.confirm("¿Transferir " + Formato.importe(importe, "USD") + " de la cuenta " + cuentaOrigen
        + " a la cuenta " + cuentaDestino + "?")) {
      MessagesUtils.printMessage("Operacion cancelada");
      return;
    }

    MessagesUtils.printMessage("Procesando...");
    printResponse(CONTROLLER.transferencia(cuentaOrigen, cuentaDestino, importe, clave));
  }

  /** RNF-010: al terminar se muestra el número de movimiento y el nuevo saldo, o el mensaje del servidor. */
  private static void printResponse(MovimientoResponseDTO response) {
    MessagesUtils.printTitle("RESULTADO");

    if (MenuView.revisarSesion(response))
      return;

    if (response.getEstado() != EstadosEnum.OK) {
      MessagesUtils.printError(response.getMensaje());
      return;
    }

    MessagesUtils.printMessage(response.getMensaje());
    MessagesUtils.printMessage("N.º de movimiento: " + response.getNumeroMovimiento());
    MessagesUtils.printMessage("Nuevo saldo:       " + Formato.importe(response.getNuevoSaldo(), "USD"));
    MessagesUtils.printMessage("Fecha:             " + Formato.fecha(new Date()));
  }

  /** ECUD3 paso 2: el cliente ve el número y el saldo de su cuenta. */
  private static void mostrarCuentaPropia() {
    ConsultaResponseDTO cuenta = CONTROLLER.consultar(ConfigHTTP.getCuenta());

    String saldo = cuenta.getEstado() == EstadosEnum.OK
      ? Formato.importe(cuenta.getSaldo(), cuenta.getMoneda())
      : Formato.SIN_DATO;

    MessagesUtils.printMessage("Cuenta: " + ConfigHTTP.getCuenta() + "  Saldo: " + saldo);
  }

  private static String pedirCuenta(String etiqueta) {
    String cuenta = MessagesUtils.readText(etiqueta);
    String error = ValidadorDatos.errorCuenta(cuenta);

    if (error == null)
      return cuenta;

    MessagesUtils.printError(error);
    return null;
  }

  private static BigDecimal pedirImporte() {
    String texto = MessagesUtils.readText("Importe");
    String error = ValidadorDatos.errorImporte(texto);

    if (error == null)
      return ValidadorDatos.convertirImporte(texto);

    MessagesUtils.printError(error);
    return null;
  }

  private static String pedirClaveCuenta(String etiqueta) {
    String clave = MessagesUtils.readPassword(etiqueta);
    String error = ValidadorDatos.errorClaveCuenta(clave);

    if (error == null)
      return clave;

    MessagesUtils.printError(error);
    return null;
  }
}
