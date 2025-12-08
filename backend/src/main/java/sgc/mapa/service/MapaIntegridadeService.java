package sgc.mapa.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapaIntegridadeService {
    private final AtividadeRepo atividadeRepo;
    private final CompetenciaRepo competenciaRepo;

    /**
     * Valida a integridade de um mapa, verificando se existem atividades ou competências órfãs.
     *
     * <p>Este método loga avisos (warnings) para:
     *
     * <ul>
     *   <li>Atividades que não estão vinculadas a nenhuma competência.
     *   <li>Competências que não estão vinculadas a nenhuma atividade.
     * </ul>
     *
     * <p>Nota: Esta é uma validação defensiva. Em operação normal, não deve haver atividades ou
     * competências órfãs se as camadas de negócio estiverem corretamente configuradas. Sirve como
     * proteção contra dados inconsistentes e para diagnosticar problemas.
     *
     * @param codMapa O código do mapa a ser validado.
     */
    public void validarIntegridadeMapa(Long codMapa) {
        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(codMapa);
        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(codMapa);

        for (Atividade atividade : atividades) {
            if (atividade.getCompetencias().isEmpty()) {
                log.warn(
                        "Atividade {} não vinculada a nenhuma competência no mapa {}",
                        atividade.getCodigo(),
                        codMapa);
            }
        }

        for (Competencia competencia : competencias) {
            if (competencia.getAtividades().isEmpty()) {
                log.warn(
                        "Competência {} sem atividades vinculadas no mapa {}",
                        competencia.getCodigo(),
                        codMapa);
            }
        }
    }
}
