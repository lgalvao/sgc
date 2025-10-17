package sgc.sgrh.dto;

/**
 * DTO para dados de usuário do SGRH.
 */
public record UsuarioDto(
    String titulo,
    String nome,
    String email,
    String matricula,
    String cargo
) {
}