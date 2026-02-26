package sgc.mapa.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.model.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

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
        Set<Long> codAtividadesComCompetencia = new HashSet<>();
        competencias.forEach(comp -> comp.getAtividades().stream()
                .map(EntidadeBase::getCodigo)
                .forEach(codAtividadesComCompetencia::add));

        List<Atividade> atividadesSemCompetencia = mapa.getAtividades().stream()
                .filter(a -> !codAtividadesComCompetencia.contains(a.getCodigo()))
                .toList();

        return MapaVisualizacaoResponse.builder()
                .unidade(subprocesso.getUnidade())
                .competencias(competencias)
                .atividadesSemCompetencia(atividadesSemCompetencia)
                .sugestoes(mapa.getSugestoes())
                .build();
    }
}
