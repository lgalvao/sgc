package sgc.mapa.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO que representa o resultado completo da análise de impactos no mapa de competências devido a
 * alterações no cadastro de atividades.
 *
 * <p>CDU-12 - Verificar impactos no mapa de competências
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE) // to encourage use of factory methods
public class ImpactoMapaDto {

    private final boolean temImpactos;
    private final int totalAtividadesInseridas;
    private final int totalAtividadesRemovidas;
    private final int totalAtividadesAlteradas;
    private final int totalCompetenciasImpactadas;
    private final List<AtividadeImpactadaDto> atividadesInseridas;
    private final List<AtividadeImpactadaDto> atividadesRemovidas;
    private final List<AtividadeImpactadaDto> atividadesAlteradas;
    private final List<CompetenciaImpactadaDto> competenciasImpactadas;

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
