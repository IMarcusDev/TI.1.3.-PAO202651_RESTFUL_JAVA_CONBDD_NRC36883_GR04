package ec.edu.monster.modelos.dto;

/**
 * Outcome of a call to the RESTful server, as seen by the controllers.
 * SESION_EXPIRADA is kept apart from ERROR so the web session can be closed.
 */
public enum EstadosEnum {
  OK,
  ERROR,
  SESION_EXPIRADA
}
