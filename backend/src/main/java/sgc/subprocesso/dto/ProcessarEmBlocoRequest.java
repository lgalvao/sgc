package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.*;
import java.util.*;

@Builder
public record ProcessarEmBlocoRequest(
        @NotBlank(message = "A ação é obrigatória")
        String acao,

        @NotEmpty(message = "Pelo menos um subprocesso deve ser selecionado")
        List<Long> subprocessos,

        LocalDate dataLimite) {
}
