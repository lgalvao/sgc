package sgc.sgrh.dto;

/**
 * DTO para dados de servidor (usuário) retornados para o frontend
 */
// TODO esse dto deve ser removido, sendo usado apenas o UsuarioDto
public record ServidorDto(
    Long codigo,
    String nome,
    String tituloEleitoral,
    String email,
    Long unidadeCodigo
) {
}
