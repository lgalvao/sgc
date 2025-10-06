package sgc.sgrh.dto;

/**
 * DTO para dados de responsável (titular/substituto) de uma unidade.
 */
public record ResponsavelDto(
    Long unidadeCodigo,
    String titularTitulo,
    String titularNome,
    String substitutoTitulo,
    String substitutoNome
) {
}