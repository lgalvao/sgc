package sgc.mapa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.subprocesso.dto.AtividadeOperacaoResp;

/**
 * DTO composto para retornar o ID de um novo conhecimento junto com a resposta da operação.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoOperacaoConhecimento {
    private Long novoConhecimentoId;
    private AtividadeOperacaoResp response;
}
