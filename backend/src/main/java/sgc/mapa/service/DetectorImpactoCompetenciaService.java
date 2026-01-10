package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.TipoImpactoCompetencia;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço especializado na análise de impactos em competências causados por alterações em atividades.
 *
 * <p>Responsável por identificar quais competências foram afetadas pela inserção, remoção ou
 * alteração de atividades durante o processo de revisão de cadastro.
 *
 * @see DetectorMudancasAtividadeService para detecção de mudanças em atividades
 */
@Service
@RequiredArgsConstructor
public class DetectorImpactoCompetenciaService {

    /**
     * Identifica quais competências foram impactadas pelas atividades removidas ou alteradas.
     */
    public List<CompetenciaImpactadaDto> competenciasImpactadas(
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

        removidas.stream()
                .filter(dto -> dto.getCodigo() != null)
                .forEach(dto -> {
                    List<Competencia> competenciasAfetadas = atividadeIdToCompetencias.getOrDefault(
                            dto.getCodigo(), List.of()
                    );
                    competenciasAfetadas.forEach(comp ->
                            adicionarImpacto(mapaImpactos, comp,
                                    "Atividade removida: %s".formatted(dto.getDescricao()),
                                    TipoImpactoCompetencia.ATIVIDADE_REMOVIDA)
                    );
                });
    }

    private void processarAlteradas(
            List<AtividadeImpactadaDto> alteradas,
            Map<String, Long> descricaoToVigenteId,
            Map<Long, List<Competencia>> atividadeIdToCompetencias,
            Map<Long, CompetenciaImpactoAcumulador> mapaImpactos) {

        alteradas.stream()
                .filter(dto -> dto.getDescricao() != null)
                .filter(dto -> descricaoToVigenteId.containsKey(dto.getDescricao()))
                .forEach(dto -> {
                    Long idVigente = descricaoToVigenteId.get(dto.getDescricao());
                    List<Competencia> competenciasAfetadas = atividadeIdToCompetencias.getOrDefault(idVigente, List.of());

                    competenciasAfetadas.forEach(comp -> {
                        String detalhe = "Atividade alterada: '%s' → '%s'".formatted(
                                dto.getDescricaoAnterior(), dto.getDescricao()
                        );
                        adicionarImpacto(mapaImpactos, comp, detalhe, TipoImpactoCompetencia.ATIVIDADE_ALTERADA);
                    });
                });
    }

    private void adicionarImpacto(
            Map<Long, CompetenciaImpactoAcumulador> mapaImpactos,
            Competencia comp,
            String detalhe,
            TipoImpactoCompetencia tipoImpacto) {

        CompetenciaImpactoAcumulador acumulador = mapaImpactos.computeIfAbsent(
                comp.getCodigo(),
                x -> new CompetenciaImpactoAcumulador(comp.getCodigo(), comp.getDescricao())
        );

        acumulador.adicionarImpacto(detalhe, tipoImpacto);
    }

    private CompetenciaImpactadaDto converterParaDto(CompetenciaImpactoAcumulador acc) {
        return new CompetenciaImpactadaDto(
                acc.codigo,
                acc.descricao,
                new ArrayList<>(acc.atividadesAfetadas),
                acc.obterTiposImpacto()
        );
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

    // Classe auxiliar interna para acumular impactos antes de converter para DTO
    private static class CompetenciaImpactoAcumulador {
        private final Long codigo;
        private final String descricao;
        private final Set<String> atividadesAfetadas = new LinkedHashSet<>();
        private final Set<TipoImpactoCompetencia> tiposImpacto = new LinkedHashSet<>();

        public CompetenciaImpactoAcumulador(Long codigo, String descricao) {
            this.codigo = codigo;
            this.descricao = descricao;
        }

        void adicionarImpacto(String descricaoImpacto, TipoImpactoCompetencia tipoImpacto) {
            atividadesAfetadas.add(descricaoImpacto);
            tiposImpacto.add(tipoImpacto);
        }

        List<TipoImpactoCompetencia> obterTiposImpacto() {
            return new ArrayList<>(tiposImpacto);
        }
    }
}

