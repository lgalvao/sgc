package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import sgc.comum.Mensagens;

import java.util.List;

/**
 * Request para salvar os ajustes feitos no mapa de competências.
 */
@Builder
public record SalvarAjustesRequest(
        @NotEmpty(message = Mensagens.LISTA_COMPETENCIAS_NAO_PODE_SER_VAZIA)
        List<CompetenciaAjusteDto> competencias) {
}
