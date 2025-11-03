package sgc.sgrh.dto;

/**
 * DTO para dados de servidor (usuário) retornados para o frontend
 */
public record ServidorDto(
    Long codigo,
    String nome,
    String tituloEleitoral,
    String email,
    Long unidadeCodigo
) {
}
