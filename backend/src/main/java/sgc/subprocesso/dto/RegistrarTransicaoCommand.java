package sgc.subprocesso.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.TipoTransicao;
import sgc.subprocesso.model.Subprocesso;

/**
 * Comando para registro de transição simples.
 */
@Builder
public record RegistrarTransicaoCommand(
        Subprocesso sp,
        TipoTransicao tipo,
        Unidade origem,
        Unidade destino,
        Usuario usuario,
        @Nullable String observacoes
) {
}
