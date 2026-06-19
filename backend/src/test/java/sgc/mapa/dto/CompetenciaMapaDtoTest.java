package sgc.mapa.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.mapa.MapaDtoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;

import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CompetenciaMapaDto")
class CompetenciaMapaDtoTest {

    private final MapaDtoMapper mapper = new MapaDtoMapper();

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

        CompetenciaMapaDto dto = mapper.paraCompetenciaMapaDto(competencia);

        assertThat(dto.codigo()).isEqualTo(10L);
        assertThat(dto.atividades()).singleElement().satisfies(item -> {
            assertThat(item.codigo()).isEqualTo(40L);
            assertThat(item.descricao()).isEqualTo("Atividade");
        });
    }
}
