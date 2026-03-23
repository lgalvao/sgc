package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;

import java.time.*;
import java.util.*;

@Builder
public record ProcessarEmBlocoRequest(
        @NotBlank(message = SgcMensagens.ACAO_OBRIGATORIA)
        String acao,

        @NotEmpty(message = SgcMensagens.PELO_MENOS_UM_SUBPROCESSO)
        List<Long> subprocessos,

        LocalDate dataLimite) {
}
