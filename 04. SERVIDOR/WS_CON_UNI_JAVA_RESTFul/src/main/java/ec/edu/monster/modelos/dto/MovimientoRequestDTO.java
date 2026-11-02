/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.modelos.dto;

/**
 *
 * @author Mateo Sosa
 */
public class MovimientoRequestDTO {
  private String token;
  private String cuentaOrigen;
  private String cuentaDestino;
  private double valor;

  public MovimientoRequestDTO() {
    this.token = "";
    this.cuentaOrigen = "";
    this.cuentaDestino = "";
    this.valor = 0;
  }

  public MovimientoRequestDTO(String token, String cuentaOrigen, String cuentaDestino, double valor) {
    this.token = token;
    this.cuentaOrigen = cuentaOrigen;
    this.cuentaDestino = cuentaDestino;
    this.valor = valor;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getCuentaOrigen() {
    return cuentaOrigen;
  }

  public void setCuentaOrigen(String cuentaOrigen) {
    this.cuentaOrigen = cuentaOrigen;
  }

  public String getCuentaDestino() {
    return cuentaDestino;
  }

  public void setCuentaDestino(String cuentaDestino) {
    this.cuentaDestino = cuentaDestino;
  }

  public double getValor() {
    return valor;
  }

  public void setValor(double valor) {
    this.valor = valor;
  }
}
