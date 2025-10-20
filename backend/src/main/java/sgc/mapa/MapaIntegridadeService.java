package sgc.mapa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;

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
     * @param idMapa O ID do mapa a ser validado.
     */
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
