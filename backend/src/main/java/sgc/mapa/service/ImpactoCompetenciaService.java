package sgc.mapa.service;

import lombok.*;
import org.springframework.stereotype.Service;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.TipoImpactoCompetencia;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImpactoCompetenciaService {
    private final CompetenciaRepo repositorioCompetencia;
    private final AtividadeRepo atividadeRepo;

    public List<CompetenciaImpactadaDto> identificarCompetenciasImpactadas(
            Mapa mapaVigente,
            List<AtividadeImpactadaDto> removidas,
            List<AtividadeImpactadaDto> alteradas) {

        Map<Long, CompetenciaImpactoAcumulador> mapaImpactos = new HashMap<>();
        List<Competencia> competenciasDoMapa =
                repositorioCompetencia.findByMapaCodigo(mapaVigente.getCodigo());

        for (AtividadeImpactadaDto atividadeDto : removidas) {
            if (atividadeDto.getCodigo() == null) continue;
            Atividade atividade = atividadeRepo.findById(atividadeDto.getCodigo()).orElse(null);
            if (atividade == null) continue;

            for (Competencia comp : competenciasDoMapa) {
                if (comp.getAtividades().stream()
                        .anyMatch(a -> a.getCodigo().equals(atividade.getCodigo()))) {
                    CompetenciaImpactoAcumulador acumulador =
                            mapaImpactos.computeIfAbsent(
                                    comp.getCodigo(),
                                    x ->
                                            CompetenciaImpactoAcumulador.builder()
                                                    .codigo(comp.getCodigo())
                                                    .descricao(comp.getDescricao())
                                                    .build());

                    acumulador.adicionarImpacto(
                            "Atividade removida: %s".formatted(atividadeDto.getDescricao()));
                }
            }
        }

        for (AtividadeImpactadaDto atividadeDto : alteradas) {
            if (atividadeDto.getCodigo() == null) continue;
            Atividade atividade = atividadeRepo.findById(atividadeDto.getCodigo()).orElse(null);
            if (atividade == null) continue;

            for (Competencia comp : atividade.getCompetencias()) {
                if (comp.getMapa().getCodigo().equals(mapaVigente.getCodigo())) {
                    CompetenciaImpactoAcumulador acumulador =
                            mapaImpactos.computeIfAbsent(
                                    comp.getCodigo(),
                                    x ->
                                            CompetenciaImpactoAcumulador.builder()
                                                    .codigo(comp.getCodigo())
                                                    .descricao(comp.getDescricao())
                                                    .build());

                    String detalhe =
                            String.format(
                                    "Atividade alterada: '%s' → '%s'",
                                    atividadeDto.getDescricaoAnterior(),
                                    atividadeDto.getDescricao());
                    acumulador.adicionarImpacto(detalhe);
                }
            }
        }

        return mapaImpactos.values().stream()
                .map(
                        acc ->
                                new CompetenciaImpactadaDto(
                                        acc.codigo,
                                        acc.descricao,
                                        new ArrayList<>(acc.atividadesAfetadas),
                                        TipoImpactoCompetencia.valueOf(
                                                determinarTipoImpacto(acc.atividadesAfetadas))))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<String> obterCompetenciasDaAtividade(Long codAtividade, Mapa mapaVigente) {
        if (codAtividade == null) return Collections.emptyList();
        Atividade atividade = atividadeRepo.findById(codAtividade).orElse(null);
        if (atividade == null) return Collections.emptyList();

        return atividade.getCompetencias().stream()
                .filter(c -> c.getMapa().getCodigo().equals(mapaVigente.getCodigo()))
                .map(Competencia::getDescricao)
                .toList();
    }

    private String determinarTipoImpacto(Set<String> atividadesAfetadas) {
        boolean temRemovida =
                atividadesAfetadas.stream().anyMatch(desc -> desc.contains("removida"));

        boolean temAlterada =
                atividadesAfetadas.stream().anyMatch(desc -> desc.contains("alterada"));

        if (temRemovida && temAlterada) {
            return "IMPACTO_GENERICO";
        } else if (temRemovida) {
            return "ATIVIDADE_REMOVIDA";
        } else if (temAlterada) {
            return "ATIVIDADE_ALTERADA";
        }
        return "IMPACTO_GENERICO";
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class CompetenciaImpactoAcumulador {
        private Long codigo;
        private String descricao;
        @Builder.Default
        private Set<String> atividadesAfetadas = new LinkedHashSet<>();

        /* default */ void adicionarImpacto(String descricaoImpacto) {
            atividadesAfetadas.add(descricaoImpacto);
        }
    }
}
