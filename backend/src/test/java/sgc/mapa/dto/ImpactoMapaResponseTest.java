package sgc.mapa.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sgc.mapa.model.Atividade;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("Testes de ImpactoMapaResponse")
class ImpactoMapaResponseTest {

    @Test
    @DisplayName("Deve cobrir branches de criação de ImpactoMapaResponse")
    void deveCobrirBranches() {
        AtividadeImpactadaDto atividade = AtividadeImpactadaDto.builder().build();
        // 1. Com impactos
        ImpactoMapaResponse d1 = ImpactoMapaResponse.builder()
                .temImpactos(true)
                .inseridas(List.of(atividade))
                .removidas(List.of())
                .alteradas(List.of())
                .competenciasImpactadas(List.of())
                .build();
        assertThat(d1.temImpactos()).isTrue();
        assertThat(d1.inseridas()).containsExactly(atividade);

        // 2. Sem impactos via builder
        ImpactoMapaResponse d2 = ImpactoMapaResponse.builder()
                .temImpactos(false)
                .inseridas(List.of())
                .removidas(List.of())
                .alteradas(List.of())
                .competenciasImpactadas(List.of())
                .build();
        assertThat(d2.temImpactos()).isFalse();

        // 3. Sem impacto via helper
        ImpactoMapaResponse d3 = ImpactoMapaResponse.semImpacto();
        assertThat(d3.temImpactos()).isFalse();
        assertThat(d3.inseridas()).isEmpty();
    }
}
