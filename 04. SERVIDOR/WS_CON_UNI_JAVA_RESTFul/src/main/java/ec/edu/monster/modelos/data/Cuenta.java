/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.modelos.data;

/**
 *
 * @author Mateo Sosa
 */
public class Cuenta {
  private String id;
  private double saldo;
  //revisado y aprovado por Marcos Escobar
  private String estado;

  public Cuenta(String id, double saldo) {
    this.id = id;
    this.saldo = saldo;
  }

  //revisado y aprovado por Marcos Escobar
  public Cuenta(String id, double saldo, String estado) {
    this(id, saldo);
    this.estado = estado;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

  //revisado y aprovado por Marcos Escobar
  public String getEstado() {
    return estado;
  }

  //revisado y aprovado por Marcos Escobar
  public void setEstado(String estado) {
    this.estado = estado;
  }
}
