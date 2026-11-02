/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.modelos.repositorios;

import ec.edu.monster.config.DBConfig;
import ec.edu.monster.modelos.data.Perfil;
import ec.edu.monster.modelos.data.Usuario;
import ec.edu.monster.modelos.interfaces.LoginRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author Mateo Sosa
 */
public class LoginImplRepository implements LoginRepository {
  @Override
  public Perfil getPerfil(String username) throws Exception {
    String sql = """
      SELECT
        p.ID_PERFIL,
        p.USERNAME_PERFIL,
        p.PASSWORD_PERFIL
      FROM PERFILES p
      WHERE p.USERNAME_PERFIL = ?
    """;

    try (
      Connection connection = DBConfig.getConnection();
      PreparedStatement statement = connection.prepareStatement(sql)
    ) {
      statement.setString(1, username);

      try (ResultSet result = statement.executeQuery()) {
        if (!result.next()) {
          throw new Exception("Perfil no encontrado");
        }

        String passwordHash = result.getString("PASSWORD_PERFIL");

        return new Perfil(
          result.getString("ID_PERFIL"),
          username,
          passwordHash
        );
      }
    }
  }

  @Override
  public Usuario getUsuario(String id) throws Exception {
    String sql = """
      SELECT
        u.ID_USUARIO,
        CONCAT(u.NOMBRE_USUARIO, ' ', u.APELLIDO_USUARIO) AS NOMBRE_COMPLETO_USUARIO,
        u.ESTADO_USUARIO,
        t.NOMBRE_TIPO_USUARIO
      FROM USUARIOS u
      JOIN USUARIO_TIPOS t ON t.ID_TIPO_USUARIO = u.ID_TIPO_USUARIO
      WHERE u.ID_USUARIO = ?;
    """;

    try (
      Connection connection = DBConfig.getConnection();
      PreparedStatement statement = connection.prepareStatement(sql)
    ) {
      statement.setString(1, id);

      try (ResultSet result = statement.executeQuery()) {
        if (!result.next()) {
          throw new Exception("Usuario Inexistente");
        }

        return new Usuario(
          result.getString("ID_USUARIO"),
          result.getString("NOMBRE_COMPLETO_USUARIO"),
          result.getString("ESTADO_USUARIO"),
          result.getString("NOMBRE_TIPO_USUARIO")
        );
      }
    }
  }
}
