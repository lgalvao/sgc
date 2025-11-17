package sgc.analise.dto;

import lombok.Builder;
import lombok.Value;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;

import java.time.LocalDateTime;

@Value
@Builder
public class AnaliseValidacaoHistoricoDto {
    LocalDateTime dataHora;
    String observacoes;
    TipoAcaoAnalise acao;
    String unidadeSigla;
    String analistaUsuarioTitulo;
    String motivo;
    TipoAnalise tipo;
}
