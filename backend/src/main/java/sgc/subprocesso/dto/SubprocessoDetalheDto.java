package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;
import sgc.atividade.dto.AtividadeDto;
import sgc.atividade.dto.ConhecimentoDto;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;
import sgc.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO com os detalhes necessários para a tela de Detalhes do Subprocesso (CDU-07).
 */
@Getter
@Builder
public class SubprocessoDetalheDto {
    private final UnidadeDto unidade;
    private final ResponsavelDto responsavel;
    private final String situacao;
    private final String localizacaoAtual;
    private final LocalDateTime prazoEtapaAtual;
    private final List<MovimentacaoDto> movimentacoes;
    private final List<ElementoProcessoDto> elementosProcesso;

    public static SubprocessoDetalheDto of(Subprocesso sp,
                                           List<Movimentacao> movimentacoes,
                                           List<AtividadeDto> atividades,
                                           List<ConhecimentoDto> conhecimentos,
                                           MovimentacaoMapper movimentacaoMapper) {
        UnidadeDto unidadeDto = null;
        if (sp.getUnidade() != null) {
            unidadeDto = UnidadeDto.builder()
                .codigo(sp.getUnidade().getCodigo())
                .sigla(HtmlUtils.escapeHtml(sp.getUnidade().getSigla()))
                .nome(HtmlUtils.escapeHtml(sp.getUnidade().getNome()))
                .build();
        }

        ResponsavelDto responsavelDto = null;
        if (sp.getUnidade() != null && sp.getUnidade().getTitular() != null) {
            var titular = sp.getUnidade().getTitular();
            responsavelDto = ResponsavelDto.builder()
                .nome(HtmlUtils.escapeHtml(titular.getNome()))
                .ramal(HtmlUtils.escapeHtml(titular.getRamal()))
                .email(HtmlUtils.escapeHtml(titular.getEmail()))
                .build();
        }

        String localizacaoAtual = null;
        if (movimentacoes != null && !movimentacoes.isEmpty()) {
            Movimentacao m = movimentacoes.getFirst();
            if (m.getUnidadeDestino() != null) {
                localizacaoAtual = HtmlUtils.escapeHtml(m.getUnidadeDestino().getSigla());
            }
        }

        var prazoEtapaAtual = sp.getDataLimiteEtapa1() != null ? sp.getDataLimiteEtapa1() : sp.getDataLimiteEtapa2();

        List<MovimentacaoDto> movimentacoesDto = new ArrayList<>();
        if (movimentacoes != null) {
            movimentacoesDto = movimentacoes.stream().map(movimentacaoMapper::toDTO).collect(Collectors.toList());
        }

        List<ElementoProcessoDto> elementos = new ArrayList<>();
        if (atividades != null) {
            atividades.forEach(a -> elementos.add(new ElementoProcessoDto("ATIVIDADE", a)));
        }
        if (conhecimentos != null) {
            conhecimentos.forEach(c -> elementos.add(new ElementoProcessoDto("CONHECIMENTO", c)));
        }

        return SubprocessoDetalheDto.builder()
            .unidade(unidadeDto)
            .responsavel(responsavelDto)
            .situacao(sp.getSituacao().name())
            .localizacaoAtual(localizacaoAtual)
            .prazoEtapaAtual(prazoEtapaAtual)
            .movimentacoes(movimentacoesDto)
            .elementosProcesso(elementos)
            .build();
    }

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

    public record ElementoProcessoDto(
            String tipo,
            Object payload
    ) {
    }
}