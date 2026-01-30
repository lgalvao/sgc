package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import sgc.seguranca.sanitizacao.SanitizarHtml;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Request para submeter mapa ajustado (CDU-19 item 5 e CDU-16).
 * Usado pelo RUP para submeter o mapa com ajustes após receber
 * sugestões/devolução.
 */
@Builder
public record SubmeterMapaAjustadoRequest(
        @NotBlank(message = "A justificativa é obrigatória") @SanitizarHtml String justificativa,

                LocalDateTime dataLimiteEtapa2,

                @Valid List<CompetenciaAjusteDto> competencias) {

        /**
         * Construtor para compatibilidade com testes legados.
         */
        public SubmeterMapaAjustadoRequest(String justificativa, LocalDateTime dataLimiteEtapa2) {
                this(justificativa, dataLimiteEtapa2, Collections.emptyList());
        }
}
