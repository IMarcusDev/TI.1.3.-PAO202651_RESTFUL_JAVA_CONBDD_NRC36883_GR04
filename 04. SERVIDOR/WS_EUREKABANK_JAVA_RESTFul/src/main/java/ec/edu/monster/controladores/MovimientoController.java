/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.controladores;

import ec.edu.monster.modelos.data.Cuenta;
import ec.edu.monster.modelos.data.Movimiento;
import ec.edu.monster.modelos.data.Usuario;
import ec.edu.monster.modelos.interfaces.CuentaRepository;
import ec.edu.monster.modelos.interfaces.LoginRepository;
import ec.edu.monster.modelos.interfaces.MovimientoRepository;
import ec.edu.monster.modelos.repositorios.CuentaImplRepository;
import ec.edu.monster.modelos.repositorios.LoginImplRepository;
import ec.edu.monster.modelos.repositorios.MovimientoImplRepository;
import java.util.List;
//revisado y aprovado por Marcos Escobar
import ec.edu.monster.config.DBConfig;
import ec.edu.monster.modelos.data.Perfil;
import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.servicios.HashService;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Mateo Sosa
 */
public class MovimientoController {
  private static final MovimientoRepository repository = new MovimientoImplRepository();
  private static final CuentaRepository cuentaRepo = new CuentaImplRepository();
  private static final LoginRepository loginRepo = new LoginImplRepository();

  //revisado y aprovado por Marcos Escobar
  public static final String TOKEN_INVALIDO = "Token inválido o sesión expirada";
  public static final String NO_PERMITIDA = "Operación no permitida para su perfil";
  public static final String CUENTA_NO_EXISTE = "La cuenta no existe";
  public static final String CUENTA_INACTIVA = "La cuenta no se encuentra activa";
  public static final String DESTINO_NO_EXISTE = "La cuenta destino no existe";
  public static final String DESTINO_INACTIVO = "La cuenta destino no se encuentra activa";
  public static final String CUENTAS_IGUALES = "La cuenta de origen y la de destino deben ser diferentes";
  public static final String CLAVE_INCORRECTA = "Clave de cuenta incorrecta";
  public static final String IMPORTE_INVALIDO = "El importe debe ser mayor que cero y tener como máximo dos decimales";
  public static final String ERROR_REGISTRO = "No se pudo registrar la operación, intente nuevamente";

  public static List<Movimiento> getMovimientosByUserId(String id) throws Exception {
    return MovimientoController.repository.getMovimientosByUserId(id);
  }

  //revisado y aprovado por Marcos Escobar
  // Las validaciones originales se mantienen; la actualización del saldo y el registro
  // del movimiento ahora van en una sola transacción con la fila de la cuenta bloqueada.
  public static Movimiento registerDeposito(Movimiento m) throws Exception {
    if (!m.getTipo().equals("DEPOSITO"))
      throw new Exception("El movimiento no es depósito");
    if (m.getValor() <= 0)
      throw new Exception("El valor debe ser mayor a cero");

    // Perfil de Empleado
    Usuario empleado = loginRepo.getUsuario(m.getCuentaOrigen());
    if (!empleado.getTipo().equals("Empleado"))
      throw new Exception("Usuario no es empleado");

    return enTransaccion(connection -> {
      // Cuenta existente y activa
      Cuenta destino = bloquear(connection, m.getCuentaDestino(), CUENTA_NO_EXISTE, CUENTA_INACTIVA);
      destino.setSaldo(destino.getSaldo() + m.getValor());

      cuentaRepo.updateCuenta(connection, destino);

      Movimiento registrado = MovimientoController.repository.registerMovimiento(connection, m);
      registrado.setNuevoSaldo(destino.getSaldo());
      return registrado;
    });
  }

