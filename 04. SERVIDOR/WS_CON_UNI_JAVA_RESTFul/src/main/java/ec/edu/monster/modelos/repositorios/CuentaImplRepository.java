/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.modelos.repositorios;

import ec.edu.monster.config.DBConfig;
import ec.edu.monster.modelos.data.Cuenta;
import ec.edu.monster.modelos.data.Perfil;
import ec.edu.monster.modelos.interfaces.CuentaRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author Mateo Sosa
 */
public class CuentaImplRepository implements CuentaRepository {

  @Override
  public Cuenta getCuenta(String id) throws Exception {
    String sql = """
      SELECT
        c.ID_CUENTA,
        c.SALDO_CUENTA
      FROM CUENTAS c
      WHERE c.ID_CUENTA = ?;
    """;

    try (
      Connection connection = DBConfig.getConnection();
      PreparedStatement statement = connection.prepareStatement(sql)
    ) {
      statement.setString(1, id);

      try (ResultSet result = statement.executeQuery()) {
        if (!result.next()) {
          throw new Exception("Cuenta no encontrada");
        }

        return new Cuenta(
          result.getString("ID_CUENTA"),
          result.getDouble("SALDO_CUENTA")
        );
      }
    }
  }

  @Override
  public Cuenta updateCuenta(Cuenta c) throws Exception {
    String sql = """
      UPDATE CUENTAS
      SET SALDO_CUENTA = ?
      WHERE ID_CUENTA = ?
    """;

    try (
      Connection connection = DBConfig.getConnection();
      PreparedStatement statement = connection.prepareStatement(sql)
    ) {
      statement.setDouble(1, c.getSaldo());
      statement.setString(2, c.getId());

      int rowsAffected = statement.executeUpdate();

      if (rowsAffected == 0)
        throw new Exception("Cuenta no encontrada");

      return getCuenta(c.getId());
    } 
  }
}
