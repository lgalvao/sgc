package sgc.organizacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para dados de responsável (titular/substituto) de uma unidade.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponsavelDto {
    private Long unidadeCodigo;
    private String titularTitulo;
    private String titularNome;
    private String substitutoTitulo;
    private String substitutoNome;
}
