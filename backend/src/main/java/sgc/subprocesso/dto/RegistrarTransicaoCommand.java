package sgc.subprocesso.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoTransicao;

/**
 * Comando para registro de transição simples.
 */
@Builder
public record RegistrarTransicaoCommand(
        Subprocesso sp,
        TipoTransicao tipo,
        @Nullable Unidade origem,
        @Nullable Unidade destino,
        @Nullable Usuario usuario,
        @Nullable String observacoes
) {
}
