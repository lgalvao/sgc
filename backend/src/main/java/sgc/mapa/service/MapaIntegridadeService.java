package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaAtividadeRepo;
import sgc.mapa.model.CompetenciaRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapaIntegridadeService {
    private final AtividadeRepo atividadeRepo;
    private final CompetenciaRepo competenciaRepo;
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;

    /**
     * Valida a integridade de um mapa, verificando se existem atividades ou competências órfãs.
     * <p>
     * Este método loga avisos (warnings) para:
     * <ul>
     *     <li>Atividades que não estão vinculadas a nenhuma competência.</li>
     *     <li>Competências que não estão vinculadas a nenhuma atividade.</li>
     * </ul>
     *
     * @param codMapa O código do mapa a ser validado.
     */
    // TODO essa validação está me parecendo inócua. Parece indicar partes ainda nao implementadas!
    public void validarIntegridadeMapa(Long codMapa) {
        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(codMapa);
        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(codMapa);

        for (Atividade atividade : atividades) {
            if (!competenciaAtividadeRepo.existsByAtividadeCodigo(atividade.getCodigo())) {
                log.warn("Atividade {} não vinculada a nenhuma competência no mapa {}", atividade.getCodigo(), codMapa);
            }
        }

        for (Competencia competencia : competencias) {
            if (competenciaAtividadeRepo.findByCompetenciaCodigo(competencia.getCodigo()).isEmpty()) {
                log.warn("Competência {} sem atividades vinculadas no mapa {}", competencia.getCodigo(), codMapa);
            }
        }
    }
}
