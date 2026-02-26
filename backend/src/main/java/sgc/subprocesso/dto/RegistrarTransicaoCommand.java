package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

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
