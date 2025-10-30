package sgc.analise.dto;

import lombok.Builder;
import sgc.analise.modelo.TipoAcaoAnalise;
import sgc.analise.modelo.TipoAnalise;

import lombok.Getter;

@Getter
@Builder
public class CriarAnaliseRequestDto {
    private final Long subprocessoCodigo;
    private final String observacoes;
    private final TipoAnalise tipo;
    private final TipoAcaoAnalise acao;
    private final String unidadeSigla;
    private final String analistaUsuarioTitulo;
    private final String motivo;
}