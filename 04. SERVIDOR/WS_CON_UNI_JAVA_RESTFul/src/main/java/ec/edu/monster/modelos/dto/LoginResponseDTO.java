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
public class LoginResponseDTO extends ResponseDTO {
  private String token;
  private String nombre;
  private String estadoUsuario;
  private String tipoUsuario;
  
  public LoginResponseDTO(
    EstadosEnum estado,
    String mensaje,
    String token,
    String nombre,
    String estadoUsuario,
    String tipoUsuario
  ) {
    super(estado, mensaje);
    
    this.token = token;
    this.nombre = nombre;
    this.estadoUsuario = estadoUsuario;
    this.tipoUsuario = tipoUsuario;
  }
  
  public void setToken(String token) {
    this.token = token;
  }
  
  public String getToken() {
    return this.token;
  }

  public String getUsername() {
    return nombre;
  }

  public void setUsername(String nombre) {
    this.nombre = nombre;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getEstadoUsuario() {
    return estadoUsuario;
  }

  public void setEstadoUsuario(String estadoUsuario) {
    this.estadoUsuario = estadoUsuario;
  }

  public String getTipoUsuario() {
    return tipoUsuario;
  }

  public void setTipoUsuario(String tipoUsuario) {
    this.tipoUsuario = tipoUsuario;
  }
}
