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
                /**
                 * Justificativa para os ajustes realizados (obrigatória).
                 */
                @NotBlank(message = "A justificativa é obrigatória") @SanitizarHtml String justificativa,

                /**
                 * Nova data limite para a próxima etapa (opcional).
                 */
                LocalDateTime dataLimiteEtapa2,

                /**
                 * Lista de competências com os ajustes feitos (opcional em alguns fluxos).
                 */
                @Valid List<CompetenciaAjusteDto> competencias) {

        /**
         * Construtor para compatibilidade com testes legados.
         */
        public SubmeterMapaAjustadoRequest(String justificativa, LocalDateTime dataLimiteEtapa2) {
                this(justificativa, dataLimiteEtapa2, Collections.emptyList());
        }
}
