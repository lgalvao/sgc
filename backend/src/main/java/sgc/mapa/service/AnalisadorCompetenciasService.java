package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.model.TipoImpactoCompetencia;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço especializado na análise de impactos em competências causados por alterações em atividades.
 */
@Service
@RequiredArgsConstructor
public class AnalisadorCompetenciasService {

    /**
     * Identifica quais competências foram impactadas pelas atividades removidas ou alteradas.
     */
    public List<CompetenciaImpactadaDto> identificarCompetenciasImpactadas(
            List<Competencia> competenciasDoMapa,
            List<AtividadeImpactadaDto> removidas,
            List<AtividadeImpactadaDto> alteradas,
            List<Atividade> atividadesVigentes) {

        Map<Long, CompetenciaImpactoAcumulador> mapaImpactos = new HashMap<>();

        // Indexar competências por ID da atividade
        Map<Long, List<Competencia>> atividadeIdToCompetencias = construirMapaAtividadeCompetencias(competenciasDoMapa);

        // Indexar IDs das atividades vigentes por descrição para lookup rápido
        Map<String, Long> descricaoToVigenteId =
                atividadesVigentes.stream()
                        .collect(Collectors.toMap(Atividade::getDescricao, Atividade::getCodigo));

        // Processar Atividades Removidas
        processarRemovidas(removidas, atividadeIdToCompetencias, mapaImpactos);

        // Processar Atividades Alteradas
        processarAlteradas(alteradas, descricaoToVigenteId, atividadeIdToCompetencias, mapaImpactos);

        return mapaImpactos.values().stream()
                .map(this::converterParaDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void processarRemovidas(
            List<AtividadeImpactadaDto> removidas,
            Map<Long, List<Competencia>> atividadeIdToCompetencias,
            Map<Long, CompetenciaImpactoAcumulador> mapaImpactos) {

        for (AtividadeImpactadaDto atividadeDto : removidas) {
            if (atividadeDto.getCodigo() == null) continue;

            List<Competencia> competenciasAfetadas =
                    atividadeIdToCompetencias.getOrDefault(
                            atividadeDto.getCodigo(), Collections.emptyList());

            for (Competencia comp : competenciasAfetadas) {
                adicionarImpacto(mapaImpactos, comp, "Atividade removida: %s".formatted(atividadeDto.getDescricao()));
            }
        }
    }

    private void processarAlteradas(
            List<AtividadeImpactadaDto> alteradas,
            Map<String, Long> descricaoToVigenteId,
            Map<Long, List<Competencia>> atividadeIdToCompetencias,
            Map<Long, CompetenciaImpactoAcumulador> mapaImpactos) {

        for (AtividadeImpactadaDto atividadeDto : alteradas) {
            String descricao = atividadeDto.getDescricao();
            if (descricao == null) continue;

            Long idVigente = descricaoToVigenteId.get(descricao);
            if (idVigente == null) continue;

            List<Competencia> competenciasAfetadas =
                    atividadeIdToCompetencias.getOrDefault(idVigente, Collections.emptyList());

            for (Competencia comp : competenciasAfetadas) {
                String detalhe = String.format(
                        "Atividade alterada: '%s' → '%s'",
                        atividadeDto.getDescricaoAnterior(),
                        atividadeDto.getDescricao());
                adicionarImpacto(mapaImpactos, comp, detalhe);
            }
        }
    }

    private void adicionarImpacto(
            Map<Long, CompetenciaImpactoAcumulador> mapaImpactos,
            Competencia comp,
            String detalhe) {

        CompetenciaImpactoAcumulador acumulador = mapaImpactos.computeIfAbsent(
                comp.getCodigo(),
                x -> new CompetenciaImpactoAcumulador(comp.getCodigo(), comp.getDescricao()));

        acumulador.adicionarImpacto(detalhe);
    }

    private CompetenciaImpactadaDto converterParaDto(CompetenciaImpactoAcumulador acc) {
        return new CompetenciaImpactadaDto(
                acc.codigo,
                acc.descricao,
                new ArrayList<>(acc.atividadesAfetadas),
                TipoImpactoCompetencia.valueOf(determinarTipoImpacto(acc.atividadesAfetadas)));
    }

    /**
     * Constrói um mapa invertido de Atividade ID -> Lista de Competências.
     * Útil para buscar quais competências estão ligadas a uma atividade (O(1)).
     */
    public Map<Long, List<Competencia>> construirMapaAtividadeCompetencias(List<Competencia> competencias) {
        Map<Long, List<Competencia>> mapa = new HashMap<>();
        for (Competencia comp : competencias) {
            for (Atividade ativ : comp.getAtividades()) {
                mapa.computeIfAbsent(ativ.getCodigo(), k -> new ArrayList<>()).add(comp);
            }
        }
        return mapa;
    }

    private String determinarTipoImpacto(Set<String> atividadesAfetadas) {
        boolean temRemovida = atividadesAfetadas.stream().anyMatch(desc -> desc.contains("removida"));
        boolean temAlterada = atividadesAfetadas.stream().anyMatch(desc -> desc.contains("alterada"));

        if (temRemovida && temAlterada) {
            return "IMPACTO_GENERICO";
        } else if (temRemovida) {
            return "ATIVIDADE_REMOVIDA";
        } else if (temAlterada) {
            return "ATIVIDADE_ALTERADA";
        }
        return "IMPACTO_GENERICO";
    }

    // Classe auxiliar interna para acumular impactos antes de converter para DTO
    private static class CompetenciaImpactoAcumulador {
        private final Long codigo;
        private final String descricao;
        private final Set<String> atividadesAfetadas = new LinkedHashSet<>();

        public CompetenciaImpactoAcumulador(Long codigo, String descricao) {
            this.codigo = codigo;
            this.descricao = descricao;
        }

        void adicionarImpacto(String descricaoImpacto) {
            atividadesAfetadas.add(descricaoImpacto);
        }
    }
}
