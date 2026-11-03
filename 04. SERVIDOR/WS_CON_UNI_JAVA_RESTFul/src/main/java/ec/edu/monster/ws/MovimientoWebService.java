/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.ws;

import ec.edu.monster.controladores.MovimientoController;
import ec.edu.monster.modelos.data.Movimiento;
import ec.edu.monster.modelos.dto.MovimientoRequestDTO;
import ec.edu.monster.modelos.dto.MovimientoResponseDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.servicios.TokenService;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
//revisado y aprovado por Marcos Escobar
import ec.edu.monster.controladores.LoginController;
import ec.edu.monster.modelos.data.Cuenta;
import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.data.Usuario;
import ec.edu.monster.modelos.dto.ConsultaMovimientosResponseDTO;
import ec.edu.monster.modelos.dto.CuentaDTO;
import ec.edu.monster.modelos.dto.MovimientoDTO;
import ec.edu.monster.modelos.dto.OperacionRequestDTO;
import java.sql.SQLException;

/**
 *
 * @author Mateo Sosa
 */
@Path("/Movimiento")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MovimientoWebService {
  @POST
  @Path("/deposito")
  public Response deposito(MovimientoRequestDTO dto) {
    if (!TokenService.validateToken(dto.getToken())) {
      return Response.status(Response.Status.UNAUTHORIZED)
              .entity(new MovimientoResponseDTO(
                      EstadosEnum.ERROR, "Token inválido", 0
              )).build();
    }

    try {
      //revisado y aprovado por Marcos Escobar
      MovimientoController.autorizar(TokenService.getSesion(dto.getToken()), "DEPOSITO", dto.getCuentaOrigen());

      Movimiento movimiento = MovimientoController.registerDeposito(
              new Movimiento(
                      "0",
                      "DEPOSITO",
                      dto.getCuentaOrigen(),
                      dto.getCuentaDestino(),
                      dto.getValor()
              )
      );

      return Response.ok(new MovimientoResponseDTO(
              EstadosEnum.OK,
              "Depósito registrado exitosamente",
              Integer.parseInt(movimiento.getId())
      )).build();

    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
              .entity(new MovimientoResponseDTO(
                      EstadosEnum.ERROR,
                      e.getLocalizedMessage(),
                      0
              )).build();
    }
  }

  @POST
  @Path("/retiro")
  public Response retiro(MovimientoRequestDTO dto) {
    if (!TokenService.validateToken(dto.getToken())) {
      return Response.status(Response.Status.UNAUTHORIZED)
              .entity(new MovimientoResponseDTO(
                      EstadosEnum.ERROR, "Token inválido", 0
              )).build();
    }

    try {
      //revisado y aprovado por Marcos Escobar
      MovimientoController.autorizar(TokenService.getSesion(dto.getToken()), "RETIRO", dto.getCuentaOrigen());

      Movimiento movimiento = MovimientoController.registerRetiro(
              new Movimiento(
                      "0",
                      "RETIRO",
                      dto.getCuentaOrigen(),
                      dto.getCuentaDestino(),
                      dto.getValor()
              )
      );

      return Response.ok(new MovimientoResponseDTO(
              EstadosEnum.OK,
              "Retiro registrado exitosamente",
              Integer.parseInt(movimiento.getId())
      )).build();

    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
              .entity(new MovimientoResponseDTO(
                      EstadosEnum.ERROR,
                      e.getLocalizedMessage(),
                      0
              )).build();
    }
  }

  @POST
  @Path("/transferencia")
  public Response transferencia(MovimientoRequestDTO dto) {
    if (!TokenService.validateToken(dto.getToken())) {
      return Response.status(Response.Status.UNAUTHORIZED)
        .entity(new MovimientoResponseDTO(EstadosEnum.ERROR, "Token inválido", 0
              )).build();
    }

    try {
      //revisado y aprovado por Marcos Escobar
      MovimientoController.autorizar(TokenService.getSesion(dto.getToken()), "TRANSFERENCIA", dto.getCuentaOrigen());

      Movimiento movimiento = MovimientoController.registerTransferencia(
              new Movimiento(
                      "0",
                      "TRANSFERENCIA",
                      dto.getCuentaOrigen(),
                      dto.getCuentaDestino(),
                      dto.getValor()
              )
      );

      return Response.ok(new MovimientoResponseDTO(
              EstadosEnum.OK,
              "Transferencia registrada exitosamente",
              Integer.parseInt(movimiento.getId())
      )).build();

    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
              .entity(new MovimientoResponseDTO(
                      EstadosEnum.ERROR,
                      e.getLocalizedMessage(),
                      0
              )).build();
    }
  }

  //revisado y aprovado por Marcos Escobar
  @POST
  @Path("/consultarMovimientos")
  public Response consultarMovimientos(OperacionRequestDTO dto) {
    Sesion sesion = TokenService.getSesion(dto.getToken());
    if (sesion == null)
      return Response.status(Response.Status.UNAUTHORIZED)
        .entity(new ConsultaMovimientosResponseDTO(EstadosEnum.ERROR, MovimientoController.TOKEN_INVALIDO))
        .build();

    try {
      Cuenta datos = MovimientoController.getCuentaConsultable(sesion, dto.getCuenta());
      Usuario titular = LoginController.getUsuarioById(dto.getCuenta());

      ConsultaMovimientosResponseDTO respuesta = new ConsultaMovimientosResponseDTO(
        EstadosEnum.OK,
        "Consulta de movimientos exitosa"
      );
      respuesta.setCuenta(new CuentaDTO(datos.getId(), titular.getNombre(), datos.getSaldo(), datos.getEstado()));
      for (Movimiento m : MovimientoController.getMovimientosByUserId(dto.getCuenta()))
        respuesta.getMovimientos().add(MovimientoDTO.desde(m, dto.getCuenta()));

      return Response.ok(respuesta).build();
    } catch (SQLException e) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(new ConsultaMovimientosResponseDTO(EstadosEnum.ERROR, LoginController.SERVICIO_NO_DISPONIBLE))
        .build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(new ConsultaMovimientosResponseDTO(EstadosEnum.ERROR, e.getLocalizedMessage()))
        .build();
    }
  }

  //revisado y aprovado por Marcos Escobar
  @POST
  @Path("/registrarDeposito")
  public Response registrarDeposito(OperacionRequestDTO dto) {
    return registrar(dto, "DEPOSITO", dto.getCuenta(), null, "Depósito registrado correctamente");
  }

  //revisado y aprovado por Marcos Escobar
  @POST
  @Path("/registrarRetiro")
  public Response registrarRetiro(OperacionRequestDTO dto) {
    return registrar(dto, "RETIRO", dto.getCuenta(), null, "Retiro registrado correctamente");
  }

  //revisado y aprovado por Marcos Escobar
  @POST
  @Path("/registrarTransferencia")
  public Response registrarTransferencia(OperacionRequestDTO dto) {
    return registrar(
      dto, "TRANSFERENCIA", dto.getCuentaOrigen(), dto.getCuentaDestino(),
      "Transferencia registrada correctamente"
    );
  }

  //revisado y aprovado por Marcos Escobar
  private static Response registrar(
    OperacionRequestDTO dto,
    String tipo,
    String cuentaOrigen,
    String cuentaDestino,
    String mensajeOk
  ) {
    Sesion sesion = TokenService.getSesion(dto.getToken());
    if (sesion == null)
      return Response.status(Response.Status.UNAUTHORIZED)
        .entity(new MovimientoResponseDTO(EstadosEnum.ERROR, MovimientoController.TOKEN_INVALIDO, 0))
        .build();

    try {
      Movimiento movimiento = MovimientoController.registrar(
        sesion, tipo, cuentaOrigen, cuentaDestino, dto.getImporte(), dto.getClave()
      );
      int numero = Integer.parseInt(movimiento.getId());

      MovimientoResponseDTO respuesta = new MovimientoResponseDTO(EstadosEnum.OK, mensajeOk, numero);
      respuesta.setNumeroMovimiento(numero);
      respuesta.setNuevoSaldo(MovimientoDTO.monto(movimiento.getNuevoSaldo()));
      return Response.ok(respuesta).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(new MovimientoResponseDTO(EstadosEnum.ERROR, e.getLocalizedMessage(), 0))
        .build();
    }
  }
}
