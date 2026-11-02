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
}
