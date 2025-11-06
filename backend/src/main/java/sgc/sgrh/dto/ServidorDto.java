package sgc.sgrh.dto;

/**
 * DTO para dados de um usuário (servidor) retornados para o frontend.
 * <p>
 * Usado no endpoint GET /api/unidades/{codigoUnidade}/servidores para retornar
 * a lista de usuários pertencentes a uma unidade específica.
 */
public record ServidorDto(
    Long codigo,
    String nome,
    String tituloEleitoral,
    String email,
    Long unidadeCodigo
) {
}
