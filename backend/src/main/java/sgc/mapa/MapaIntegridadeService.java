package sgc.mapa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapaIntegridadeService {

    private final AtividadeRepo atividadeRepo;
    private final CompetenciaRepo competenciaRepo;
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;
    private final MapaService mapaService;

    @Transactional(readOnly = true)
    public void validarMapaCompleto(Long idMapa) {
        log.debug("Validando integridade do mapa: idMapa={}", idMapa);

        MapaCompletoDto mapa = mapaService.obterMapaCompleto(idMapa);

        for (CompetenciaMapaDto comp : mapa.competencias()) {
            if (comp.atividadesCodigos().isEmpty()) {
                throw new IllegalStateException("A competência '%s' não possui atividades vinculadas".formatted(comp.descricao()));
            }
        }

        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(idMapa);

        for (Atividade atividade : atividades) {
            boolean temVinculo = competenciaAtividadeRepo.existsByAtividadeCodigo(atividade.getCodigo());
            if (!temVinculo) {
                throw new IllegalStateException("A atividade '%s' não está vinculada a nenhuma competência".formatted(atividade.getDescricao()));
            }
        }
        log.debug("Mapa {} validado com sucesso", idMapa);
    }

    public void validarIntegridadeMapa(Long idMapa) {
        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(idMapa);
        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(idMapa);

        for (Atividade atividade : atividades) {
            if (!competenciaAtividadeRepo.existsByAtividadeCodigo(atividade.getCodigo())) {
                log.warn("Atividade {} não vinculada a nenhuma competência no mapa {}", atividade.getCodigo(), idMapa);
            }
        }

        for (Competencia competencia : competencias) {
            if (competenciaAtividadeRepo.findByCompetenciaCodigo(competencia.getCodigo()).isEmpty()) {
                log.warn("Competência {} sem atividades vinculadas no mapa {}", competencia.getCodigo(), idMapa);
            }
        }
    }
}
