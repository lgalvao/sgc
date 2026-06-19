package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import sgc.comum.Mensagens;

import java.time.LocalDate;
import java.util.List;

@Builder
public record ProcessarEmBlocoRequest(
        @NotEmpty(message = Mensagens.PELO_MENOS_UM_SUBPROCESSO) List<Long> subprocessos,
        LocalDate dataLimite) {
}
