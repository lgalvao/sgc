package sgc.organizacao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.testutils.UnidadeTestBuilder;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import sgc.comum.repo.ComumRepo;

/**
 * Testes unitários para {@link UnidadeConsultaService}.
 * <p>
 * Foco em cobertura das 2 branches não cobertas.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("UnidadeConsultaService")
class UnidadeConsultaServiceTest {

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private ComumRepo repo;

    @InjectMocks
    private UnidadeConsultaService service;

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar unidade ativa quando encontrada")
        void deveRetornarUnidadeAtivaQuandoEncontrada() {
            // Arrange
            Unidade unidade = UnidadeTestBuilder.raiz().build();
            unidade.setCodigo(1L);
            unidade.setSituacao(SituacaoUnidade.ATIVA);

            when(repo.buscar(eq(Unidade.class), anyMap())).thenReturn(unidade);

            // Act
            Unidade resultado = service.buscarPorId(1L);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigo()).isEqualTo(1L);
            assertThat(resultado.getSituacao()).isEqualTo(SituacaoUnidade.ATIVA);
        }
    }

    @Nested
    @DisplayName("buscarPorSigla")
    class BuscarPorSigla {

        @Test
        @DisplayName("deve retornar unidade quando encontrada por sigla")
        void deveRetornarUnidadeQuandoEncontradaPorSigla() {
            // Arrange
            Unidade unidade = UnidadeTestBuilder.raiz().build();
            when(repo.buscarPorSigla(Unidade.class, "ADMIN")).thenReturn(unidade);

            // Act
            Unidade resultado = service.buscarPorSigla("ADMIN");

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getSigla()).isEqualTo("ADMIN");
        }
    }

    @Nested
    @DisplayName("buscarEntidadesPorIds")
    class BuscarEntidadesPorIds {

        @Test
        @DisplayName("deve retornar lista de unidades quando encontradas")
        void deveRetornarListaQuandoEncontradas() {
            // Arrange
            Unidade unidade1 = UnidadeTestBuilder.raiz().build();
            unidade1.setCodigo(1L);
            Unidade unidade2 = UnidadeTestBuilder.intermediaria().build();
            unidade2.setCodigo(2L);

            when(unidadeRepo.findAllById(List.of(1L, 2L)))
                    .thenReturn(List.of(unidade1, unidade2));

            // Act
            List<Unidade> resultado = service.buscarEntidadesPorIds(List.of(1L, 2L));

            // Assert
            assertThat(resultado).hasSize(2);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando nenhuma unidade encontrada")
        void deveRetornarListaVaziaQuandoNenhumaEncontrada() {
            // Arrange
            when(unidadeRepo.findAllById(List.of(999L))).thenReturn(List.of());

            // Act
            List<Unidade> resultado = service.buscarEntidadesPorIds(List.of(999L));

            // Assert
            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarTodasEntidadesComHierarquia")
    class BuscarTodasEntidadesComHierarquia {

        @Test
        @DisplayName("deve retornar todas as unidades com hierarquia")
        void deveRetornarTodasUnidadesComHierarquia() {
            // Arrange
            Unidade unidade1 = UnidadeTestBuilder.raiz().build();
            Unidade unidade2 = UnidadeTestBuilder.intermediaria().build();

            when(unidadeRepo.findAllWithHierarquia())
                    .thenReturn(List.of(unidade1, unidade2));

            // Act
            List<Unidade> resultado = service.buscarTodasEntidadesComHierarquia();

            // Assert
            assertThat(resultado).hasSize(2);
        }
    }

    @Nested
    @DisplayName("buscarSiglasPorIds")
    class BuscarSiglasPorIds {

        @Test
        @DisplayName("deve retornar lista de siglas quando encontradas")
        void deveRetornarListaDeSiglas() {
            // Arrange
            when(unidadeRepo.findSiglasByCodigos(List.of(1L, 2L)))
                    .thenReturn(List.of("ADMIN", "COORD_11"));

            // Act
            List<String> resultado = service.buscarSiglasPorIds(List.of(1L, 2L));

            // Assert
            assertThat(resultado).containsExactly("ADMIN", "COORD_11");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando nenhuma sigla encontrada")
        void deveRetornarListaVaziaQuandoNenhuma() {
            // Arrange
            when(unidadeRepo.findSiglasByCodigos(List.of(999L)))
                    .thenReturn(List.of());

            // Act
            List<String> resultado = service.buscarSiglasPorIds(List.of(999L));

            // Assert
            assertThat(resultado).isEmpty();
        }
    }
}
