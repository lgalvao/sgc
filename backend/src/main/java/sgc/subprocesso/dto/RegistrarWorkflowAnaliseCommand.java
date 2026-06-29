package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

@Builder
public record RegistrarWorkflowAnaliseCommand(
        Subprocesso sp,
        SituacaoSubprocesso novaSituacao,
        TipoTransicao tipoTransicao,
        TipoAnalise tipoAnalise,
        TipoAcaoAnalise tipoAcaoAnalise,
        @Nullable Unidade unidadeAnalise,
        @Nullable Unidade unidadeDestino,
        Usuario usuario,
        @Nullable String motivoAnalise,
        @Nullable String observacoes,
        ModoComunicacaoWorkflow modoComunicacao
) {

    public enum ModoComunicacaoWorkflow {
        PADRAO,
        SEM_EMAIL,
        SEM_COMUNICACOES
    }
}
