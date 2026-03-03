package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

@Builder
public record NotificacaoCommand(
        Subprocesso subprocesso,
        TipoTransicao tipoTransicao,
        Unidade unidadeOrigem,
        Unidade unidadeDestino,
        @Nullable String observacoes
) {
}
