/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.modelos.data;

/**
 *
 * @author Mateo Sosa
 */
public class Movimiento {
  private String id;
  private String tipo;
  private String cuentaOrigen;
  private String cuentaDestino;
  private double valor;

  public Movimiento(String id, String tipo, String cuentaOrigen, String cuentaDestino, double valor) {
    this.id = id;
    this.tipo = tipo;
    this.cuentaOrigen = cuentaOrigen;
    this.cuentaDestino = cuentaDestino;
    this.valor = valor;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTipo() {
    return tipo;
  }

  public void setTipo(String tipo) {
    this.tipo = tipo;
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
