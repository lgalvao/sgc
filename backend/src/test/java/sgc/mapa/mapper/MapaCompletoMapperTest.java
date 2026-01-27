package sgc.mapa.mapper;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;

@Tag("unit")
class MapaCompletoMapperTest {
    private final MapaCompletoMapper mapper = Mappers.getMapper(MapaCompletoMapper.class);

    @Test
    void toDto() {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setObservacoesDisponibilizacao("Obs");

        Atividade atividade = new Atividade();
        atividade.setCodigo(10L);

        Competencia competencia = new Competencia();
        competencia.setCodigo(2L);
        competencia.setDescricao("Comp");
        competencia.setAtividades(Set.of(atividade));

        List<Competencia> competencias = List.of(competencia);

        MapaCompletoDto dto = mapper.toDto(mapa, 100L, competencias);

        assertThat(dto).isNotNull();
        assertThat(dto.codigo()).isEqualTo(1L);
        assertThat(dto.subprocessoCodigo()).isEqualTo(100L);
        assertThat(dto.observacoes()).isEqualTo("Obs");
        assertThat(dto.competencias()).hasSize(1);
        assertThat(dto.competencias().getFirst().codigo()).isEqualTo(2L);
        assertThat(dto.competencias().getFirst().atividadesCodigos()).containsExactly(10L);
    }
}
