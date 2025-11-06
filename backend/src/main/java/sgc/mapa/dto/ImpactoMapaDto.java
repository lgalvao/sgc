package sgc.mapa.dto;

import java.util.List;

/**
 * DTO que representa o resultado completo da análise de impactos
 * no mapa de competências devido a alterações no cadastro de atividades.
 * <p>
 * CDU-12 - Verificar impactos no mapa de competências
 *
 * @param temImpactos                 Indica se há algum impacto.
 * @param totalAtividadesInseridas    Total de atividades inseridas.
 * @param totalAtividadesRemovidas    Total de atividades removidas.
 * @param totalAtividadesAlteradas    Total de atividades alteradas.
 * @param totalCompetenciasImpactadas Total de competências impactadas.
 * @param atividadesInseridas         Lista de atividades inseridas.
 * @param atividadesRemovidas         Lista de atividades removidas.
 * @param atividadesAlteradas         Lista de atividades alteradas.
 * @param competenciasImpactadas      Lista de competências impactadas.
 */
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

    // Compact constructor for defensive copying to ensure true immutability
    public ImpactoMapaDto {
        atividadesInseridas = List.copyOf(atividadesInseridas);
        atividadesRemovidas = List.copyOf(atividadesRemovidas);
        atividadesAlteradas = List.copyOf(atividadesAlteradas);
        competenciasImpactadas = List.copyOf(competenciasImpactadas);
    }

    /**
     * Factory method for creating an ImpactoMapaDto with no impacts.
     *
     * @return An empty ImpactoMapaDto.
     */
    public static ImpactoMapaDto semImpacto() {
        return new ImpactoMapaDto(false, 0, 0, 0, 0, List.of(), List.of(), List.of(), List.of());
    }

    /**
     * Factory method for creating an ImpactoMapaDto from lists of changes.
     * It calculates the totals and the 'temImpactos' flag automatically.
     */
    public static ImpactoMapaDto comImpactos(
            List<AtividadeImpactadaDto> atividadesInseridas,
            List<AtividadeImpactadaDto> atividadesRemovidas,
            List<AtividadeImpactadaDto> atividadesAlteradas,
            List<CompetenciaImpactadaDto> competenciasImpactadas
    ) {
        boolean temImpactos = !atividadesInseridas.isEmpty() || !atividadesRemovidas.isEmpty() || !atividadesAlteradas.isEmpty() || !competenciasImpactadas.isEmpty();
        return new ImpactoMapaDto(
                temImpactos,
                atividadesInseridas.size(),
                atividadesRemovidas.size(),
                atividadesAlteradas.size(),
                competenciasImpactadas.size(),
                atividadesInseridas,
                atividadesRemovidas,
                atividadesAlteradas,
                competenciasImpactadas
        );
    }
}
