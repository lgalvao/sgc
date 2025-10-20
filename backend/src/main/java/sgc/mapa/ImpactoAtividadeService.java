package sgc.mapa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.TipoImpactoAtividade;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImpactoAtividadeService {

    private final ImpactoCompetenciaService impactoCompetenciaService;
    private final AtividadeRepo atividadeRepo;
    private final CompetenciaRepo repositorioCompetencia;
    private final CompetenciaAtividadeRepo repositorioCompetenciaAtividade;


    /**
     * Obtém todas as atividades associadas a um mapa, percorrendo as competências e seus vínculos.
     *
     * @param mapa O mapa do qual as atividades serão extraídas.
     * @return Uma {@link List} de {@link Atividade}s.
     */
    public List<Atividade> obterAtividadesDoMapa(Mapa mapa) {
        List<Competencia> competencias = repositorioCompetencia
                .findByMapaCodigo(mapa.getCodigo());

        Set<Long> idsAtividades = new HashSet<>();
        for (Competencia comp : competencias) {
            List<CompetenciaAtividade> vinculos = repositorioCompetenciaAtividade
                    .findByCompetenciaCodigo(comp.getCodigo());

            for (CompetenciaAtividade vinculo : vinculos) {
                idsAtividades.add(vinculo.getId().getAtividadeCodigo());
            }
        }

        if (idsAtividades.isEmpty()) {
            return List.of();
        }
        return atividadeRepo.findAllById(idsAtividades);
    }

    /**
     * Compara duas listas de atividades e identifica aquelas que foram inseridas na lista atual.
     * A comparação é feita com base na descrição da atividade, ignorando maiúsculas/minúsculas e espaços.
     *
     * @param atuais   A lista de atividades do mapa atual (em revisão).
     * @param vigentes A lista de atividades do mapa vigente (original).
     * @return Uma lista de {@link AtividadeImpactadaDto} representando as atividades inseridas.
     */
    public List<AtividadeImpactadaDto> detectarAtividadesInseridas(List<Atividade> atuais,
                                                                   List<Atividade> vigentes) {
        Set<String> descricoesVigentes = vigentes.stream()
                .map(a -> a.getDescricao().toLowerCase().trim())
                .collect(Collectors.toSet());

        return atuais.stream()
                .filter(a -> !descricoesVigentes.contains(
                        a.getDescricao().toLowerCase().trim()))
                .map(a -> new AtividadeImpactadaDto(
                        a.getCodigo(),
                        a.getDescricao(),
                        TipoImpactoAtividade.INSERIDA,
                        null,
                        List.of()
                ))
                .toList();
    }

    /**
     * Compara duas listas de atividades e identifica aquelas que foram removidas da lista vigente.
     * Também inclui as competências associadas a cada atividade removida para análise de impacto.
     *
     * @param atuais      A lista de atividades do mapa atual (em revisão).
     * @param vigentes    A lista de atividades do mapa vigente (original).
     * @param mapaVigente O mapa vigente, usado para buscar as competências associadas.
     * @return Uma lista de {@link AtividadeImpactadaDto} representando as atividades removidas.
     */
    public List<AtividadeImpactadaDto> detectarAtividadesRemovidas(
            List<Atividade> atuais,
            List<Atividade> vigentes,
            Mapa mapaVigente) {
        Set<String> descricoesAtuais = atuais.stream()
                .map(a -> a.getDescricao().toLowerCase().trim())
                .collect(Collectors.toSet());

        return vigentes.stream()
                .filter(a -> !descricoesAtuais.contains(
                        a.getDescricao().toLowerCase().trim()))
                .map(a -> new AtividadeImpactadaDto(
                        a.getCodigo(),
                        a.getDescricao(),
                        TipoImpactoAtividade.REMOVIDA,
                        null,
                        impactoCompetenciaService.obterCompetenciasDaAtividade(a.getCodigo(), mapaVigente)))
                .toList();
    }

    /**
     * Detecta atividades que tiveram seu conteúdo ou associações alterados entre a versão vigente e a atual.
     * <p>
     * <b>Nota:</b> A implementação atual é um placeholder e não detecta alterações.
     *
     * @param atuais      A lista de atividades do mapa atual.
     * @param vigentes    A lista de atividades do mapa vigente.
     * @param mapaVigente O mapa vigente.
     * @return Uma lista vazia de {@link AtividadeImpactadaDto}.
     */
    public List<AtividadeImpactadaDto> detectarAtividadesAlteradas(List<Atividade> atuais, List<Atividade> vigentes, Mapa mapaVigente) {
        return new ArrayList<>();
    }
}