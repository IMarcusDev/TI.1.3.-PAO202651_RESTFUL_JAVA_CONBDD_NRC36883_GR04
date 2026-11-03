/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.servicios;

import ec.edu.monster.modelos.data.Sesion;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Mateo Sosa
 */
public class TokenService {
  //revisado y aprovado por Marcos Escobar
  // Cada token guarda de quién es (perfil y código). Ya no hay un token fijo
  // de pruebas: un token sin dueño no se puede autorizar.
  private static final Map<String, Sesion> tokensActivos = new ConcurrentHashMap<>();

  //revisado y aprovado por Marcos Escobar
  public static String generateToken(String perfil, String codigo) {
    String token = UUID.randomUUID().toString();

    tokensActivos.put(token, new Sesion(perfil, codigo));

    return token;
  }

  //revisado y aprovado por Marcos Escobar
  public static boolean validateToken(String token) {
    return getSesion(token) != null;
  }

  //revisado y aprovado por Marcos Escobar
  public static Sesion getSesion(String token) {
    return token == null ? null : tokensActivos.get(token);
  }

  //revisado y aprovado por Marcos Escobar
  public static void removeToken(String token) {
    if (token != null)
      tokensActivos.remove(token);
  }
}
