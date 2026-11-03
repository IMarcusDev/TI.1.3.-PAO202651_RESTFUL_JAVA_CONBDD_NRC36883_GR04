/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.controladores;

import ec.edu.monster.modelos.data.Perfil;
import ec.edu.monster.modelos.data.Usuario;
import ec.edu.monster.modelos.interfaces.LoginRepository;
import ec.edu.monster.modelos.repositorios.LoginImplRepository;
//revisado y aprovado por Marcos Escobar
import ec.edu.monster.modelos.data.Cuenta;
import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.interfaces.CuentaRepository;
import ec.edu.monster.modelos.repositorios.CuentaImplRepository;
import ec.edu.monster.servicios.HashService;
import java.sql.SQLException;

/**
 *
 * @author Mateo Sosa
 */
public class LoginController {
  private static final LoginRepository repository = new LoginImplRepository();
  //revisado y aprovado por Marcos Escobar
  private static final CuentaRepository cuentaRepository = new CuentaImplRepository();

  //revisado y aprovado por Marcos Escobar
  public static final String CREDENCIALES_INCORRECTAS = "Credenciales incorrectas";
  public static final String SERVICIO_NO_DISPONIBLE = "Servicio no disponible, intente más tarde";
  public static final String USUARIO_INACTIVO = "El usuario no se encuentra activo";
  public static final String CUENTA_INACTIVA = "La cuenta no se encuentra activa";
  public static final String ACTIVO = "ACTIVO";

  public static Usuario getUsuarioById(String id) throws Exception {
    return LoginController.repository.getUsuario(id);
  }

  public static Perfil getPerfil(String username) throws Exception {
    return LoginController.repository.getPerfil(username);
  }

  //revisado y aprovado por Marcos Escobar
  // Login general (operación login): usuario y clave de cualquier perfil.
  public static Usuario autenticar(String username, String password) throws Exception {
    Usuario u = verificarCredenciales(username, null, password);

    if (!ACTIVO.equalsIgnoreCase(u.getEstado()))
      throw new Exception(USUARIO_INACTIVO);

    return u;
  }

  //revisado y aprovado por Marcos Escobar
  // loginEmpleado: solo entra un usuario de tipo Empleado y activo.
  public static Usuario autenticarEmpleado(String usuario, String clave) throws Exception {
    Usuario u = verificarCredenciales(usuario, null, clave);

    if (!"Empleado".equals(u.getTipo()))
      throw new Exception(CREDENCIALES_INCORRECTAS);
    if (!ACTIVO.equalsIgnoreCase(u.getEstado()))
      throw new Exception(USUARIO_INACTIVO);

    return u;
  }

  //revisado y aprovado por Marcos Escobar
  // loginCliente: número de cuenta (que es el ID del perfil) y su clave; la cuenta debe estar ACTIVO.
  public static Usuario autenticarCliente(String cuenta, String clave) throws Exception {
    Usuario u = verificarCredenciales(null, cuenta, clave);
    Cuenta c;

    try {
      c = cuentaRepository.getCuenta(cuenta);
    } catch (SQLException e) {
      throw new Exception(SERVICIO_NO_DISPONIBLE);
    } catch (Exception e) {
      throw new Exception(CREDENCIALES_INCORRECTAS);
    }

    if (!ACTIVO.equalsIgnoreCase(c.getEstado()))
      throw new Exception(CUENTA_INACTIVA);

    return u;
  }

  //revisado y aprovado por Marcos Escobar
  public static String perfilDe(Usuario u) {
    return "Empleado".equals(u.getTipo()) ? Sesion.EMPLEADO : Sesion.CLIENTE;
  }

  //revisado y aprovado por Marcos Escobar
  // Busca el perfil por usuario o por ID y compara la clave. Cualquier dato que no
  // coincida da el mismo mensaje, para no revelar qué usuarios o cuentas existen.
  private static Usuario verificarCredenciales(String username, String id, String clave) throws Exception {
    Perfil p;
    Usuario u;

    try {
      p = username != null ? repository.getPerfil(username) : repository.getPerfilById(id);
      u = repository.getUsuario(p.getId());
    } catch (SQLException e) {
      throw new Exception(SERVICIO_NO_DISPONIBLE);
    } catch (Exception e) {
      throw new Exception(CREDENCIALES_INCORRECTAS);
    }

    if (clave == null || !HashService.verifyPassword(clave, p.getPasswordHash()))
      throw new Exception(CREDENCIALES_INCORRECTAS);

    return u;
  }
}
