package sgc.mapa.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("Testes de ImpactoMapaDto")
class ImpactoMapaDtoTest {

    @Test
    @DisplayName("Deve cobrir branches de comImpactos")
    void deveCobrirBranchesComImpactos() {
        AtividadeImpactadaDto atividade = AtividadeImpactadaDto.builder().build();
        CompetenciaImpactadaDto competencia = CompetenciaImpactadaDto.builder().build();

        // 1. Apenas inseridas
        ImpactoMapaDto d1 = ImpactoMapaDto.comImpactos(List.of(atividade), List.of(), List.of(), List.of());
        assertThat(d1.temImpactos()).isTrue();

        // 2. Apenas removidas
        ImpactoMapaDto d2 = ImpactoMapaDto.comImpactos(List.of(), List.of(atividade), List.of(), List.of());
        assertThat(d2.temImpactos()).isTrue();

        // 3. Apenas alteradas
        ImpactoMapaDto d3 = ImpactoMapaDto.comImpactos(List.of(), List.of(), List.of(atividade), List.of());
        assertThat(d3.temImpactos()).isTrue();

        // 4. Apenas competências
        ImpactoMapaDto d4 = ImpactoMapaDto.comImpactos(List.of(), List.of(), List.of(), List.of(competencia));
        assertThat(d4.temImpactos()).isTrue();

        // 5. Nada (sem impactos)
        ImpactoMapaDto d5 = ImpactoMapaDto.comImpactos(List.of(), List.of(), List.of(), List.of());
        assertThat(d5.temImpactos()).isFalse();
    }

    @Test
    @DisplayName("Deve criar DTO sem impacto")
    void deveCriarSemImpacto() {
        ImpactoMapaDto dto = ImpactoMapaDto.semImpacto();
        assertThat(dto.temImpactos()).isFalse();
        assertThat(dto.atividadesInseridas()).isEmpty();
    }
}
