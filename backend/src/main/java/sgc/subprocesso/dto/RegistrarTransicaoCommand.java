package sgc.subprocesso.dto;

import org.jspecify.annotations.Nullable;

import lombok.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

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
