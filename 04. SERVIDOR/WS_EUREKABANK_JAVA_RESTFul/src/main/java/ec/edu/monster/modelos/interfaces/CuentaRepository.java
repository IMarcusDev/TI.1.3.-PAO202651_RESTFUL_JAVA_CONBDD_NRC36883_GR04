/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.modelos.interfaces;

import ec.edu.monster.modelos.data.Cuenta;
//revisado y aprovado por Marcos Escobar
import java.sql.Connection;

/**
 *
 * @author Mateo Sosa
 */
public interface CuentaRepository {
  public abstract Cuenta getCuenta(String id) throws Exception;
  //revisado y aprovado por Marcos Escobar
  // Lee la cuenta y bloquea su fila hasta que termine la transacción de la conexión.
  // Devuelve null si la cuenta no existe.
  public abstract Cuenta getCuentaParaActualizar(Connection connection, String id) throws Exception;
  //revisado y aprovado por Marcos Escobar
  public abstract Cuenta updateCuenta(Connection connection, Cuenta c) throws Exception;
}
