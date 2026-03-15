package sgc.subprocesso.dto;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.jspecify.annotations.*;
import sgc.comum.MsgValidacao;
import sgc.seguranca.sanitizacao.*;

import java.time.*;
import java.util.*;

/**
 * Request para submeter o mapa com ajustes após receber sugestões/devolução.
 */
@Builder
public record SubmeterMapaAjustadoRequest(
        @NotBlank(message = MsgValidacao.JUSTIFICATIVA_OBRIGATORIA)
        @Size(max = 500, message = MsgValidacao.JUSTIFICATIVA_MAX)
        @SanitizarHtml
        String justificativa,

        @Nullable
        LocalDateTime dataLimiteEtapa2,

        @Valid
        List<CompetenciaAjusteDto> competencias) {

}
