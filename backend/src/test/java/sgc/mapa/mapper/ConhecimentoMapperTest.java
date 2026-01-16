package sgc.mapa.mapper;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.dto.ConhecimentoDto;
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
    @DisplayName("toDto")
    class ToDtoTests {

        @Test
        @DisplayName("Deve mapear entidade para DTO")
        void deveMapearEntidadeParaDto() {
            Atividade atividade = new Atividade();
            atividade.setCodigo(10L);

            Conhecimento conhecimento = new Conhecimento();
            conhecimento.setCodigo(1L);
            conhecimento.setDescricao("Conhecimento de Teste");
            conhecimento.setAtividade(atividade);

            ConhecimentoDto dto = mapper.toDto(conhecimento);

            assertThat(dto).isNotNull();
            assertThat(dto.getCodigo()).isEqualTo(1L);
            assertThat(dto.getDescricao()).isEqualTo("Conhecimento de Teste");
            assertThat(dto.getAtividadeCodigo()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Deve retornar null quando entidade é null")
        void deveRetornarNullQuandoEntidadeNull() {
            assertThat(mapper.toDto(null)).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntityTests {

        @Test
        @DisplayName("Deve mapear DTO para entidade")
        void deveMapearDtoParaEntidade() {
            Atividade atividade = new Atividade();
            atividade.setCodigo(10L);

            when(repo.buscar(Atividade.class, 10L)).thenReturn(atividade);

            ConhecimentoDto dto = ConhecimentoDto.builder()
                    .codigo(1L)
                    .descricao("Conhecimento de Teste")
                    .atividadeCodigo(10L)
                    .build();

            Conhecimento conhecimento = mapper.toEntity(dto);

            assertThat(conhecimento).isNotNull();
            assertThat(conhecimento.getCodigo()).isEqualTo(1L);
            assertThat(conhecimento.getDescricao()).isEqualTo("Conhecimento de Teste");
            assertThat(conhecimento.getAtividade()).isEqualTo(atividade);
        }

        @Test
        @DisplayName("Deve lançar erro se atividade não encontrada")
        void deveLancarErroSeAtividadeNaoEncontrada() {
            when(repo.buscar(Atividade.class, 99L)).thenThrow(new ErroEntidadeNaoEncontrada("Atividade", 99L));

            ConhecimentoDto dto = ConhecimentoDto.builder()
                    .atividadeCodigo(99L)
                    .build();

            assertThatThrownBy(() -> mapper.toEntity(dto))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve retornar null quando DTO é null")
        void deveRetornarNullQuandoDtoNull() {
            assertThat(mapper.toEntity(null)).isNull();
        }
    }
}
