/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/WebServices/WebService.java to edit this template
 */
package ec.edu.monster.ws;

import ec.edu.monster.controladores.LoginController;
import ec.edu.monster.modelos.data.Usuario;
import ec.edu.monster.modelos.dto.LoginRequestDTO;
import ec.edu.monster.modelos.dto.LoginResponseDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.servicios.TokenService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
//revisado y aprovado por Marcos Escobar
import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.dto.CredencialesRequestDTO;

/**
 *
 * @author Mateo Sosa
 */
@Path("/Login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoginWebService {

  //revisado y aprovado por Marcos Escobar
  // Ya no indica si falló el usuario o la clave, revisa que el usuario esté activo
  // y el token queda asociado al perfil y al código del usuario.
  @POST
  public Response login(LoginRequestDTO dto) {
    try {
      Usuario u = LoginController.autenticar(dto.getUsername(), dto.getPassword());

      String token = TokenService.generateToken(LoginController.perfilDe(u), u.getId());

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

  //revisado y aprovado por Marcos Escobar
  @POST
  @Path("/loginEmpleado")
  public Response loginEmpleado(CredencialesRequestDTO dto) {
    try {
      Usuario u = LoginController.autenticarEmpleado(dto.getUsuario(), dto.getClave());
      return sesionIniciada(u, Sesion.EMPLEADO, u.getId());
    } catch (Exception e) {
      return error(e.getLocalizedMessage());
    }
  }

  //revisado y aprovado por Marcos Escobar
  @POST
  @Path("/loginCliente")
  public Response loginCliente(CredencialesRequestDTO dto) {
    try {
      Usuario u = LoginController.autenticarCliente(dto.getCuenta(), dto.getClave());
      return sesionIniciada(u, Sesion.CLIENTE, dto.getCuenta());
    } catch (Exception e) {
      return error(e.getLocalizedMessage());
    }
  }

  //revisado y aprovado por Marcos Escobar
  // Elimina el token. Responde OK aunque ya no exista, porque la sesión queda cerrada igual.
  @POST
  @Path("/logout")
  public Response logout(CredencialesRequestDTO dto) {
    TokenService.removeToken(dto == null ? null : dto.getToken());
    return Response.ok(new LoginResponseDTO(EstadosEnum.OK, "Sesión cerrada", "", "", "", "")).build();
  }

  //revisado y aprovado por Marcos Escobar
  private static Response sesionIniciada(Usuario u, String perfil, String codigo) {
    LoginResponseDTO respuesta = new LoginResponseDTO(
      EstadosEnum.OK,
      "Ingreso de sesión exitoso",
      TokenService.generateToken(perfil, codigo),
      u.getNombre(),
      u.getEstado(),
      u.getTipo()
    );
    respuesta.setPerfil(perfil);
    return Response.ok(respuesta).build();
  }

  //revisado y aprovado por Marcos Escobar
  private static Response error(String mensaje) {
    return Response.status(Response.Status.BAD_REQUEST)
      .entity(new LoginResponseDTO(EstadosEnum.ERROR, mensaje, "", "", "INACTIVO", ""))
      .build();
  }
}

