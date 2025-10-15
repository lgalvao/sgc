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

    public List<AtividadeImpactadaDto> detectarAtividadesAlteradas(List<Atividade> atuais, List<Atividade> vigentes, Mapa mapaVigente) {
        return new ArrayList<>();
    }
}