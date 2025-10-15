package sgc.mapa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.TipoImpactoCompetencia;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImpactoCompetenciaService {

    private final CompetenciaRepo repositorioCompetencia;
    private final CompetenciaAtividadeRepo repositorioCompetenciaAtividade;

    public List<CompetenciaImpactadaDto> identificarCompetenciasImpactadas(
            Mapa mapaVigente,
            List<AtividadeImpactadaDto> removidas,
            List<AtividadeImpactadaDto> alteradas) {

        Map<Long, CompetenciaImpactoAcumulador> mapaImpactos = new HashMap<>();

        for (AtividadeImpactadaDto atividade : removidas) {
            List<CompetenciaAtividade> vinculos = repositorioCompetenciaAtividade
                    .findByAtividadeCodigo(atividade.codigo());

            for (CompetenciaAtividade vinculo : vinculos) {
                Competencia comp = repositorioCompetencia
                        .findById(vinculo.getId().getCompetenciaCodigo())
                        .orElse(null);

                if (comp != null && comp.getMapa().getCodigo().equals(mapaVigente.getCodigo())) {
                    CompetenciaImpactoAcumulador acumulador = mapaImpactos
                            .computeIfAbsent(comp.getCodigo(), x -> new CompetenciaImpactoAcumulador(
                                    comp.getCodigo(),
                                    comp.getDescricao()));

                    acumulador.adicionarImpacto(
                            "Atividade removida: " + atividade.descricao());
                }
            }
        }

        for (AtividadeImpactadaDto atividade : alteradas) {
            List<CompetenciaAtividade> vinculos = repositorioCompetenciaAtividade.findByAtividadeCodigo(atividade.codigo());
            for (CompetenciaAtividade vinculo : vinculos) {
                Competencia comp = repositorioCompetencia
                        .findById(vinculo.getId().getCompetenciaCodigo())
                        .orElse(null);

                if (comp != null && comp.getMapa().getCodigo().equals(mapaVigente.getCodigo())) {
                    CompetenciaImpactoAcumulador acumulador = mapaImpactos
                            .computeIfAbsent(comp.getCodigo(),
                                    x -> new CompetenciaImpactoAcumulador(
                                            comp.getCodigo(),
                                            comp.getDescricao()));

                    String detalhe = String.format(
                            "Atividade alterada: '%s' → '%s'",
                            atividade.descricaoAnterior(),
                            atividade.descricao());
                    acumulador.adicionarImpacto(detalhe);
                }
            }
        }

        return mapaImpactos.values().stream()
                .map(acc -> new CompetenciaImpactadaDto(
                        acc.codigo,
                        acc.descricao,
                        new ArrayList<>(acc.atividadesAfetadas),
                        TipoImpactoCompetencia.valueOf(determinarTipoImpacto(acc.atividadesAfetadas))))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<String> obterCompetenciasDaAtividade(Long idAtividade, Mapa mapaVigente) {
        return repositorioCompetenciaAtividade
                .findByAtividadeCodigo(idAtividade)
                .stream()
                .map(ca -> {
                    Competencia comp = repositorioCompetencia
                            .findById(ca.getId().getCompetenciaCodigo())
                            .orElse(null);
                    if (comp != null && comp.getMapa().getCodigo().equals(mapaVigente.getCodigo())) {
                        return comp.getDescricao();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private String determinarTipoImpacto(Set<String> atividadesAfetadas) {
        boolean temRemovida = atividadesAfetadas.stream()
                .anyMatch(desc -> desc.contains("removida"));

        boolean temAlterada = atividadesAfetadas.stream()
                .anyMatch(desc -> desc.contains("alterada"));

        if (temRemovida && temAlterada) {
            return "IMPACTO_GENERICO";
        } else if (temRemovida) {
            return "ATIVIDADE_REMOVIDA";
        } else if (temAlterada) {
            return "ATIVIDADE_ALTERADA";
        }
        return "IMPACTO_GENERICO";
    }

    private static class CompetenciaImpactoAcumulador {
        final Long codigo;
        final String descricao;
        final Set<String> atividadesAfetadas = new LinkedHashSet<>();

        CompetenciaImpactoAcumulador(Long codigo, String descricao) {
            this.codigo = codigo;
            this.descricao = descricao;
        }

        void adicionarImpacto(String descricaoImpacto) {
            atividadesAfetadas.add(descricaoImpacto);
        }
    }
}