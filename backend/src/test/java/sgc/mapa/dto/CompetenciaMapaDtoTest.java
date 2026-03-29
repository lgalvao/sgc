package sgc.mapa.dto;

import org.junit.jupiter.api.*;
import sgc.mapa.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CompetenciaMapaDto")
class CompetenciaMapaDtoTest {

    @Test
    @DisplayName("deve mapear competencia com atividades")
    void deveMapearCompetenciaComAtividades() {
        Atividade atividade = new Atividade();
        atividade.setCodigo(40L);
        atividade.setDescricao("Atividade");
        atividade.setConhecimentos(new LinkedHashSet<>());

        Competencia competencia = new Competencia();
        competencia.setCodigo(10L);
        competencia.setDescricao("Competencia");
        competencia.setAtividades(new LinkedHashSet<>(List.of(atividade)));

        CompetenciaMapaDto dto = CompetenciaMapaDto.fromEntity(competencia);

        assertThat(dto.codigo()).isEqualTo(10L);
        assertThat(dto.atividades()).singleElement().satisfies(item -> {
            assertThat(item.codigo()).isEqualTo(40L);
            assertThat(item.descricao()).isEqualTo("Atividade");
        });
    }
}
