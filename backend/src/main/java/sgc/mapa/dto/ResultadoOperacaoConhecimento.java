package sgc.mapa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;

/**
 * DTO composto para retornar o ID de um novo conhecimento junto com a resposta da operação.
 */
@Getter
@Builder
@AllArgsConstructor
public class ResultadoOperacaoConhecimento {
    private final Long novoConhecimentoId;
    private final AtividadeOperacaoResponse response;
}
