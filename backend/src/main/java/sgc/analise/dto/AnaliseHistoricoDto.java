package sgc.analise.dto;

import lombok.Builder;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;

import java.time.LocalDateTime;

@Builder
public record AnaliseHistoricoDto(
        LocalDateTime dataHora,
        String observacoes,
        TipoAcaoAnalise acao,
        String unidadeSigla,
        String analistaUsuarioTitulo,
        String motivo,
        TipoAnalise tipo) {
}
