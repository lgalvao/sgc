package sgc.subprocesso.dto;

import lombok.*;
import sgc.subprocesso.model.*;

/**
 * DTO que representa o status atual de um subprocesso.
 *
 * <p>Usado para retornar informações básicas de status sem precisar
 * carregar o processo ou subprocesso completo.
 */
@Builder
public record SubprocessoSituacaoDto(
        Long codigo,
        SituacaoSubprocesso situacao
) {
}
