package sgc.analise.dto;

import sgc.analise.modelo.TipoAcaoAnalise;
import sgc.analise.modelo.TipoAnalise;

import java.time.LocalDateTime;

public record AnaliseValidacaoHistoricoDto(
    LocalDateTime dataHora,
    String observacoes,
    TipoAcaoAnalise acao,
    String unidadeSigla,
    String analistaUsuarioTitulo,
    String motivo,
    TipoAnalise tipo
) {}
