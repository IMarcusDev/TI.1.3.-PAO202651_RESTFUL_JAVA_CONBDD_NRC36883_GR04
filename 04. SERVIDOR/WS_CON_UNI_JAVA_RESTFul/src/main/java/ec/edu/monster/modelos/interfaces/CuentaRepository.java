/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.modelos.interfaces;

import ec.edu.monster.modelos.data.Cuenta;

/**
 *
 * @author Mateo Sosa
 */
public interface CuentaRepository {
  public abstract Cuenta getCuenta(String id) throws Exception;
  public abstract Cuenta updateCuenta(Cuenta c) throws Exception;
}
