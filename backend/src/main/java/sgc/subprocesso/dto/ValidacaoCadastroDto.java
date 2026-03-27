package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;

import java.util.*;

/**
 * DTO de resposta para validação de cadastro de subprocesso.
 */
@Builder
public record ValidacaoCadastroDto(
        Boolean valido,
        List<Erro> erros
) {
    /**
     * DTO de erro de validação retornado na lista de erros.
     */
    @Builder
    public record Erro(
            String tipo,
            @Nullable Long atividadeCodigo,
            @Nullable String descricaoAtividade,
            String mensagem
    ) {
    }
}
