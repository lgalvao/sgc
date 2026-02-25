package sgc.subprocesso.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
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
        @Nullable Unidade unidadeOrigemTransicao,
        @Nullable Unidade unidadeDestinoTransicao,
        @Nullable Usuario usuario,
        @Nullable String motivoAnalise,
        @Nullable String observacoes
) {
}
