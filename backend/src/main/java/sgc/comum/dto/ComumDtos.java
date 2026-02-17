package sgc.comum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTOs genéricos para requisições de campo único, reduzindo boilerplate.
 */
public final class ComumDtos {

    public record TextoRequest(
        @NotBlank(message = "O campo texto é obrigatório")
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

    public record IdRequest(
        @NotNull(message = "O ID é obrigatório")
        Long id
    ) {}

    private ComumDtos() {}
}
