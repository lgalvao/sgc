package sgc.mapa.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
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
        Mapa mapa = mapaRepo.buscarCompletoPorSubprocesso(subprocesso.getCodigo())
                .orElse(subprocesso.getMapa());

        if (mapa == null) {
            if (subprocesso.getSituacao() != null && subprocesso.getSituacao().name().contains("MAPA")) {
                throw new ErroInconsistenciaInterna(
                        "Subprocesso %s em etapa de mapa sem mapa vinculado para visualizacao"
                                .formatted(subprocesso.getCodigo())
                );
            }
            return criarRespostaVazia(subprocesso);
        }

        List<Competencia> competencias = competenciaRepo.findByMapa_Codigo(mapa.getCodigo());
        Set<Long> codAtividadesComCompetencia = new HashSet<>();
        competencias.forEach(comp -> comp.getAtividades().stream()
                .map(EntidadeBase::getCodigo)
                .forEach(codAtividadesComCompetencia::add));

        Set<Atividade> atividadesMapa = mapa.getAtividades();
        List<Atividade> atividadesSemCompetencia = atividadesMapa.stream()
                .filter(a -> !codAtividadesComCompetencia.contains(a.getCodigo()))
                .toList();

        return MapaVisualizacaoResponse.builder()
                .unidade(subprocesso.getUnidade())
                .competencias(competencias)
                .atividadesSemCompetencia(atividadesSemCompetencia)
                .sugestoes(Objects.toString(mapa.getSugestoes(), ""))
                .build();
    }

    private MapaVisualizacaoResponse criarRespostaVazia(Subprocesso subprocesso) {
        return MapaVisualizacaoResponse.builder()
                .unidade(subprocesso.getUnidade())
                .competencias(List.of())
                .atividadesSemCompetencia(List.of())
                .build();
    }
}
