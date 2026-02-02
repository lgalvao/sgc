package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para requisição de processamento de subprocessos em lote.
 */
@Builder
public record ProcessarEmBlocoRequest(
        @NotBlank(message = "A ação é obrigatória")
        String acao,

        @NotEmpty(message = "Pelo menos um subprocesso deve ser selecionado")
        List<Long> subprocessos,

        @Nullable LocalDate dataLimite) {
}
