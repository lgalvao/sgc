package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;
import lombok.*;
import sgc.comum.*;

import java.time.*;
import java.util.*;

@Builder
public record ProcessarEmBlocoRequest(
        @NotBlank(message = Mensagens.ACAO_OBRIGATORIA)
        String acao,

        @NotEmpty(message = Mensagens.PELO_MENOS_UM_SUBPROCESSO)
        List<Long> subprocessos,

        @Nullable
        LocalDate dataLimite) {
}
