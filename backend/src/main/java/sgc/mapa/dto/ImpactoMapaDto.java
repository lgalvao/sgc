package sgc.mapa.dto;

import lombok.*;

import java.util.List;

/**
 * DTO que representa o resultado completo da análise de impactos
 * no mapa de competências devido a alterações no cadastro de atividades.
 * <p>
 * CDU-12 - Verificar impactos no mapa de competências
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // to encourage use of factory methods
public class ImpactoMapaDto {
    private boolean temImpactos;
    private int totalAtividadesInseridas;
    private int totalAtividadesRemovidas;
    private int totalAtividadesAlteradas;
    private int totalCompetenciasImpactadas;
    private List<AtividadeImpactadaDto> atividadesInseridas;
    private List<AtividadeImpactadaDto> atividadesRemovidas;
    private List<AtividadeImpactadaDto> atividadesAlteradas;
    private List<CompetenciaImpactadaDto> competenciasImpactadas;

    /**
     * Factory method for creating an ImpactoMapaDto with no impacts.
     *
     * @return An empty ImpactoMapaDto.
     */
    public static ImpactoMapaDto semImpacto() {
        // TODO Usar sempre o builder, nao usar construtores enormes
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
        // TODO Usar sempre o builder, nao usar construtores enormes
        return new ImpactoMapaDto(
                temImpactos,
                atividadesInseridas.size(),
                atividadesRemovidas.size(),
                atividadesAlteradas.size(),
                competenciasImpactadas.size(),
                List.copyOf(atividadesInseridas),
                List.copyOf(atividadesRemovidas),
                List.copyOf(atividadesAlteradas),
                List.copyOf(competenciasImpactadas)
        );
    }
}
