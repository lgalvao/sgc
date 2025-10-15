package sgc.analise.dto;

import lombok.Builder;
import sgc.analise.modelo.TipoAcaoAnalise;
import sgc.analise.modelo.TipoAnalise;

@Builder
public record CriarAnaliseRequestDto(
    Long subprocessoCodigo,
    String observacoes,
    TipoAnalise tipo,
    TipoAcaoAnalise acao,
    String unidadeSigla,
    String analistaUsuarioTitulo,
    String motivo
) {}