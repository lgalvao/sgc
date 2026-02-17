package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.jspecify.annotations.Nullable;
import sgc.seguranca.sanitizacao.SanitizarHtml;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Request para submeter o mapa com ajustes após receber sugestões/devolução.
 */
@Builder
public record SubmeterMapaAjustadoRequest(
        @NotBlank(message = "A justificativa é obrigatória")
        @Size(max = 500, message = "A justificativa deve ter no máximo 500 caracteres")
        @SanitizarHtml
        String justificativa,

        @Nullable
        LocalDateTime dataLimiteEtapa2,

        @Valid
        List<CompetenciaAjusteDto> competencias) {

    /**
     * Construtor para compatibilidade com testes legados.
     */
    // TODO remover sempre coisas legadas!
    public SubmeterMapaAjustadoRequest(String justificativa, LocalDateTime dataLimiteEtapa2) {
        this(justificativa, dataLimiteEtapa2, Collections.emptyList());
    }
}
