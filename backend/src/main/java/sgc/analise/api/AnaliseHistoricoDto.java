package sgc.analise.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import sgc.analise.internal.model.TipoAcaoAnalise;
import sgc.analise.internal.model.TipoAnalise;

import java.time.LocalDateTime;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class AnaliseHistoricoDto {
    LocalDateTime dataHora;
    String observacoes;
    TipoAcaoAnalise acao;
    String unidadeSigla;
    String analistaUsuarioTitulo;
    String motivo;
    TipoAnalise tipo;
}
