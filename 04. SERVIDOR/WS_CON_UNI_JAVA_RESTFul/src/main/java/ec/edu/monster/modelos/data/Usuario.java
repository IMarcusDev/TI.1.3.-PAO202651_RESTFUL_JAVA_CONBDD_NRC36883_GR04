/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.modelos.data;

/**
 *
 * @author Mateo Sosa
 */
public class Usuario {
  private String id;
  private String nombre;
  private String estado;
  private String tipo;

  public Usuario(String id, String nombre, String estado, String tipo) {
    this.id = id;
    this.nombre = nombre;
    this.estado = estado;
    this.tipo = tipo;
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public String getTipo() {
    return tipo;
  }

  public void setTipo(String tipo) {
    this.tipo = tipo;
  }
}
