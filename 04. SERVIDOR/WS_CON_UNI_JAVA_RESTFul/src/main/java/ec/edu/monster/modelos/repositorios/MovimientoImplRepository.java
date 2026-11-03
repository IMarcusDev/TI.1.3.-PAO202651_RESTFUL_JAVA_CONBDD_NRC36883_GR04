/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.modelos.repositorios;

import ec.edu.monster.config.DBConfig;
import ec.edu.monster.modelos.data.Movimiento;
import ec.edu.monster.modelos.data.Perfil;
import ec.edu.monster.modelos.interfaces.MovimientoRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//revisado y aprovado por Marcos Escobar
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mateo Sosa
 */
public class MovimientoImplRepository implements MovimientoRepository {
  //revisado y aprovado por Marcos Escobar
  // Movimientos donde la cuenta es origen o destino (una transferencia se guarda en
  // una sola fila y aparece en las dos cuentas), con su fecha y en orden de registro.
  @Override
  public List<Movimiento> getMovimientosByUserId(String id) throws Exception {
    String sql = """
      SELECT
        m.ID_MOVIMIENTO,
        mt.NOMBRE_TIPO_MOVIMIENTO,
        m.CUENTA_ORIGEN_MOVIMIENTO,
        m.CUENTA_DESTINO_MOVIMIENTO,
        m.FECHA_MOVIMIENTO,
        m.VALOR_MOVIMIENTO
      FROM MOVIMIENTOS m
      JOIN MOVIMIENTO_TIPOS mt on m.ID_TIPO_MOVIMIENTO = mt.ID_TIPO_MOVIMIENTO
      WHERE m.CUENTA_ORIGEN_MOVIMIENTO = ? OR m.CUENTA_DESTINO_MOVIMIENTO = ?
      ORDER BY m.ID_MOVIMIENTO;
    """;

    List<Movimiento> movimientos = new ArrayList<Movimiento>();

    try (
      Connection connection = DBConfig.getConnection();
      PreparedStatement statement = connection.prepareStatement(sql)
    ) {
      statement.setString(1, id);
      statement.setString(2, id);

      try (ResultSet result = statement.executeQuery()) {
        while (result.next()) {
          Movimiento movimiento = new Movimiento(
            result.getString("ID_MOVIMIENTO"),
            result.getString("NOMBRE_TIPO_MOVIMIENTO"),
            result.getString("CUENTA_ORIGEN_MOVIMIENTO"),
            result.getString("CUENTA_DESTINO_MOVIMIENTO"),
            result.getDouble("VALOR_MOVIMIENTO")
          );
          Timestamp fecha = result.getTimestamp("FECHA_MOVIMIENTO");
          if (fecha != null)
            movimiento.setFecha(fecha.toLocalDateTime());
          movimientos.add(movimiento);
        }
      }

      return movimientos;
    }
  }

  //revisado y aprovado por Marcos Escobar
  // Usa la conexión de la transacción en curso y guarda la fecha con la hora (NOW()).
  @Override
  public Movimiento registerMovimiento(Connection connection, Movimiento m) throws Exception {
    String sql = """
      INSERT INTO MOVIMIENTOS(
        ID_TIPO_MOVIMIENTO,
        CUENTA_ORIGEN_MOVIMIENTO,
        CUENTA_DESTINO_MOVIMIENTO,
        FECHA_MOVIMIENTO,
        VALOR_MOVIMIENTO)
      VALUES
        ((SELECT mt.ID_TIPO_MOVIMIENTO FROM MOVIMIENTO_TIPOS mt WHERE mt.NOMBRE_TIPO_MOVIMIENTO = ?),
        ?,
        ?,
        NOW(),
        ?);
    """;

    try (
      PreparedStatement statement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
    ) {
      statement.setString(1, m.getTipo().toUpperCase());
      statement.setString(2, m.getCuentaOrigen());
      statement.setString(3, m.getCuentaDestino());
      statement.setDouble(4, m.getValor());

      int rowsAffected = statement.executeUpdate();

      if (rowsAffected == 0) {
        throw new Exception("No se pudo registrar el movimiento");
      }

      try (ResultSet result = statement.getGeneratedKeys()) {
        if (result.next()) {
          m.setId(result.getString(1));
        }
      }

      return m;
    }
  }
}
