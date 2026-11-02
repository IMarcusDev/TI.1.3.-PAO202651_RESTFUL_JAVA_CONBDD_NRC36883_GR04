/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.controladores;

import ec.edu.monster.modelos.data.Perfil;
import ec.edu.monster.modelos.data.Usuario;
import ec.edu.monster.modelos.interfaces.LoginRepository;
import ec.edu.monster.modelos.repositorios.LoginImplRepository;

/**
 *
 * @author Mateo Sosa
 */
public class LoginController {
  private static final LoginRepository repository = new LoginImplRepository();
  
  public static Usuario getUsuarioById(String id) throws Exception {
    return LoginController.repository.getUsuario(id);
  }
  
  public static Perfil getPerfil(String username) throws Exception {
    return LoginController.repository.getPerfil(username);
  }
}
