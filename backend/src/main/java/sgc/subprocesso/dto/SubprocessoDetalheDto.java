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

    private final UnidadeDto unidade;
    private final ResponsavelDto titular;
    private final ResponsavelDto responsavel;
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

    @Getter
    @Builder
    public static class UnidadeDto {
        private final Long codigo;
        private final String sigla;
        private final String nome;
    }

    @Getter
    @Builder
    public static class ResponsavelDto {
        private final Long codigo;
        private final String nome;
        private final String tipoResponsabilidade;
        private final String ramal;
        private final String email;
    }
}
