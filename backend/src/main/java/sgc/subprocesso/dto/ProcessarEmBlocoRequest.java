package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.jspecify.annotations.*;
import sgc.comum.*;

import java.time.*;
import java.util.*;

@Builder
public record ProcessarEmBlocoRequest(
        @NotEmpty(message = Mensagens.PELO_MENOS_UM_SUBPROCESSO)
        List<Long> subprocessos,

        @Nullable
        LocalDate dataLimite) {
}
