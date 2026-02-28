package sgc.processo.dto;

import jakarta.validation.constraints.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;

public record AcaoEmBlocoRequest(
        @NotEmpty(message = "Pelo menos uma unidade deve ser selecionada")
        List<Long> unidadeCodigos,

        @NotNull(message = "A ação deve ser informada")
        AcaoProcesso acao,

        LocalDate dataLimite
) {
}
