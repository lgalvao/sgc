package sgc.processo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import sgc.comum.Mensagens;
import sgc.comum.erros.ErroValidacao;
import sgc.processo.model.AcaoProcesso;

import java.time.LocalDate;
import java.util.List;

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
