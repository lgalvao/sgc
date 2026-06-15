package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.diagnostico.model.*;
import sgc.relatorio.*;
import sgc.subprocesso.model.Subprocesso;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosticoRelatorioService {
    private final DiagnosticoRepo diagnosticoRepo;
    private final AvaliacaoServidorRepo avaliacaoServidorRepo;
    private final SituacaoCapacitacaoRepo situacaoCapacitacaoRepo;

    public RelatorioDiagnosticoGapDto criarRelatorioGapDiagnostico(Subprocesso subprocesso) {
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(subprocesso.getCodigo())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", subprocesso.getCodigo()));
        List<AvaliacaoServidor> avaliacoes = avaliacaoServidorRepo.listarPorDiagnostico(diagnostico.getCodigo());

        var avaliacoesPorCompetencia = avaliacoes.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        avaliacao -> avaliacao.getCompetencia().getCodigo(),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));

        List<RelatorioDiagnosticoGapCompetenciaDto> competencias = subprocesso.getMapa().getCompetencias().stream()
                .map(competencia -> {
                    List<AvaliacaoServidor> avaliacoesCompetencia = avaliacoesPorCompetencia.getOrDefault(competencia.getCodigo(), List.of());
                    java.util.IntSummaryStatistics estatisticas = avaliacoesCompetencia.stream()
                            .map(AvaliacaoServidor::getGap)
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .summaryStatistics();
                    Double mediaGap = estatisticas.getCount() == 0
                            ? null
                            : arredondarDuasCasas((double) estatisticas.getSum() / estatisticas.getCount());
                    return RelatorioDiagnosticoGapCompetenciaDto.builder()
                            .competenciaCodigo(competencia.getCodigo())
                            .competenciaDescricao(competencia.getDescricao())
                            .mediaGap(mediaGap)
                            .totalAvaliacoesConsideradas((int) estatisticas.getCount())
                            .build();
                })
                .toList();

        return RelatorioDiagnosticoGapDto.builder()
                .codigoUnidade(subprocesso.getUnidade().getCodigo())
                .siglaUnidade(subprocesso.getUnidade().getSigla())
                .nomeUnidade(subprocesso.getUnidade().getNome())
                .competencias(competencias)
                .build();
    }

    public RelatorioDiagnosticoSituacaoCapacitacaoDto criarRelatorioSituacaoCapacitacaoDiagnostico(Subprocesso subprocesso) {
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(subprocesso.getCodigo())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", subprocesso.getCodigo()));
        List<SituacaoCapacitacao> situacoes = situacaoCapacitacaoRepo.listarPorDiagnostico(diagnostico.getCodigo());

        var situacoesPorCompetencia = situacoes.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        situacao -> situacao.getCompetencia().getCodigo(),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));

        List<RelatorioDiagnosticoSituacaoCapacitacaoCompetenciaDto> competencias = subprocesso.getMapa().getCompetencias().stream()
                .map(competencia -> {
                    List<SituacaoCapacitacao> situacoesCompetencia = situacoesPorCompetencia.getOrDefault(competencia.getCodigo(), List.of());
                    return RelatorioDiagnosticoSituacaoCapacitacaoCompetenciaDto.builder()
                            .competenciaCodigo(competencia.getCodigo())
                            .competenciaDescricao(competencia.getDescricao())
                            .totalNaoSeAplica(contarSituacoes(situacoesCompetencia, ValorSituacaoCapacitacao.NA))
                            .totalACapacitar(contarSituacoes(situacoesCompetencia, ValorSituacaoCapacitacao.AC))
                            .totalEmCapacitacao(contarSituacoes(situacoesCompetencia, ValorSituacaoCapacitacao.EC))
                            .totalCapacitado(contarSituacoes(situacoesCompetencia, ValorSituacaoCapacitacao.C))
                            .totalInstrutor(contarSituacoes(situacoesCompetencia, ValorSituacaoCapacitacao.I))
                            .build();
                })
                .toList();

        return RelatorioDiagnosticoSituacaoCapacitacaoDto.builder()
                .codigoUnidade(subprocesso.getUnidade().getCodigo())
                .siglaUnidade(subprocesso.getUnidade().getSigla())
                .nomeUnidade(subprocesso.getUnidade().getNome())
                .competencias(competencias)
                .build();
    }

    private int contarSituacoes(List<SituacaoCapacitacao> situacoes, ValorSituacaoCapacitacao valor) {
        return (int) situacoes.stream()
                .filter(situacao -> situacao.getSituacaoCapacitacao() == valor)
                .count();
    }

    private double arredondarDuasCasas(double valor) {
        return Math.round(valor * 100.0d) / 100.0d;
    }
}
