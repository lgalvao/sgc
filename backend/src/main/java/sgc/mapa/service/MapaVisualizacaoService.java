package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.dto.MapaVisualizacaoResponse;
import sgc.mapa.model.*;
import sgc.subprocesso.model.Subprocesso;

import java.util.HashSet;
import java.util.List;
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

    public MapaVisualizacaoResponse obterMapaParaVisualizacao(Subprocesso subprocesso) {
        Mapa mapa = mapaRepo.findFullBySubprocessoCodigo(subprocesso.getCodigo())
                .orElse(subprocesso.getMapa());

        if (mapa == null) {
            return MapaVisualizacaoResponse.builder()
                    .unidade(subprocesso.getUnidade())
                    .competencias(List.of())
                    .atividadesSemCompetencia(List.of())
                    .build();
        }

        List<Competencia> competencias = competenciaRepo.findByMapa_Codigo(mapa.getCodigo());
        
        Set<Long> atividadesComCompetenciaIds = new HashSet<>();
        for (Competencia comp : competencias) {
            for (Atividade ativ : comp.getAtividades()) {
                atividadesComCompetenciaIds.add(ativ.getCodigo());
            }
        }

        List<Atividade> atividadesSemCompetencia = mapa.getAtividades().stream()
                .filter(a -> !atividadesComCompetenciaIds.contains(a.getCodigo()))
                .toList();

        return MapaVisualizacaoResponse.builder()
                .unidade(subprocesso.getUnidade())
                .competencias(competencias)
                .atividadesSemCompetencia(atividadesSemCompetencia)
                .sugestoes(mapa.getSugestoes())
                .build();
    }
}
