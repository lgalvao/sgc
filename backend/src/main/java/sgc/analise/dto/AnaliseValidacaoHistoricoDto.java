package sgc.analise.dto;

import lombok.Builder;
import org.hibernate.validator.constraints.br.TituloEleitoral;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;

import java.time.LocalDateTime;

@SuppressWarnings("unused")
@Builder
public record AnaliseValidacaoHistoricoDto(
        LocalDateTime dataHora,
        String observacoes,
        TipoAcaoAnalise acao,
        String unidadeSigla,

        @TituloEleitoral
        String analistaUsuarioTitulo,

        String motivo,
        TipoAnalise tipo) {
}
