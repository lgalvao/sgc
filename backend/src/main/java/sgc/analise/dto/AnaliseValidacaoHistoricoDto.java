package sgc.analise.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class AnaliseValidacaoHistoricoDto {
    LocalDateTime dataHora;
    String observacoes;
    TipoAcaoAnalise acao;
    String unidadeSigla;
    String analistaUsuarioTitulo;
    String motivo;
    TipoAnalise tipo;
}
