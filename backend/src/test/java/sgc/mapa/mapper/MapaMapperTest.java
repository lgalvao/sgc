package sgc.mapa.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.model.Mapa;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("MapaMapper")
class MapaMapperTest {

    private MapaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(MapaMapper.class);
    }

    @Test
    @DisplayName("Deve mapear entidade para DTO")
    void deveMapearEntidadeParaDto() {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);

        MapaDto dto = mapper.toDto(mapa);

        assertThat(dto).isNotNull();
        assertThat(dto.codigo()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve mapear DTO para entidade")
    void deveMapearDtoParaEntidade() {
        MapaDto dto = MapaDto.builder()
                .codigo(1L)
                .build();

        Mapa mapa = mapper.toEntity(dto);

        assertThat(mapa).isNotNull();
        assertThat(mapa.getCodigo()).isEqualTo(1L);
    }
}
