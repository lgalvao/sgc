package sgc.mapa.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.mapa.MapaDtoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.subprocesso.model.Subprocesso;

import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MapaCompletoDto")
class MapaCompletoDtoTest {

    private final MapaDtoMapper mapper = new MapaDtoMapper();

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

        MapaCompletoDto dto = mapper.paraMapaCompletoDto(mapa);

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

    @Test
    @DisplayName("deve mapear mapa com lista de competencias vazia")
    void deveMapearMapaComListaDeCompetenciasVazia() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(70L);

        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        mapa.setSubprocesso(subprocesso);
        mapa.setCompetencias(new LinkedHashSet<>());

        MapaCompletoDto dto = mapper.paraMapaCompletoDto(mapa);

        assertThat(dto.codigo()).isEqualTo(10L);
        assertThat(dto.subprocessoCodigo()).isEqualTo(70L);
        assertThat(dto.competencias()).isEmpty();
    }
}
