package sgc.comum;

import jakarta.validation.constraints.*;

import java.time.*;

public final class ComumDtos {

    private ComumDtos() {
    }

    public record TextoRequest(
            @NotBlank(message = MsgValidacao.CAMPO_TEXTO_OBRIGATORIO)
            String texto
    ) {
    }

    public record TextoOpcionalRequest(
            String texto
    ) {
    }

    public record JustificativaRequest(
            @NotBlank(message = MsgValidacao.JUSTIFICATIVA_OBRIGATORIA)
            String justificativa
    ) {
    }

    public record DataRequest(
            @NotNull(message = MsgValidacao.DATA_OBRIGATORIA)
            LocalDate data
    ) {
    }
}
