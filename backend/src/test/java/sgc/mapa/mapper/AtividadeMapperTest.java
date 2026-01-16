package sgc.mapa.mapper;

import org.junit.jupiter.api.*;
import org.mapstruct.factory.Mappers;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("AtividadeMapper")
@SuppressWarnings("deprecation")
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
        @DisplayName("Deve mapear CriarAtividadeRequest para entidade")
        void deveMapearCriarAtividadeRequestParaEntidade() {
            CriarAtividadeRequest request = CriarAtividadeRequest.builder()
                    .descricao("Atividade de Teste")
                    .mapaCodigo(10L)
                    .build();

            Atividade atividade = mapper.toEntity(request);

            assertThat(atividade).isNotNull();
            assertThat(atividade.getDescricao()).isEqualTo("Atividade de Teste");
            assertThat(atividade.getMapa()).isNull();
        }

        @Test
        @DisplayName("Deve mapear AtualizarAtividadeRequest para entidade")
        void deveMapearAtualizarAtividadeRequestParaEntidade() {
            AtualizarAtividadeRequest request = AtualizarAtividadeRequest.builder()
                    .descricao("Atividade Atualizada")
                    .build();

            Atividade atividade = mapper.toEntity(request);

            assertThat(atividade).isNotNull();
            assertThat(atividade.getDescricao()).isEqualTo("Atividade Atualizada");
        }

        @Test
        @DisplayName("Deve retornar null quando request é null")
        void deveRetornarNullQuandoRequestNull() {
            assertThat(mapper.toEntity((CriarAtividadeRequest) null)).isNull();
            assertThat(mapper.toEntity((AtualizarAtividadeRequest) null)).isNull();
        }
    }
}
