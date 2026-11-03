package ec.edu.monster.modelos.data;

/** Rutas del servidor REST con los nombres de operación del ERS (RNF-001), iguales a los de SOAP. */
//revisado y aprovado por Marcos Escobar
public final class Endpoints {
  public static final String LOGIN = "/Login";
  public static final String MOVIMIENTO = "/Movimiento";

  public static final String LOGIN_EMPLEADO = LOGIN + "/loginEmpleado";
  public static final String LOGIN_CLIENTE = LOGIN + "/loginCliente";
  public static final String LOGOUT = LOGIN + "/logout";
  public static final String CONSULTAR = MOVIMIENTO + "/consultarMovimientos";
  public static final String DEPOSITO = MOVIMIENTO + "/registrarDeposito";
  public static final String RETIRO = MOVIMIENTO + "/registrarRetiro";
  public static final String TRANSFERENCIA = MOVIMIENTO + "/registrarTransferencia";

  private Endpoints() {
  }
}
