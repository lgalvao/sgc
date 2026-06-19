package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.jspecify.annotations.Nullable;
import sgc.comum.Mensagens;
import sgc.seguranca.sanitizacao.SanitizarHtmlFormatado;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request para submeter o mapa com ajustes após receber sugestões/devolução.
 */
@Builder
public record SubmeterMapaAjustadoRequest(
        @NotBlank(message = Mensagens.JUSTIFICATIVA_OBRIGATORIA)
        @Size(max = 500, message = Mensagens.JUSTIFICATIVA_MAX)
        @SanitizarHtmlFormatado
        String justificativa,

        @Nullable
        LocalDateTime dataLimiteEtapa2,

        @Valid
        List<CompetenciaAjusteDto> competencias) {

}
