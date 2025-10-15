package sgc.mapa.dto;

import java.util.List;

/**
 * DTO que representa o resultado completo da análise de impactos
 * no mapa de competências devido a alterações no cadastro de atividades.
 * <p>
 * CDU-12 - Verificar impactos no mapa de competências
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
    public ImpactoMapaDto(
        boolean temImpactos,
        int totalAtividadesInseridas,
        int totalAtividadesRemovidas,
        int totalAtividadesAlteradas,
        List<AtividadeImpactadaDto> atividadesInseridas,
        List<AtividadeImpactadaDto> atividadesRemovidas,
        List<AtividadeImpactadaDto> atividadesAlteradas,
        List<CompetenciaImpactadaDto> competenciasImpactadas
    ) {
        this(
            temImpactos,
            totalAtividadesInseridas,
            totalAtividadesRemovidas,
            totalAtividadesAlteradas,
            competenciasImpactadas.size(),
            new java.util.ArrayList<>(atividadesInseridas),
            new java.util.ArrayList<>(atividadesRemovidas),
            new java.util.ArrayList<>(atividadesAlteradas),
            new java.util.ArrayList<>(competenciasImpactadas)
        );
    }

    @Override
    public List<AtividadeImpactadaDto> atividadesInseridas() {
        return new java.util.ArrayList<>(atividadesInseridas);
    }

    @Override
    public List<AtividadeImpactadaDto> atividadesRemovidas() {
        return new java.util.ArrayList<>(atividadesRemovidas);
    }

    @Override
    public List<AtividadeImpactadaDto> atividadesAlteradas() {
        return new java.util.ArrayList<>(atividadesAlteradas);
    }

    @Override
    public List<CompetenciaImpactadaDto> competenciasImpactadas() {
        return new java.util.ArrayList<>(competenciasImpactadas);
    }
}