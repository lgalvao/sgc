package sgc.subprocesso.dto;

/**
 * DTO para retornar sugestões apresentadas ao mapa (CDU-20 item 5).
 */
public record SugestoesDto(
    String sugestoes,
    boolean sugestoesApresentadas,
    String unidadeNome
) {}