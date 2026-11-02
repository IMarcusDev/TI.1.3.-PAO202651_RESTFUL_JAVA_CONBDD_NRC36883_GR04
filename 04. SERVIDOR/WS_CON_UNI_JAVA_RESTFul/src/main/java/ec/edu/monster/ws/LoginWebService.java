/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/WebServices/WebService.java to edit this template
 */
package ec.edu.monster.ws;

import ec.edu.monster.controladores.LoginController;
import ec.edu.monster.modelos.data.Perfil;
import ec.edu.monster.modelos.data.Usuario;
import ec.edu.monster.modelos.dto.LoginRequestDTO;
import ec.edu.monster.modelos.dto.LoginResponseDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.servicios.HashService;
import ec.edu.monster.servicios.TokenService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author Mateo Sosa
 */
@Path("/Login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoginWebService {

  @POST
  public Response login(LoginRequestDTO dto) {
    try {
      Perfil p = LoginController.getPerfil(dto.getUsername());
      Usuario u = LoginController.getUsuarioById(p.getId());

      if (!HashService.verifyPassword(
        dto.getPassword(),
        p.getPasswordHash()
      ))
        throw new Exception("Contraseña incorrecta");

      String token = TokenService.generateToken();

      return Response.ok(new LoginResponseDTO(
        EstadosEnum.OK,
        "Ingreso de Sesión Exitoso!",
        token,
        u.getNombre(),
        u.getEstado(),
        u.getTipo()
      )).build();

    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(new LoginResponseDTO(
          EstadosEnum.ERROR,
          e.getLocalizedMessage(),
          "",
          "",
          "INACTIVO",
          ""
        )).build();
    }
  }
}

