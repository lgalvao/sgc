package sgc.auth.dto;

/**
 * DTO com informações básicas do servidor autenticado.
 */
public record ServidorDto(
    String titulo,
    String nome,
    String email,
    String ramal,
    String unidadeCodigo
) {}