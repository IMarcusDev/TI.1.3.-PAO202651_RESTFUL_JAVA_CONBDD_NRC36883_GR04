package ec.edu.monster.modelos.http;

/** Código HTTP y cuerpo de la respuesta (el servidor puede responder 400/401 con un cuerpo válido). */
//revisado y aprovado por Marcos Escobar
public record RespuestaHttp(int codigo, String cuerpo) {
}
