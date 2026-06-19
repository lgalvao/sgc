package sgc.comum;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;
import sgc.seguranca.sanitizacao.SanitizarHtmlFormatado;

import java.time.LocalDate;

public final class ComumDtos {

    private ComumDtos() {
    }

    public record TextoRequest(
            @NotBlank(message = Mensagens.CAMPO_TEXTO_OBRIGATORIO)
            @Size(max = 500, message = Mensagens.OBSERVACOES_MAX_500)
            @SanitizarHtmlFormatado
            String texto
    ) {
    }

    public record TextoOpcionalRequest(
            @Size(max = 500, message = Mensagens.OBSERVACOES_MAX_500)
            @SanitizarHtmlFormatado
            @Nullable String texto
    ) {
    }

    public record JustificativaRequest(
            @NotBlank(message = Mensagens.JUSTIFICATIVA_OBRIGATORIA)
            @Size(max = 500, message = Mensagens.JUSTIFICATIVA_MAX)
            @SanitizarHtmlFormatado
            String justificativa
    ) {
    }

    public record DataRequest(
            @NotNull(message = Mensagens.DATA_OBRIGATORIA)
            @Future(message = Mensagens.DATA_LIMITE_FUTURA)
            LocalDate data
    ) {
    }
}
