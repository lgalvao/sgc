package sgc.mapa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.atividade.modelo.AtividadeRepo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapaVinculoService {

    private final CompetenciaAtividadeRepo repositorioCompetenciaAtividade;
    private final CompetenciaRepo repositorioCompetencia;
    private final AtividadeRepo atividadeRepo;

    /**
     * Sincroniza os vínculos entre uma competência e uma lista de atividades.
     * <p>
     * O método compara a lista de IDs de atividades fornecida com os vínculos
     * existentes para a competência e realiza as seguintes operações:
     * <ul>
     *     <li>Remove vínculos com atividades que não estão na nova lista.</li>
     *     <li>Cria novos vínculos para atividades que estão na nova lista mas não
     *         nos vínculos atuais.</li>
     * </ul>
     *
     * @param idCompetencia      O ID da competência a ser atualizada.
     * @param novosIdsAtividades A lista completa de IDs de atividades que devem
     *                           estar vinculadas à competência.
     */
    public void atualizarVinculosAtividades(Long idCompetencia, List<Long> novosIdsAtividades) {
        List<CompetenciaAtividade> vinculosAtuais = repositorioCompetenciaAtividade.findByCompetenciaCodigo(idCompetencia);
        Set<Long> idsAtuais = vinculosAtuais.stream()
                .map(v -> v.getId().getAtividadeCodigo())
                .collect(Collectors.toSet());

        Set<Long> novosIds = new HashSet<>(novosIdsAtividades);

        // Remover os que não estão na nova lista
        vinculosAtuais.stream()
                .filter(v -> !novosIds.contains(v.getId().getAtividadeCodigo()))
                .forEach(repositorioCompetenciaAtividade::delete);

        // Adicionar os que não estão na lista atual
        Competencia competencia = repositorioCompetencia.findById(idCompetencia).orElseThrow();
        novosIds.stream()
                .filter(id -> !idsAtuais.contains(id))
                .forEach(idAtividade -> atividadeRepo.findById(idAtividade).ifPresent(atividade -> {
                    CompetenciaAtividade.Id id = new CompetenciaAtividade.Id(idAtividade, idCompetencia);
                    CompetenciaAtividade vinculo = new CompetenciaAtividade(id, competencia, atividade);
                    repositorioCompetenciaAtividade.save(vinculo);
                }));

        log.debug("Atualizados {} vínculos para competência {}", novosIdsAtividades.size(), idCompetencia);
    }
}
