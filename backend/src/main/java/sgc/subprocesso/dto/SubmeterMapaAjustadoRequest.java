package sgc.subprocesso.dto;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.jspecify.annotations.*;
import sgc.seguranca.sanitizacao.*;

import java.time.*;
import java.util.*;

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

}
