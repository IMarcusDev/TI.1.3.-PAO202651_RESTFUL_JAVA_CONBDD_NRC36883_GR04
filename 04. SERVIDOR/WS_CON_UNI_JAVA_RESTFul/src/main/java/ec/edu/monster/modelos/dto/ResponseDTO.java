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
public abstract class ResponseDTO {
  private EstadosEnum estado;
  private String mensaje;
  
  public ResponseDTO(EstadosEnum estado, String mensaje) {
    this.estado = estado;
    this.mensaje = mensaje;
  }
  
  public void setEstado(EstadosEnum estado) {
    this.estado = estado;
  }
  
  public EstadosEnum getEstado() {
    return this.estado;
  }
  
  public void setMensaje(String mensaje) {
    this.mensaje = mensaje;
  }
  
  public String getMensaje() {
    return this.mensaje;
  }
}
