package sgc.analise.dto;

import lombok.Builder;
import lombok.Value;
import sgc.analise.modelo.TipoAcaoAnalise;
import sgc.analise.modelo.TipoAnalise;

import java.time.LocalDateTime;

@Value
@Builder
public class AnaliseHistoricoDto {
    LocalDateTime dataHora;
    String observacoes;
    TipoAcaoAnalise acao;
    String unidadeSigla;
    String analistaUsuarioTitulo;
    String motivo;
    TipoAnalise tipo;
}