  //revisado y aprovado por Marcos Escobar
  public static Movimiento registerRetiro(Movimiento m) throws Exception {
    if (!m.getTipo().equals("RETIRO"))
      throw new Exception("El movimiento no es retiro");
    if (m.getValor() <= 0)
      throw new Exception("El valor debe ser mayor a cero");

    // Un retiro no tiene cuenta destino (la columna tiene clave foránea hacia CUENTAS).
    m.setCuentaDestino(null);

    return enTransaccion(connection -> {
      // Cuenta existente y activa
      Cuenta origen = bloquear(connection, m.getCuentaOrigen(), CUENTA_NO_EXISTE, CUENTA_INACTIVA);

      // Cuenta con plata (se revisa con la fila bloqueada)
      if (m.getValor() > origen.getSaldo())
        throw new Exception("Saldo insuficiente");

      origen.setSaldo(origen.getSaldo() - m.getValor());
      cuentaRepo.updateCuenta(connection, origen);

      Movimiento registrado = MovimientoController.repository.registerMovimiento(connection, m);
      registrado.setNuevoSaldo(origen.getSaldo());
      return registrado;
    });
  }

  //revisado y aprovado por Marcos Escobar
  public static Movimiento registerTransferencia(Movimiento m) throws Exception {
    if (!m.getTipo().equals("TRANSFERENCIA"))
      throw new Exception("El movimiento no es transferencia");
    if (m.getValor() <= 0)
      throw new Exception("El valor debe ser mayor a cero");
    if (m.getCuentaDestino() == null || m.getCuentaDestino().isBlank())
      throw new Exception(DESTINO_NO_EXISTE);
    if (m.getCuentaDestino().equals(m.getCuentaOrigen()))
      throw new Exception(CUENTAS_IGUALES);
    if (m.getCuentaOrigen() == null)
      throw new Exception(CUENTA_NO_EXISTE);

    return enTransaccion(connection -> {
      // Cuentas existentes y activas. Se bloquean en orden de número para que dos
      // transferencias cruzadas no se esperen mutuamente.
      Cuenta origen;
      Cuenta destino;
      if (m.getCuentaOrigen().compareTo(m.getCuentaDestino()) < 0) {
        origen = bloquear(connection, m.getCuentaOrigen(), CUENTA_NO_EXISTE, CUENTA_INACTIVA);
        destino = bloquear(connection, m.getCuentaDestino(), DESTINO_NO_EXISTE, DESTINO_INACTIVO);
      } else {
        destino = bloquear(connection, m.getCuentaDestino(), DESTINO_NO_EXISTE, DESTINO_INACTIVO);
        origen = bloquear(connection, m.getCuentaOrigen(), CUENTA_NO_EXISTE, CUENTA_INACTIVA);
      }

      // Cuenta origen con plata
      if (m.getValor() > origen.getSaldo())
        throw new Exception("Saldo insuficiente");

      origen.setSaldo(origen.getSaldo() - m.getValor());
      cuentaRepo.updateCuenta(connection, origen);

      destino.setSaldo(destino.getSaldo() + m.getValor());
      cuentaRepo.updateCuenta(connection, destino);

      Movimiento registrado = MovimientoController.repository.registerMovimiento(connection, m);
      registrado.setNuevoSaldo(origen.getSaldo());
      return registrado;
    });
  }

  //revisado y aprovado por Marcos Escobar
  // Operaciones del ERS (registrarDeposito, registrarRetiro, registrarTransferencia):
  // revisa el perfil, el importe y la clave de la cuenta, y registra con los métodos de arriba.
  public static Movimiento registrar(
    Sesion sesion,
    String tipo,
    String cuentaOrigen,
    String cuentaDestino,
    BigDecimal importe,
    String clave
  ) throws Exception {
    autorizar(sesion, tipo, cuentaOrigen);
    validarImporte(importe);

    // El empleado opera con la clave de la cuenta que le entrega el cliente.
    if (!sesion.esCliente() && !tipo.equals("DEPOSITO"))
      validarClaveCuenta(cuentaOrigen, clave);

    double valor = importe.doubleValue();

    return switch (tipo) {
      case "DEPOSITO" -> registerDeposito(new Movimiento("0", tipo, sesion.codigo(), cuentaOrigen, valor));
      case "RETIRO" -> registerRetiro(new Movimiento("0", tipo, cuentaOrigen, null, valor));
      default -> registerTransferencia(new Movimiento("0", tipo, cuentaOrigen, cuentaDestino, valor));
    };
  }

