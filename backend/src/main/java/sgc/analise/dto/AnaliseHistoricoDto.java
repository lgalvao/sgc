package sgc.analise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;

import java.time.LocalDateTime;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class AnaliseHistoricoDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    LocalDateTime dataHora;
    String observacoes;
    TipoAcaoAnalise acao;
    String unidadeSigla;
    String analistaUsuarioTitulo;
    String motivo;
    TipoAnalise tipo;
}
