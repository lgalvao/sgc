package sgc.processo.dto;

import lombok.Builder;

import java.util.List;

/**
 * DTO de resposta contendo o contexto completo de um processo.
 */
@Builder
public record ProcessoContextoDto(
    ProcessoDetalheDto processo,
    List<SubprocessoElegivelDto> elegiveis
) {}
