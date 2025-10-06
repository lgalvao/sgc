package sgc.sgrh.dto;

/**
 * DTO para perfil de usuário em uma unidade.
 */
public record PerfilDto(
    String usuarioTitulo,
    Long unidadeCodigo,
    String unidadeNome,
    String perfil  // ADMIN, GESTOR, CHEFE, SERVIDOR
) {
}