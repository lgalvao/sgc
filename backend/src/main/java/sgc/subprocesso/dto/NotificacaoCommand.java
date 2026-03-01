package sgc.subprocesso.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoTransicao;

@Builder
public record NotificacaoCommand(
        Subprocesso subprocesso,
        TipoTransicao tipoTransicao,
        Unidade unidadeOrigem,
        Unidade unidadeDestino,
        @Nullable String observacoes
) {
}
