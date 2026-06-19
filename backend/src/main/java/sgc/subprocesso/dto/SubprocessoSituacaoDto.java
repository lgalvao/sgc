package sgc.subprocesso.dto;

import lombok.Builder;

/**
 * DTO que representa o status atual de um subprocesso.
 *
 * <p>Usado para retornar informações básicas de status sem precisar
 * carregar o processo ou subprocesso completo.
 */
@Builder
public record SubprocessoSituacaoDto(
        Long codigo,
        String situacao
) {
}
