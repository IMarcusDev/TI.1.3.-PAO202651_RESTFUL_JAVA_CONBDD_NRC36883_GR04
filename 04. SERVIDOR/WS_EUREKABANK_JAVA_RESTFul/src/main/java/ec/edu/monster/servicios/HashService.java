/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.servicios;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 *
 * @author Mateo Sosa
 */
public class HashService {
  public static String hashPassword(String password) {
    try {
      byte[] bytes = MessageDigest
        .getInstance("SHA-256")
        .digest(password.getBytes(StandardCharsets.UTF_8));

      return HexFormat.of().withUpperCase().formatHex(bytes);

    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 not available", e);
    }
  }

  public static boolean verifyPassword(String password, String hash) {
    try {
      byte[] passwordHash = HexFormat.of().parseHex(hash);
      byte[] calculatedHash = HexFormat.of().parseHex(hashPassword(password));

      return MessageDigest.isEqual(calculatedHash, passwordHash);

    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
