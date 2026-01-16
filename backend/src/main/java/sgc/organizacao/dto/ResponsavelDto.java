package sgc.organizacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO para dados de responsável (titular/substituto) de uma unidade.
 */
@Getter
@Builder
@AllArgsConstructor
public class ResponsavelDto {

    private final Long unidadeCodigo;
    private final String titularTitulo;
    private final String titularNome;
    private final String substitutoTitulo;
    private final String substitutoNome;
}
