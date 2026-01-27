package sgc.organizacao.dto;

import lombok.Builder;

/**
 * DTO para dados de responsável (titular/substituto) de uma unidade.
 */
@Builder
public record ResponsavelDto(
                Long unidadeCodigo,
                String titularTitulo,
                String titularNome,
                String substitutoTitulo,
                String substitutoNome) {
}
