package sgc.processo.dto;

import jakarta.validation.constraints.*;
import sgc.comum.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;

public record AcaoEmBlocoRequest(
        @NotEmpty(message = Mensagens.PELO_MENOS_UMA_UNIDADE)
        List<Long> unidadeCodigos,

        @NotNull(message = Mensagens.ACAO_DEVE_SER_INFORMADA)
        AcaoProcesso acao,

        LocalDate dataLimite
) {
}
