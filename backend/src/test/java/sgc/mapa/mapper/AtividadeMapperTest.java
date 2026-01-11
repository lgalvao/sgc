package sgc.mapa.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("AtividadeMapper")
class AtividadeMapperTest {

    private AtividadeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(AtividadeMapper.class);
    }

    @Nested
    @DisplayName("toDto")
    class ToDtoTests {

        @Test
        @DisplayName("Deve mapear entidade para DTO")
        void deveMapearEntidadeParaDto() {
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);

            Atividade atividade = new Atividade();
            atividade.setCodigo(1L);
            atividade.setDescricao("Atividade de Teste");
            atividade.setMapa(mapa);

            AtividadeDto dto = mapper.toDto(atividade);

            assertThat(dto).isNotNull();
            assertThat(dto.getCodigo()).isEqualTo(1L);
            assertThat(dto.getDescricao()).isEqualTo("Atividade de Teste");
            assertThat(dto.getMapaCodigo()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Deve retornar null quando entidade é null")
        void deveRetornarNullQuandoEntidadeNull() {
            AtividadeDto dto = mapper.toDto(null);

            assertThat(dto).isNull();
        }

        @Test
        @DisplayName("Deve lidar com mapa null na entidade")
        void deveLidarComMapaNull() {
            Atividade atividade = new Atividade();
            atividade.setCodigo(1L);
            atividade.setDescricao("Atividade de Teste");
            atividade.setMapa(null);

            AtividadeDto dto = mapper.toDto(atividade);

            assertThat(dto).isNotNull();
            assertThat(dto.getMapaCodigo()).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntityTests {

        @Test
        @DisplayName("Deve mapear DTO para entidade")
        void deveMapearDtoParaEntidade() {
            AtividadeDto dto = new AtividadeDto();
            dto.setCodigo(1L);
            dto.setDescricao("Atividade de Teste");
            dto.setMapaCodigo(10L);

            Atividade atividade = mapper.toEntity(dto);

            assertThat(atividade).isNotNull();
            assertThat(atividade.getCodigo()).isEqualTo(1L);
            assertThat(atividade.getDescricao()).isEqualTo("Atividade de Teste");
            // Mapa deve ser ignorado no mapeamento manual/automático simples
            assertThat(atividade.getMapa()).isNull();
        }

        @Test
        @DisplayName("Deve retornar null quando DTO é null")
        void deveRetornarNullQuandoDtoNull() {
            Atividade atividade = mapper.toEntity(null);

            assertThat(atividade).isNull();
        }
    }
}
