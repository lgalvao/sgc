package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.ConhecimentoRepo;
import sgc.mapa.model.AtividadeRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.mapa.dto.visualizacao.CompetenciaDto;
import sgc.mapa.dto.visualizacao.ConhecimentoDto;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class MapaVisualizacaoService {
    private final SubprocessoRepo subprocessoRepo;
    private final CompetenciaRepo competenciaRepo;
    private final ConhecimentoRepo conhecimentoRepo;
    private final AtividadeRepo atividadeRepo;

    public MapaVisualizacaoDto obterMapaParaVisualizacao(Long codSubprocesso) {
        Subprocesso subprocesso =
                subprocessoRepo
                        .findById(codSubprocesso)
                        .orElseThrow(
                                () -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

        Mapa mapa = subprocesso.getMapa();
        if (mapa == null) {
            throw new ErroEntidadeNaoEncontrada(
                    "Subprocesso não possui mapa associado: ", codSubprocesso);
        }

        Unidade unidade = subprocesso.getUnidade();
        var unidadeDto =
                MapaVisualizacaoDto.UnidadeDto.builder()
                        .codigo(unidade.getCodigo())
                        .sigla(unidade.getSigla())
                        .nome(unidade.getNome())
                        .build();

        // 1. Fetch all activities with their knowledge efficiently
        List<Atividade> atividadesComConhecimentos =
                atividadeRepo.findByMapaCodigoWithConhecimentos(mapa.getCodigo());

        // 2. Map them to DTOs and index by ID for fast lookup
        Map<Long, AtividadeDto> atividadeDtoMap = atividadesComConhecimentos.stream()
                .collect(Collectors.toMap(
                        Atividade::getCodigo,
                        this::mapAtividadeToDto
                ));

        List<Competencia> competencias =
                competenciaRepo.findByMapaCodigo(mapa.getCodigo());

        List<CompetenciaDto> competenciasDto =
                competencias.stream()
                        .map(
                                competencia -> {
                                    // Competencia.atividades are loaded by findByMapaCodigo
                                    // but they are "shallow" (lazy knowledge).
                                    // Use the map to get the full DTO.
                                    List<AtividadeDto> atividadesDto =
                                            competencia.getAtividades().stream()
                                                    .map(a -> atividadeDtoMap.get(a.getCodigo()))
                                                    // Handle case where activity might not be in the map (shouldn't happen if consistent)
                                                    .filter(java.util.Objects::nonNull)
                                                    .toList();

                                    return CompetenciaDto.builder()
                                            .codigo(competencia.getCodigo())
                                            .descricao(competencia.getDescricao())
                                            .atividades(atividadesDto)
                                            .build();
                                })
                        .toList();

        // Buscar atividades sem competência (orphaned)
        List<AtividadeDto> atividadesSemCompetencia =
                atividadesComConhecimentos.stream()
                        .filter(a -> a.getCompetencias().isEmpty())
                        .map(a -> atividadeDtoMap.get(a.getCodigo()))
                        .toList();

        return MapaVisualizacaoDto.builder()
                .unidade(unidadeDto)
                .competencias(competenciasDto)
                .atividadesSemCompetencia(atividadesSemCompetencia)
                .sugestoes(mapa.getSugestoes())
                .build();
    }

    private AtividadeDto mapAtividadeToDto(Atividade atividade) {
        // Here atividade.getConhecimentos() is already fetched if passed from findByMapaCodigoWithConhecimentos
        List<Conhecimento> conhecimentos = atividade.getConhecimentos();

        List<ConhecimentoDto> conhecimentosDto =
                conhecimentos.stream()
                        .map(
                                c ->
                                        ConhecimentoDto.builder()
                                                .codigo(c.getCodigo())
                                                .descricao(c.getDescricao())
                                                .build())
                        .toList();
        return AtividadeDto.builder()
                .codigo(atividade.getCodigo())
                .descricao(atividade.getDescricao())
                .conhecimentos(conhecimentosDto)
                .build();
    }
}
