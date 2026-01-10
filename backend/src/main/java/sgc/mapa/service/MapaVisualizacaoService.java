package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.mapa.dto.visualizacao.CompetenciaDto;
import sgc.mapa.dto.visualizacao.ConhecimentoDto;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.mapa.model.*;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service especializado para visualização de mapas de competências.
 * 
 * <p><b>IMPORTANTE:</b> Este serviço deve ser acessado APENAS via {@link MapaFacade}.
 * Controllers não devem injetar este serviço diretamente.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class MapaVisualizacaoService {
    private final SubprocessoService subprocessoService;
    private final CompetenciaRepo competenciaRepo;
    private final AtividadeRepo atividadeRepo;

    public MapaVisualizacaoDto obterMapaParaVisualizacao(Long codSubprocesso) {
        Subprocesso subprocesso = subprocessoService.buscarSubprocesso(codSubprocesso);
        Mapa mapa = subprocesso.getMapa();
        if (mapa == null) {
            throw new sgc.comum.erros.ErroEstadoImpossivel(
                    "Subprocesso %d existe mas não possui Mapa associado - violação de invariante"
                    .formatted(codSubprocesso));
        }

        Unidade unidade = subprocesso.getUnidade();
        var unidadeDto = MapaVisualizacaoDto.UnidadeDto.builder()
                .codigo(unidade.getCodigo())
                .sigla(unidade.getSigla())
                .nome(unidade.getNome())
                .build();

        List<Atividade> atividadesComConhecimentos =
                atividadeRepo.findByMapaCodigoWithConhecimentos(mapa.getCodigo());

        Map<Long, AtividadeDto> atividadeDtoMap = atividadesComConhecimentos.stream()
                .collect(Collectors.toMap(Atividade::getCodigo, this::mapAtividadeToDto));

        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(mapa.getCodigo());

        List<CompetenciaDto> competenciasDto = competencias.stream()
                .map(competencia -> {
                    List<AtividadeDto> atividadesDto =
                            competencia.getAtividades().stream()
                                    .map(a -> atividadeDtoMap.get(a.getCodigo()))
                                    .filter(Objects::nonNull)
                                    .toList();

                    return CompetenciaDto.builder()
                            .codigo(competencia.getCodigo())
                            .descricao(competencia.getDescricao())
                            .atividades(atividadesDto)
                            .build();
                }).toList();

        var atividadesComCompetenciaIds = competencias.stream()
                .flatMap(c -> c.getAtividades().stream())
                .map(Atividade::getCodigo)
                .collect(Collectors.toSet());

        // Buscar atividades sem competência (órfãs)
        List<AtividadeDto> atividadesSemCompetencia =
                atividadesComConhecimentos.stream()
                        .filter(a -> !atividadesComCompetenciaIds.contains(a.getCodigo()))
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
        List<Conhecimento> conhecimentos = atividade.getConhecimentos();

        List<ConhecimentoDto> conhecimentosDto = conhecimentos.stream()
                .map(c -> ConhecimentoDto.builder()
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
