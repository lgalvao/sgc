package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** DTO com os detalhes necessários para a tela de Detalhes do Subprocesso (CDU-07). */
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

    public static SubprocessoDetalheDto of(
            Subprocesso sp,
            Usuario responsavel,
            List<Movimentacao> movimentacoes,
            MovimentacaoMapper movimentacaoMapper,
            SubprocessoPermissoesDto permissoes) {
        UnidadeDto unidadeDto = null;
        if (sp.getUnidade() != null) {
            unidadeDto =
                    UnidadeDto.builder()
                            .codigo(sp.getUnidade().getCodigo())
                            .sigla(sp.getUnidade().getSigla())
                            .nome(sp.getUnidade().getNome())
                            .build();
        }

        Usuario titular = (sp.getUnidade() != null) ? sp.getUnidade().getTitular() : null;
        ResponsavelDto responsavelDto = null;
        boolean isTitularResponsavel = false;

        if (responsavel != null) {
            String tipo = "Substituição"; // Default
            if (titular != null && titular.equals(responsavel)) {
                tipo = "Titular";
                isTitularResponsavel = true;
            }

            responsavelDto =
                    ResponsavelDto.builder()
                            .nome(responsavel.getNome())
                            .ramal(responsavel.getRamal())
                            .email(responsavel.getEmail())
                            .tipoResponsabilidade(tipo)
                            .build();
        }

        ResponsavelDto titularDto = null;
        // Titular só é exibido se não for o responsável
        if (titular != null && !isTitularResponsavel) {
            titularDto =
                    ResponsavelDto.builder()
                            .nome(titular.getNome())
                            .ramal(titular.getRamal())
                            .email(titular.getEmail())
                            .build();
        }

        String localizacaoAtual = null;
        if (movimentacoes != null && !movimentacoes.isEmpty()) {
            Movimentacao movimentacaoRecente = movimentacoes.getFirst();
            if (movimentacaoRecente.getUnidadeDestino() != null) {
                localizacaoAtual = movimentacaoRecente.getUnidadeDestino().getSigla();
            }
        }

        var prazoEtapaAtual =
                sp.getDataLimiteEtapa1() != null
                        ? sp.getDataLimiteEtapa1()
                        : sp.getDataLimiteEtapa2();

        List<MovimentacaoDto> movimentacoesDto = new ArrayList<>();
        if (movimentacoes != null) {
            movimentacoesDto =
                    movimentacoes.stream()
                            .map(movimentacaoMapper::toDTO)
                            .collect(Collectors.toList());
        }

        return SubprocessoDetalheDto.builder()
                .unidade(unidadeDto)
                .titular(titularDto)
                .responsavel(responsavelDto)
                .situacao(sp.getSituacao().name())
                .situacaoLabel(sp.getSituacao().getDescricao())
                .localizacaoAtual(localizacaoAtual)
                .processoDescricao(
                        sp.getProcesso() != null ? sp.getProcesso().getDescricao() : null)
                .tipoProcesso(sp.getProcesso() != null ? sp.getProcesso().getTipo().name() : null)
                .prazoEtapaAtual(prazoEtapaAtual)
                .isEmAndamento(sp.isEmAndamento())
                .etapaAtual(sp.getEtapaAtual())
                .movimentacoes(movimentacoesDto)
                .permissoes(permissoes)
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
}
