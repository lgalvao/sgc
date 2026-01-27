package sgc.mapa.dto;

import lombok.Builder;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;

/**
 * DTO para resultado de operação de conhecimento.
 */
@Builder
public record ResultadoOperacaoConhecimento(
                Long novoConhecimentoId,
                AtividadeOperacaoResponse response) {
}
