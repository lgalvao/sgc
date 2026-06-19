package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroInconsistenciaInterna;
import sgc.mapa.dto.AtividadeMapaDto;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaVisualizacaoResponse;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.OrganizacaoDtoMapper;
import sgc.subprocesso.model.Subprocesso;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Service especializado para visualização de mapas de competências.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class MapaVisualizacaoService {
    private final MapaRepo mapaRepo;
    private final CompetenciaRepo competenciaRepo;
    private final sgc.mapa.MapaDtoMapper mapaDtoMapper;
    private final OrganizacaoDtoMapper organizacaoDtoMapper;

    public MapaVisualizacaoResponse obterMapaParaVisualizacao(Subprocesso subprocesso) {
        return mapaRepo.buscarCompletoPorSubprocesso(subprocesso.getCodigo())
                .map(mapa -> montarRespostaComMapa(subprocesso, mapa))
                .orElseGet(() -> {
                    if (subprocesso.getSituacao().ehEtapaMapa()) {
                        throw new ErroInconsistenciaInterna(
                                "Subprocesso %s em etapa de mapa sem mapa vinculado para visualizacao"
                                        .formatted(subprocesso.getCodigo())
                        );
                    }
                    return criarRespostaVazia(subprocesso);
                });
    }

    private MapaVisualizacaoResponse montarRespostaComMapa(Subprocesso subprocesso, Mapa mapa) {
        List<CompetenciaMapaDto> competencias = competenciaRepo.findByMapa_Codigo(mapa.getCodigo()).stream()
                .map(mapaDtoMapper::paraCompetenciaMapaDto)
                .toList();
        Set<Long> codAtividadesComCompetencia = new HashSet<>();
        competencias.forEach(comp -> comp.atividades().stream()
                .map(AtividadeMapaDto::codigo)
                .filter(Objects::nonNull)
                .forEach(codAtividadesComCompetencia::add));

        Set<Atividade> atividadesMapa = mapa.getAtividades();
        List<AtividadeMapaDto> atividadesSemCompetencia = atividadesMapa.stream()
                .filter(a -> !codAtividadesComCompetencia.contains(a.getCodigo()))
                .map(mapaDtoMapper::paraAtividadeMapaDto)
                .toList();

        return MapaVisualizacaoResponse.builder()
                .unidade(organizacaoDtoMapper.paraUnidadeResumoObrigatoria(subprocesso.getUnidade()))
                .competencias(competencias)
                .atividadesSemCompetencia(atividadesSemCompetencia)
                .sugestoes(Objects.toString(mapa.getSugestoes(), ""))
                .build();
    }

    private MapaVisualizacaoResponse criarRespostaVazia(Subprocesso subprocesso) {
        return MapaVisualizacaoResponse.builder()
                .unidade(organizacaoDtoMapper.paraUnidadeResumoObrigatoria(subprocesso.getUnidade()))
                .competencias(List.of())
                .atividadesSemCompetencia(List.of())
                .build();
    }
}
