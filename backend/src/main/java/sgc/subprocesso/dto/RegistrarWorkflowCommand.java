package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

/**
 * Comando para registro de workflow completo (Análise + Transição).
 */
@Builder
public record RegistrarWorkflowCommand(
        Subprocesso sp,
        SituacaoSubprocesso novaSituacao,
        TipoTransicao tipoTransicao,
        TipoAnalise tipoAnalise,
        TipoAcaoAnalise tipoAcaoAnalise,
        Unidade unidadeAnalise,
        @org.jspecify.annotations.Nullable Unidade unidadeOrigemTransicao,
        @org.jspecify.annotations.Nullable Unidade unidadeDestinoTransicao,
        @org.jspecify.annotations.Nullable Usuario usuario,
        @Nullable String motivoAnalise,
        @Nullable String observacoes
) {
}
