package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;
import sgc.atividade.dto.AtividadeDto;
import sgc.conhecimento.dto.ConhecimentoDto;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.util.HtmlUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO com os detalhes necessários para a tela de Detalhes do Subprocesso (CDU-07).
 */
@Getter
@Builder
public class SubprocessoDetalheDto {
    private final UnidadeDTO unidade;
    private final ResponsavelDTO responsavel;
    private final String situacao;
    private final String localizacaoAtual;
    private final LocalDate prazoEtapaAtual;
    private final List<MovimentacaoDto> movimentacoes;
    private final List<ElementoProcessoDTO> elementosDoProcesso;

    public static SubprocessoDetalheDto of(Subprocesso sp, List<Movimentacao> movimentacoes, List<AtividadeDto> atividades, List<ConhecimentoDto> conhecimentos, MovimentacaoMapper movimentacaoMapper) {
        UnidadeDTO unidadeDto = null;
        if (sp.getUnidade() != null) {
            unidadeDto = UnidadeDTO.builder()
                .codigo(sp.getUnidade().getCodigo())
                .sigla(HtmlUtils.escapeHtml(sp.getUnidade().getSigla()))
                .nome(HtmlUtils.escapeHtml(sp.getUnidade().getNome()))
                .build();
        }

        ResponsavelDTO responsavelDto = null;
        if (sp.getUnidade() != null && sp.getUnidade().getTitular() != null) {
            var titular = sp.getUnidade().getTitular();
            responsavelDto = ResponsavelDTO.builder()
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

        List<ElementoProcessoDTO> elementos = new ArrayList<>();
        if (atividades != null) {
            atividades.forEach(a -> elementos.add(new ElementoProcessoDTO("ATIVIDADE", a)));
        }
        if (conhecimentos != null) {
            conhecimentos.forEach(c -> elementos.add(new ElementoProcessoDTO("CONHECIMENTO", c)));
        }

        return SubprocessoDetalheDto.builder()
            .unidade(unidadeDto)
            .responsavel(responsavelDto)
            .situacao(sp.getSituacao().name())
            .localizacaoAtual(localizacaoAtual)
            .prazoEtapaAtual(prazoEtapaAtual)
            .movimentacoes(movimentacoesDto)
            .elementosDoProcesso(elementos)
            .build();
    }

    @Getter
    @Builder
    public static class UnidadeDTO {
        private final Long codigo;
        private final String sigla;
        private final String nome;
    }

    @Getter
    @Builder
    public static class ResponsavelDTO {
        private final Long id;
        private final String nome;
        private final String tipoResponsabilidade;
        private final String ramal;
        private final String email;
    }

    public record ElementoProcessoDTO(
            String tipo,
            Object payload
    ) {
    }
}