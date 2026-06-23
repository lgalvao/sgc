package sgc.mapa.dto;

import lombok.*;
import sgc.subprocesso.dto.*;

/**
 * DTO para resultado de operação de conhecimento.
 */
@Builder
public record ResultadoOperacaoConhecimento(
        Long novoConhecimentoCodigo,
        AtividadeOperacaoResponse response) {
}
