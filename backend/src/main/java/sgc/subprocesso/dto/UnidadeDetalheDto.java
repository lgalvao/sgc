package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO simplificado de unidade para detalhes de subprocesso.
 */
@Getter
@Builder
public class UnidadeDetalheDto {
    private final Long codigo;
    private final String sigla;
    private final String nome;
}
