/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.servicios;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author Mateo Sosa
 */
public class TokenService {
  private static final Set<String> tokensActivos = new HashSet<>();
  
  static {
    tokensActivos.add("123");
  }

  public static String generateToken() {
    String token = UUID.randomUUID().toString();

    tokensActivos.add(token);

    return token;
  }

  public static boolean validateToken(String token) {
    return tokensActivos.contains(token);
  }
}
