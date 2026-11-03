/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.modelos.interfaces;

import ec.edu.monster.modelos.data.Movimiento;
import java.util.List;
//revisado y aprovado por Marcos Escobar
import java.sql.Connection;

/**
 *
 * @author Mateo Sosa
 */
public interface MovimientoRepository {
  public abstract List<Movimiento> getMovimientosByUserId(String id) throws Exception;
  //revisado y aprovado por Marcos Escobar
  public abstract Movimiento registerMovimiento(Connection connection, Movimiento m) throws Exception;
}
