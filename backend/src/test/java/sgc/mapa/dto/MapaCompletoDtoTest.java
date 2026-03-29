package sgc.mapa.dto;

import org.junit.jupiter.api.*;
import sgc.mapa.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MapaCompletoDto")
class MapaCompletoDtoTest {

    @Test
    @DisplayName("deve mapear mapa completo com competencias")
    void deveMapearMapaCompletoComCompetencias() {
        Atividade atividade = new Atividade();
        atividade.setCodigo(30L);
        atividade.setDescricao("Atividade");
        atividade.setConhecimentos(new LinkedHashSet<>());

        Competencia competencia = new Competencia();
        competencia.setCodigo(20L);
        competencia.setDescricao("Competencia");
        competencia.setAtividades(new LinkedHashSet<>(List.of(atividade)));

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(70L);

        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        mapa.setSubprocesso(subprocesso);
        mapa.setObservacoesDisponibilizacao("Observacoes");
        mapa.setCompetencias(new LinkedHashSet<>(List.of(competencia)));

        MapaCompletoDto dto = MapaCompletoDto.fromEntity(mapa);

        assertThat(dto.codigo()).isEqualTo(10L);
        assertThat(dto.subprocessoCodigo()).isEqualTo(70L);
        assertThat(dto.observacoes()).isEqualTo("Observacoes");
        assertThat(dto.competencias()).singleElement().satisfies(item -> {
            assertThat(item.codigo()).isEqualTo(20L);
            assertThat(item.atividades()).singleElement().satisfies(atividadeDto -> {
                assertThat(atividadeDto.codigo()).isEqualTo(30L);
                assertThat(atividadeDto.descricao()).isEqualTo("Atividade");
            });
        });
    }
}
