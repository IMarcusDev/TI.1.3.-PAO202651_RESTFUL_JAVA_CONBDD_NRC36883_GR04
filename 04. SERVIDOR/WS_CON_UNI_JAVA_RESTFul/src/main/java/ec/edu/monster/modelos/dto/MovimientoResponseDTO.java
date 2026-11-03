/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.enums.EstadosEnum;
//revisado y aprovado por Marcos Escobar
import java.math.BigDecimal;

/**
 *
 * @author Mateo Sosa
 */
public class MovimientoResponseDTO extends ResponseDTO {
  private int idMovimiento;
  //revisado y aprovado por Marcos Escobar
  private int numeroMovimiento;
  //revisado y aprovado por Marcos Escobar
  private BigDecimal nuevoSaldo;

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

  //revisado y aprovado por Marcos Escobar
  public int getNumeroMovimiento() {
    return numeroMovimiento;
  }

  //revisado y aprovado por Marcos Escobar
  public void setNumeroMovimiento(int numeroMovimiento) {
    this.numeroMovimiento = numeroMovimiento;
  }

  //revisado y aprovado por Marcos Escobar
  public BigDecimal getNuevoSaldo() {
    return nuevoSaldo;
  }

  //revisado y aprovado por Marcos Escobar
  public void setNuevoSaldo(BigDecimal nuevoSaldo) {
    this.nuevoSaldo = nuevoSaldo;
  }
}
