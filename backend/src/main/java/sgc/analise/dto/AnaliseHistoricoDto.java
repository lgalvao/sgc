package sgc.analise.dto;

import lombok.Builder;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;

import java.time.LocalDateTime;

import org.hibernate.validator.constraints.br.TituloEleitoral;

@Builder
public record AnaliseHistoricoDto(
        LocalDateTime dataHora,
        String dataHoraFormatada,
        String observacoes,
        TipoAcaoAnalise acao,
        String unidadeSigla,
        String unidadeNome,

        @TituloEleitoral
        String analistaUsuarioTitulo,

        String motivo,
        TipoAnalise tipo) {
}
