package sgc.comum;

import jakarta.validation.constraints.*;

import java.time.*;

public final class ComumDtos {

    private ComumDtos() {
    }

    public record TextoRequest(
            @NotBlank(message = Mensagens.CAMPO_TEXTO_OBRIGATORIO)
            String texto
    ) {
    }

    public record TextoOpcionalRequest(
            String texto
    ) {
    }

    public record JustificativaRequest(
            @NotBlank(message = Mensagens.JUSTIFICATIVA_OBRIGATORIA)
            String justificativa
    ) {
    }

    public record DataRequest(
            @NotNull(message = Mensagens.DATA_OBRIGATORIA)
            LocalDate data
    ) {
    }
}
