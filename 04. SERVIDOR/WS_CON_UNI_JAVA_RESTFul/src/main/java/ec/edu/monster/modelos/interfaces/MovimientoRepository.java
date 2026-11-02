/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.modelos.interfaces;

import ec.edu.monster.modelos.data.Movimiento;
import java.util.List;

/**
 *
 * @author Mateo Sosa
 */
public interface MovimientoRepository {
  public abstract List<Movimiento> getMovimientosByUserId(String id) throws Exception;
  public abstract Movimiento registerMovimiento(Movimiento m) throws Exception;
}
