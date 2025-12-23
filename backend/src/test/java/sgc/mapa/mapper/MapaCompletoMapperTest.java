package sgc.mapa.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.atividade.internal.model.Atividade;
import sgc.mapa.api.MapaCompletoDto;
import sgc.mapa.internal.model.Competencia;
import sgc.mapa.internal.model.Mapa;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(dto.getCodigo()).isEqualTo(1L);
        assertThat(dto.getSubprocessoCodigo()).isEqualTo(100L);
        assertThat(dto.getObservacoes()).isEqualTo("Obs");
        assertThat(dto.getCompetencias()).hasSize(1);
        assertThat(dto.getCompetencias().get(0).getCodigo()).isEqualTo(2L);
        assertThat(dto.getCompetencias().get(0).getAtividadesCodigos()).containsExactly(10L);
    }
}
