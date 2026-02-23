package sgc.analise.dto;

import jakarta.annotation.Nullable;
import lombok.Builder;
import org.hibernate.validator.constraints.br.TituloEleitoral;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;

import java.time.LocalDateTime;

@Builder
public record AnaliseHistoricoDto(
        TipoAnalise tipo,
        TipoAcaoAnalise acao,

        @TituloEleitoral
        String analistaUsuarioTitulo,

        String unidadeSigla,
        String unidadeNome,
        LocalDateTime dataHora,

        String motivo,
        String observacoes
) {
}
