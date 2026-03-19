package sgc.comum;

import jakarta.validation.constraints.*;

import java.time.*;

public final class ComumDtos {

    private ComumDtos() {
    }

    public record TextoRequest(
            @NotBlank(message = SgcMensagens.CAMPO_TEXTO_OBRIGATORIO)
            String texto
    ) {
    }

    public record TextoOpcionalRequest(
            String texto
    ) {
    }

    public record JustificativaRequest(
            @NotBlank(message = SgcMensagens.JUSTIFICATIVA_OBRIGATORIA)
            String justificativa
    ) {
    }

    public record DataRequest(
            @NotNull(message = SgcMensagens.DATA_OBRIGATORIA)
            LocalDate data
    ) {
    }
}
