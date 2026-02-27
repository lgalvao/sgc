package sgc.comum;

import jakarta.validation.constraints.*;

import java.time.*;

public final class ComumDtos {

    public record TextoRequest(
        @NotBlank(message = "O campo texto é obrigatório")
        String texto
    ) {}

    public record TextoOpcionalRequest(
        String texto
    ) {}

    public record JustificativaRequest(
        @NotBlank(message = "A justificativa é obrigatória")
        String justificativa
    ) {}

    public record DataRequest(
        @NotNull(message = "A data é obrigatória")
        LocalDate data
    ) {}

    private ComumDtos() {}
}
