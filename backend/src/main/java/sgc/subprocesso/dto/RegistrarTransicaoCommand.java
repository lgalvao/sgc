package sgc.subprocesso.dto;

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
        @org.jspecify.annotations.Nullable Unidade origem,
        @org.jspecify.annotations.Nullable Unidade destino,
        @org.jspecify.annotations.Nullable Usuario usuario,
        @org.jspecify.annotations.Nullable String observacoes
) {
}
