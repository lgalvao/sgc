package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;

import java.util.*;

/**
 * Request para salvar os ajustes feitos no mapa de competências.
 */
@Builder
public record SalvarAjustesRequest(
        @NotEmpty(message = SgcMensagens.LISTA_COMPETENCIAS_NAO_PODE_SER_VAZIA)
        List<CompetenciaAjusteDto> competencias) {
}