  //revisado y aprovado por Marcos Escobar
  // El depósito solo lo hace un empleado; el cliente solo opera sobre la cuenta de su sesión.
  public static void autorizar(Sesion sesion, String tipo, String cuentaOrigen) throws Exception {
    if (sesion == null)
      throw new Exception(TOKEN_INVALIDO);
    if (sesion.esCliente() && (tipo.equals("DEPOSITO") || !sesion.codigo().equals(cuentaOrigen)))
      throw new Exception(NO_PERMITIDA);
  }

  //revisado y aprovado por Marcos Escobar
  // Consulta: el empleado ve cualquier cuenta y el cliente solo la suya. No exige cuenta activa.
  public static Cuenta getCuentaConsultable(Sesion sesion, String cuenta) throws Exception {
    if (sesion.esCliente() && !sesion.codigo().equals(cuenta))
      throw new Exception(NO_PERMITIDA);

    try {
      return cuentaRepo.getCuenta(cuenta);
    } catch (SQLException e) {
      throw new Exception(LoginController.SERVICIO_NO_DISPONIBLE);
    } catch (Exception e) {
      throw new Exception(CUENTA_NO_EXISTE);
    }
  }

  //revisado y aprovado por Marcos Escobar
  public static void validarImporte(BigDecimal importe) throws Exception {
    if (importe == null
        || importe.signum() <= 0
        || importe.stripTrailingZeros().scale() > 2
        || importe.precision() - importe.scale() > 10)
      throw new Exception(IMPORTE_INVALIDO);
  }

  //revisado y aprovado por Marcos Escobar
  // La clave de la cuenta es la del perfil del titular (mismo ID que la cuenta).
  public static void validarClaveCuenta(String cuenta, String clave) throws Exception {
    Perfil perfil;

    try {
      perfil = loginRepo.getPerfilById(cuenta);
    } catch (SQLException e) {
      throw new Exception(LoginController.SERVICIO_NO_DISPONIBLE);
    } catch (Exception e) {
      throw new Exception(CUENTA_NO_EXISTE);
    }

    if (clave == null || !HashService.verifyPassword(clave, perfil.getPasswordHash()))
      throw new Exception(CLAVE_INCORRECTA);
  }

  //revisado y aprovado por Marcos Escobar
  private interface Operacion {
    Movimiento ejecutar(Connection connection) throws Exception;
  }

  //revisado y aprovado por Marcos Escobar
  // Todo o nada: si algo falla se deshacen el saldo y el movimiento. Los errores de
  // base de datos no se muestran al usuario tal cual.
  private static Movimiento enTransaccion(Operacion operacion) throws Exception {
    try (Connection connection = DBConfig.getConnection()) {
      connection.setAutoCommit(false);
      try {
        Movimiento registrado = operacion.ejecutar(connection);
        connection.commit();
        return registrado;
      } catch (Exception e) {
        connection.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw new Exception(ERROR_REGISTRO);
    }
  }

  //revisado y aprovado por Marcos Escobar
  private static Cuenta bloquear(Connection connection, String id, String noExiste, String inactiva) throws Exception {
    Cuenta cuenta = id == null ? null : cuentaRepo.getCuentaParaActualizar(connection, id);

    if (cuenta == null)
      throw new Exception(noExiste);
    if (!LoginController.ACTIVO.equalsIgnoreCase(cuenta.getEstado()))
      throw new Exception(inactiva);

    return cuenta;
  }
}
