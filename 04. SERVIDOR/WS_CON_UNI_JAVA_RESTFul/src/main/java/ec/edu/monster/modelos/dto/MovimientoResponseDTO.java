/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.enums.EstadosEnum;

/**
 *
 * @author Mateo Sosa
 */
public class MovimientoResponseDTO extends ResponseDTO {
  private int idMovimiento;

  public MovimientoResponseDTO(EstadosEnum estado, String mensaje, int idMovimiento) {
    super(estado, mensaje);
    this.idMovimiento = idMovimiento;
  }

  public int getIdMovimiento() {
    return idMovimiento;
  }

  public void setIdMovimiento(int idMovimiento) {
    this.idMovimiento = idMovimiento;
  }
}
