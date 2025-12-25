package sgc.mapa.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.api.model.Atividade;
import sgc.atividade.api.model.Conhecimento;
import sgc.atividade.api.model.ConhecimentoRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.internal.mapper.visualizacao.AtividadeDto;
import sgc.mapa.internal.mapper.visualizacao.CompetenciaDto;
import sgc.mapa.internal.mapper.visualizacao.ConhecimentoDto;
import sgc.mapa.internal.mapper.visualizacao.MapaVisualizacaoDto;
import sgc.mapa.api.model.Competencia;
import sgc.mapa.api.model.CompetenciaRepo;
import sgc.mapa.api.model.Mapa;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SubprocessoRepo;
import sgc.unidade.api.model.Unidade;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class MapaVisualizacaoService {
    private final SubprocessoRepo subprocessoRepo;
    private final CompetenciaRepo competenciaRepo;
    private final ConhecimentoRepo conhecimentoRepo;
    private final sgc.atividade.api.model.AtividadeRepo atividadeRepo;

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

        List<Competencia> competencias =
                competenciaRepo.findByMapaCodigo(mapa.getCodigo());

        List<CompetenciaDto> competenciasDto =
                competencias.stream()
                        .map(
                                competencia -> {
                                    List<Atividade> atividades =
                                            competencia.getAtividades().stream().toList();

                                    List<AtividadeDto> atividadesDto =
                                            atividades.stream()
                                                    .map(this::mapAtividadeToDto)
                                                    .toList();

                                    return CompetenciaDto.builder()
                                            .codigo(competencia.getCodigo())
                                            .descricao(competencia.getDescricao())
                                            .atividades(atividadesDto)
                                            .build();
                                })
                        .toList();

        // Buscar atividades sem competência (orphaned)
        List<Atividade> todasAtividades =
                atividadeRepo.findByMapaCodigo(mapa.getCodigo());
        List<AtividadeDto> atividadesSemCompetencia =
                todasAtividades.stream()
                        .filter(a -> a.getCompetencias().isEmpty())
                        .map(this::mapAtividadeToDto)
                        .toList();

        return MapaVisualizacaoDto.builder()
                .unidade(unidadeDto)
                .competencias(competenciasDto)
                .atividadesSemCompetencia(atividadesSemCompetencia)
                .sugestoes(mapa.getSugestoes())
                .build();
    }

    private AtividadeDto mapAtividadeToDto(Atividade atividade) {
        List<Conhecimento> conhecimentos =
                conhecimentoRepo.findByAtividadeCodigo(atividade.getCodigo());
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
