package sgc.subprocesso.dto;

import lombok.Builder;
import org.hibernate.validator.constraints.br.TituloEleitoral;
import sgc.subprocesso.model.TipoAcaoAnalise;
import sgc.subprocesso.model.TipoAnalise;

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
