package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO de responsável para detalhes de subprocesso.
 */
@Getter
@Builder
public class ResponsavelDetalheDto {
    private final Long codigo;
    private final String nome;
    private final String tipoResponsabilidade;
    private final String ramal;
    private final String email;
}
