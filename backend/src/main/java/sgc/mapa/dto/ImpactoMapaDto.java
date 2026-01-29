package sgc.mapa.dto;

import lombok.Builder;

import java.util.List;

/**
 * DTO que representa o resultado completo da análise de impactos no mapa de competências devido a
 * alterações no cadastro de atividades.
 *
 * <p>CDU-12 - Verificar impactos no mapa de competências
 */
@Builder
public record ImpactoMapaDto(
        boolean temImpactos,
        int totalAtividadesInseridas,
        int totalAtividadesRemovidas,
        int totalAtividadesAlteradas,
        int totalCompetenciasImpactadas,
        List<AtividadeImpactadaDto> atividadesInseridas,
        List<AtividadeImpactadaDto> atividadesRemovidas,
        List<AtividadeImpactadaDto> atividadesAlteradas,
        List<CompetenciaImpactadaDto> competenciasImpactadas
) {

    /**
     * Factory method for creating an ImpactoMapaDto with no impacts.
     *
     * @return An empty ImpactoMapaDto.
     */
    public static ImpactoMapaDto semImpacto() {
        return ImpactoMapaDto.builder()
                .temImpactos(false)
                .totalAtividadesInseridas(0)
                .totalAtividadesRemovidas(0)
                .totalAtividadesAlteradas(0)
                .totalCompetenciasImpactadas(0)
                .atividadesInseridas(List.of())
                .atividadesRemovidas(List.of())
                .atividadesAlteradas(List.of())
                .competenciasImpactadas(List.of())
                .build();
    }

    public static ImpactoMapaDto comImpactos(
            List<AtividadeImpactadaDto> atividadesInseridas,
            List<AtividadeImpactadaDto> atividadesRemovidas,
            List<AtividadeImpactadaDto> atividadesAlteradas,
            List<CompetenciaImpactadaDto> competenciasImpactadas) {

        boolean temImpactos = !atividadesInseridas.isEmpty()
                || !atividadesRemovidas.isEmpty()
                || !atividadesAlteradas.isEmpty()
                || !competenciasImpactadas.isEmpty();

        return ImpactoMapaDto.builder()
                .temImpactos(temImpactos)
                .totalAtividadesInseridas(atividadesInseridas.size())
                .totalAtividadesRemovidas(atividadesRemovidas.size())
                .totalAtividadesAlteradas(atividadesAlteradas.size())
                .totalCompetenciasImpactadas(competenciasImpactadas.size())
                .atividadesInseridas(atividadesInseridas)
                .atividadesRemovidas(atividadesRemovidas)
                .atividadesAlteradas(atividadesAlteradas)
                .competenciasImpactadas(competenciasImpactadas)
                .build();
    }
}
