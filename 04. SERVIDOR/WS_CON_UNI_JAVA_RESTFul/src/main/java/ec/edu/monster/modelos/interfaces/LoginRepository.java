/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.modelos.interfaces;

import ec.edu.monster.modelos.data.Perfil;
import ec.edu.monster.modelos.data.Usuario;

/**
 *
 * @author Mateo Sosa
 */
public interface LoginRepository {
  public abstract Perfil getPerfil(String username) throws Exception;
  public abstract Usuario getUsuario(String id) throws Exception;
}
