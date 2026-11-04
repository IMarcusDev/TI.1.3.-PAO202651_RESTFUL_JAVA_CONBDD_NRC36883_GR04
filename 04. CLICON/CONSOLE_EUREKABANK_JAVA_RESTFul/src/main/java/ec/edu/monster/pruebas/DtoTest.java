package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.dto.ConsultaResponseDTO;
import ec.edu.monster.modelos.dto.MovimientoDTO;
import ec.edu.monster.modelos.dto.LoginResponseDTO;
import ec.edu.monster.modelos.dto.MovimientoResponseDTO;
import ec.edu.monster.modelos.dto.ResponseDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

//revisado y aprovado por Marcos Escobar
class DtoTest {
  @Test
  void estadoComoTextoJavaOComoNumeroNet() {
    assertEquals(EstadosEnum.OK, ResponseDTO.fromJSON("{\"estado\":\"OK\",\"mensaje\":\"x\"}").getEstado());
    assertEquals(EstadosEnum.ERROR, ResponseDTO.fromJSON("{\"estado\":\"ERROR\"}").getEstado());
    assertEquals(EstadosEnum.OK, ResponseDTO.fromJSON("{\"estado\":0,\"mensaje\":\"x\"}").getEstado());
    assertEquals(EstadosEnum.ERROR, ResponseDTO.fromJSON("{\"estado\":1,\"mensaje\":\"x\"}").getEstado());
  }

  @Test
  void tokenInvalidoEsSesionExpirada() {
    assertTrue(ResponseDTO.fromJSON("{\"estado\":\"ERROR\",\"mensaje\":\"Token inválido o sesión expirada\"}").sesionExpirada());
    assertFalse(ResponseDTO.fromJSON("{\"estado\":\"ERROR\",\"mensaje\":\"Saldo insuficiente\"}").sesionExpirada());
  }

  @Test
  void loginTraeTokenPerfilYNombre() {
    LoginResponseDTO dto = LoginResponseDTO.fromJSON(
      "{\"estado\":\"OK\",\"mensaje\":\"Ingreso de sesión exitoso\",\"token\":\"t-1\",\"perfil\":\"EMPLEADO\",\"nombre\":\"Carla Andrade\"}");
    assertEquals("t-1", dto.getToken());
    assertEquals("EMPLEADO", dto.getPerfil());
    assertEquals("Carla Andrade", dto.getNombre());
  }

  @Test
  void operacionTraeNumeroYNuevoSaldo() {
    MovimientoResponseDTO dto = MovimientoResponseDTO.fromJSON(
      "{\"estado\":\"OK\",\"mensaje\":\"ok\",\"idMovimiento\":23,\"numeroMovimiento\":23,\"nuevoSaldo\":475.00}");
    assertEquals(23, dto.getNumeroMovimiento());
    assertEquals(0, new BigDecimal("475").compareTo(dto.getNuevoSaldo()));
    MovimientoResponseDTO texto = MovimientoResponseDTO.fromJSON(
      "{\"estado\":0,\"mensaje\":\"ok\",\"numeroMovimiento\":\"7\",\"nuevoSaldo\":\"10.50\"}");
    assertEquals(7, texto.getNumeroMovimiento());
    assertEquals(new BigDecimal("10.50"), texto.getNuevoSaldo());
  }

  @Test
  void consultaConCabeceraYMovimientos() {
    ConsultaResponseDTO dto = ConsultaResponseDTO.fromJSON("""
      {"estado":"OK","mensaje":"Consulta de movimientos exitosa",
       "cuenta":{"numeroCuenta":"00100001","titular":"Ana Torres","moneda":"USD","saldo":575.00,"estadoCuenta":"ACTIVO"},
       "movimientos":[
         {"numeroMovimiento":23,"fecha":"2026-11-03T16:07:05","tipoMovimiento":"Transferencia","accion":"SALIDA","importe":25.00,"cuentaReferencia":"00100002"},
         {"numeroMovimiento":24,"fecha":"2026-11-03T16:09:00","tipoMovimiento":"Depósito","accion":"INGRESO","importe":100.00}]}
      """);
    assertEquals("00100001", dto.getNumeroCuenta());
    assertEquals("Ana Torres", dto.getTitular());
    assertEquals(0, new BigDecimal("575").compareTo(dto.getSaldo()));
    assertEquals("ACTIVO", dto.getEstadoCuenta());
    assertEquals(2, dto.getMovimientos().size());
    assertEquals("00100002", dto.getMovimientos().get(0).getCuentaReferencia());
    assertEquals("", dto.getMovimientos().get(1).getCuentaReferencia());
    assertFalse(dto.getMovimientos().get(0).esIngreso());
    assertTrue(dto.getMovimientos().get(1).esIngreso());
  }

  @Test
  void consultaSinMovimientosYErrorSinDatos() {
    assertEquals(0, ConsultaResponseDTO.fromJSON(
      "{\"estado\":\"OK\",\"mensaje\":\"ok\",\"cuenta\":{\"numeroCuenta\":\"00100002\"},\"movimientos\":[]}").getMovimientos().size());
    ConsultaResponseDTO error = ConsultaResponseDTO.fromJSON("{\"estado\":\"ERROR\",\"mensaje\":\"La cuenta no existe\"}");
    assertEquals(EstadosEnum.ERROR, error.getEstado());
    assertEquals("La cuenta no existe", error.getMensaje());
  }

  //revisado y aprovado por Marcos Escobar
  @Test
  void consultaMuestraPrimeroElUltimoMovimiento() {
    ConsultaResponseDTO dto = new ConsultaResponseDTO(EstadosEnum.OK, "ok");
    for (int n : new int[] { 3, 6, 10, 9 }) {
      dto.getMovimientos().add(new MovimientoDTO(n, "", "", "", BigDecimal.ONE, ""));
    }
    StringBuilder recientes = new StringBuilder();
    for (MovimientoDTO m : dto.getMovimientosRecientes()) {
      recientes.append(recientes.length() == 0 ? "" : ",").append(m.getNumeroMovimiento());
    }
    assertEquals("10,9,6,3", recientes.toString());
    assertEquals(3, dto.getMovimientos().get(0).getNumeroMovimiento());
  }
}
