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

/**
 *
 * @author Mateo Sosa
 */
public class MovimientoController {
  private static final MovimientoRepository repository = new MovimientoImplRepository();
  private static final CuentaRepository cuentaRepo = new CuentaImplRepository();
  private static final LoginRepository loginRepo = new LoginImplRepository();
  
  public static List<Movimiento> getMovimientosByUserId(String id) throws Exception {
    return MovimientoController.repository.getMovimientosByUserId(id);
  }
  
  public static Movimiento registerDeposito(Movimiento m) throws Exception {
    if (!m.getTipo().equals("DEPOSITO"))
      throw new Exception("El movimiento no es depósito");
    if (m.getValor() <= 0)
      throw new Exception("El valor debe ser mayor a cero");
    
    // Perfil de Empleado
    Usuario empleado = loginRepo.getUsuario(m.getCuentaOrigen());
    if (!empleado.getTipo().equals("Empleado"))
      throw new Exception("Usuario no es empleado");
    
    // Cuenta existente
    Cuenta destino = cuentaRepo.getCuenta(m.getCuentaDestino());
    destino.setSaldo(destino.getSaldo() + m.getValor());
    
    cuentaRepo.updateCuenta(destino);
    
    return MovimientoController.repository.registerMovimiento(m);
  }
  
  public static Movimiento registerRetiro(Movimiento m) throws Exception {
    if (!m.getTipo().equals("RETIRO"))
      throw new Exception("El movimiento no es retiro");
    if (m.getValor() <= 0)
      throw new Exception("El valor debe ser mayor a cero");
    
    // Cuenta existente
    Cuenta origen = cuentaRepo.getCuenta(m.getCuentaOrigen());
    
    // Cuenta con plata
    if (m.getValor() > origen.getSaldo())
      throw new Exception("Saldo insuficiente");
    
    origen.setSaldo(origen.getSaldo() - m.getValor());
    cuentaRepo.updateCuenta(origen);
    
    return MovimientoController.repository.registerMovimiento(m);
  }
  
  public static Movimiento registerTransferencia(Movimiento m) throws Exception {
    if (!m.getTipo().equals("TRANSFERENCIA"))
      throw new Exception("El movimiento no es transferencia");
    if (m.getValor() <= 0)
      throw new Exception("El valor debe ser mayor a cero");
    
    // Cuentas existentes
    Cuenta origen = cuentaRepo.getCuenta(m.getCuentaOrigen());
    Cuenta destino = cuentaRepo.getCuenta(m.getCuentaDestino());
    
    // Cuenta origen con plata
    if (m.getValor() > origen.getSaldo())
      throw new Exception("Saldo insuficiente");
    
    origen.setSaldo(origen.getSaldo() - m.getValor());
    cuentaRepo.updateCuenta(origen);
    
    destino.setSaldo(destino.getSaldo() + m.getValor());
    cuentaRepo.updateCuenta(destino);
    
    return MovimientoController.repository.registerMovimiento(m);
  }
}
