package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;

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
     * @param codCompetencia      O código da competência a ser atualizada.
     * @param novosCodsAtividades A lista completa de IDs de atividades que devem
     *                           estar vinculadas à competência.
     */
    public void atualizarVinculosAtividades(Long codCompetencia, List<Long> novosCodsAtividades) {
        List<CompetenciaAtividade> vinculosAtuais = repositorioCompetenciaAtividade.findByCompetencia_Codigo(codCompetencia);
        Set<Long> idsAtuais = vinculosAtuais.stream()
                .map(v -> v.getId().getCodAtividade())
                .collect(Collectors.toSet());

        Set<Long> novosIds = new HashSet<>(novosCodsAtividades);

        // Remover os que não estão na nova lista
        vinculosAtuais.stream()
                .filter(v -> !novosIds.contains(v.getId().getCodAtividade()))
                .forEach(repositorioCompetenciaAtividade::delete);

        // Adicionar os que não estão na lista atual
        Competencia competencia = repositorioCompetencia.findById(codCompetencia).orElseThrow();
        novosIds.stream()
                .filter(id -> !idsAtuais.contains(id))
                .forEach(codAtividade -> atividadeRepo.findById(codAtividade).ifPresent(atividade -> {
                    CompetenciaAtividade.Id id = new CompetenciaAtividade.Id(codAtividade, codCompetencia);
                    CompetenciaAtividade vinculo = new CompetenciaAtividade(id, competencia, atividade);
                    repositorioCompetenciaAtividade.save(vinculo);
                }));

        log.debug("Atualizados {} vínculos para competência {}", novosCodsAtividades.size(), codCompetencia);
    }
}
