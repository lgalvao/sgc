package sgc.mapa.mapper;

import org.junit.jupiter.api.*;
import org.mapstruct.factory.Mappers;
import sgc.mapa.dto.AtualizarConhecimentoRequest;
import sgc.mapa.dto.ConhecimentoResponse;
import sgc.mapa.dto.CriarConhecimentoRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("ConhecimentoMapper")
class ConhecimentoMapperTest {

    private ConhecimentoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ConhecimentoMapper.class);
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponseTests {
        @Test
        @DisplayName("Deve mapear entidade para Response")
        void deveMapearEntidadeParaResponse() {
            Atividade atividade = new Atividade();
            atividade.setCodigo(10L);

            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);
            conhecimento.setDescricao("Conhecimento de Teste");
            conhecimento.setAtividade(atividade);

            ConhecimentoResponse dto = mapper.toResponse(conhecimento);

            assertThat(dto).isNotNull();
            assertThat(dto.codigo()).isEqualTo(1L);
            assertThat(dto.descricao()).isEqualTo("Conhecimento de Teste");
            assertThat(dto.atividadeCodigo()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntityTests {

        @Test
        @DisplayName("Deve mapear CriarConhecimentoRequest para entidade (sem Atividade)")
        void deveMapearCriarConhecimentoRequestParaEntidade() {
            CriarConhecimentoRequest request = CriarConhecimentoRequest.builder()
                    .descricao("Conhecimento de Teste")
                    .atividadeCodigo(10L)
                    .build();

            Conhecimento conhecimento = mapper.toEntity(request);

            assertThat(conhecimento).isNotNull();
            assertThat(conhecimento.getDescricao()).isEqualTo("Conhecimento de Teste");
            // Atividade não é setada pelo mapper - deve ser setada no Service
            assertThat(conhecimento.getAtividade()).isNull();
        }

        @Test
        @DisplayName("Deve mapear AtualizarConhecimentoRequest para entidade")
        void deveMapearAtualizarConhecimentoRequestParaEntidade() {
            AtualizarConhecimentoRequest request = AtualizarConhecimentoRequest.builder()
                    .descricao("Descricao Atualizada")
                    .build();

            Conhecimento conhecimento = mapper.toEntity(request);

            assertThat(conhecimento).isNotNull();
            assertThat(conhecimento.getDescricao()).isEqualTo("Descricao Atualizada");
        }
    }
    @Nested
    @DisplayName("Cobertura de Nulos")
    class CoberturaNulos {
        @Test
        @DisplayName("Deve retornar null quando parâmetros são nulos")
        void deveRetornarNullQuandoNulos() {
            assertThat(mapper.toResponse(null)).isNull();
            assertThat(mapper.toEntity((sgc.mapa.dto.CriarConhecimentoRequest) null)).isNull();
            assertThat(mapper.toEntity((sgc.mapa.dto.AtualizarConhecimentoRequest) null)).isNull();
        }

        @Test
        @DisplayName("Deve retornar atividadeCodigo nulo quando atividade é nula")
        void deveRetornarAtividadeCodigoNulo() {
            Conhecimento c = new Conhecimento();
            c.setAtividade(null);
            assertThat(mapper.toResponse(c).atividadeCodigo()).isNull();
        }
    }
}
