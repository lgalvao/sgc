package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.MsgValidacao;

import java.time.*;
import java.util.*;

@Builder
public record ProcessarEmBlocoRequest(
        @NotBlank(message = MsgValidacao.ACAO_OBRIGATORIA)
        String acao,

        @NotEmpty(message = MsgValidacao.PELO_MENOS_UM_SUBPROCESSO)
        List<Long> subprocessos,

        LocalDate dataLimite) {
}
