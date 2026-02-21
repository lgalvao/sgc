package sgc.subprocesso.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

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
