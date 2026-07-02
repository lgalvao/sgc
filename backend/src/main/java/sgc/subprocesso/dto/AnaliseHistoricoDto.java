package sgc.subprocesso.dto;

import lombok.*;
import org.hibernate.validator.constraints.br.*;
import org.jspecify.annotations.*;

import java.time.*;

@Builder
public record AnaliseHistoricoDto(
        String tipo,
        String acao,
        String acaoDescricao,

        @TituloEleitoral
        String analistaUsuarioTitulo,
        String usuarioNome,

        String unidadeSigla,
        String unidadeNome,
        LocalDateTime dataHora,

        @Nullable String motivo,
        @Nullable String observacoes
) {
}
