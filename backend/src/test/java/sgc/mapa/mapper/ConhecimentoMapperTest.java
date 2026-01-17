package sgc.mapa.mapper;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.dto.AtualizarConhecimentoRequest;
import sgc.mapa.dto.ConhecimentoResponse;
import sgc.mapa.dto.CriarConhecimentoRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Conhecimento;
import sgc.comum.repo.RepositorioComum;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ConhecimentoMapper")
class ConhecimentoMapperTest {

    private ConhecimentoMapper mapper;

    @Mock
    private AtividadeRepo atividadeRepo;

    @Mock
    private RepositorioComum repo;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ConhecimentoMapper.class);
        mapper.atividadeRepo = atividadeRepo;
        mapper.repo = repo;
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

        @Test
        @DisplayName("Deve retornar null quando entidade é null")
        void deveRetornarNullQuandoEntidadeNull() {
            assertThat(mapper.toResponse(null)).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntityTests {

        @Test
        @DisplayName("Deve mapear CriarConhecimentoRequest para entidade")
        void deveMapearCriarConhecimentoRequestParaEntidade() {
            Atividade atividade = new Atividade();
            atividade.setCodigo(10L);

            when(repo.buscar(Atividade.class, 10L)).thenReturn(atividade);

            CriarConhecimentoRequest request = CriarConhecimentoRequest.builder()
                    .descricao("Conhecimento de Teste")
                    .atividadeCodigo(10L)
                    .build();

            Conhecimento conhecimento = mapper.toEntity(request);

            assertThat(conhecimento).isNotNull();
            assertThat(conhecimento.getDescricao()).isEqualTo("Conhecimento de Teste");
            assertThat(conhecimento.getAtividade()).isEqualTo(atividade);
        }

        @Test
        @DisplayName("Deve lançar erro se atividade não encontrada (CriarRequest)")
        void deveLancarErroSeAtividadeNaoEncontrada() {
            when(repo.buscar(Atividade.class, 99L)).thenThrow(new ErroEntidadeNaoEncontrada("Atividade", 99L));

            CriarConhecimentoRequest request = CriarConhecimentoRequest.builder()
                    .atividadeCodigo(99L)
                    .build();

            assertThatThrownBy(() -> mapper.toEntity(request))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
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

        @Test
        @DisplayName("Deve retornar null quando request é null")
        void deveRetornarNullQuandoRequestNull() {
            assertThat(mapper.toEntity((CriarConhecimentoRequest) null)).isNull();
            assertThat(mapper.toEntity((AtualizarConhecimentoRequest) null)).isNull();
        }
    }
}
