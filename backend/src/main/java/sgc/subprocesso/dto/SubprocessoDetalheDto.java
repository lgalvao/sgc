package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO com os detalhes necessários para a tela de Detalhes do Subprocesso (CDU-07).
 */
@Getter
@Builder
public class SubprocessoDetalheDto {
    private final Long codigo;
    private final UnidadeDetalheDto unidade;
    private final ResponsavelDetalheDto titular;
    private final ResponsavelDetalheDto responsavel;
    private final String situacao;
    private final String situacaoLabel;
    private final String localizacaoAtual;
    private final String processoDescricao;
    private final String tipoProcesso;
    private final LocalDateTime prazoEtapaAtual;
    private final boolean isEmAndamento;
    private final Integer etapaAtual;
    private final List<MovimentacaoDto> movimentacoes;
    private final SubprocessoPermissoesDto permissoes;

}
