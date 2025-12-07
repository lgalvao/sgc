package sgc.mapa.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public class MapaVinculoService {
    private final CompetenciaRepo competenciaRepo;
    private final AtividadeRepo atividadeRepo;

    /**
     * Sincroniza os vínculos entre uma competência e uma lista de atividades.
     *
     * <p>O método compara a lista de IDs de atividades fornecida com os vínculos existentes para a
     * competência e realiza as seguintes operações:
     *
     * <ul>
     *   <li>Remove vínculos com atividades que não estão na nova lista.
     *   <li>Cria novos vínculos para atividades que estão na nova lista mas não nos vínculos
     *       atuais.
     * </ul>
     *
     * @param codCompetencia O código da competência a ser atualizada.
     * @param novosCodsAtividades A lista completa de IDs de atividades que devem estar vinculadas à
     *     competência.
     */
    public void atualizarVinculosAtividades(Long codCompetencia, List<Long> novosCodsAtividades) {
        Competencia competencia = competenciaRepo.findById(codCompetencia).orElseThrow();

        Set<Atividade> novasAtividades =
                new HashSet<>(atividadeRepo.findAllById(novosCodsAtividades));

        competencia.setAtividades(novasAtividades);
        competenciaRepo.save(competencia);

        log.debug(
                "Atualizados {} vínculos para competência {}",
                novosCodsAtividades.size(),
                codCompetencia);
    }
}
