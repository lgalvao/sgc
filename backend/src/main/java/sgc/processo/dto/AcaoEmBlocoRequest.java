package sgc.processo.dto;

import jakarta.validation.constraints.*;
import org.jspecify.annotations.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;

public record AcaoEmBlocoRequest(
        @NotEmpty(message = Mensagens.PELO_MENOS_UMA_UNIDADE)
        List<Long> unidadeCodigos,

        @NotNull(message = Mensagens.ACAO_DEVE_SER_INFORMADA)
        AcaoProcesso acao,

        @Nullable LocalDate dataLimite
) {
    public AcaoEmBlocoCommand paraCommand() {
        if (acao == AcaoProcesso.DISPONIBILIZAR) {
            return new DisponibilizarMapaEmBlocoCommand(unidadeCodigos, exigirDataLimiteDisponibilizacao());
        }
        return new ProcessarAnaliseEmBlocoCommand(unidadeCodigos, acao);
    }

    private LocalDate exigirDataLimiteDisponibilizacao() {
        if (dataLimite == null) {
            throw new ErroValidacao(Mensagens.DATA_LIMITE_OBRIGATORIA);
        }
        return dataLimite;
    }
}
