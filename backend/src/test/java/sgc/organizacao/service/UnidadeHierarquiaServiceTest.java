package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.repo.ComumRepo;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.testutils.UnidadeTestBuilder;

import java.util.*;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para {@link UnidadeHierarquiaService}.
 * <p>
 * Foco em cobertura de branches (28 branches, alvo: 90%+).
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("UnidadeHierarquiaService")
class UnidadeHierarquiaServiceTest {

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private ComumRepo repo;

    @InjectMocks
    private UnidadeHierarquiaService service;

    private Unidade unidadeRaiz;
    private Unidade unidadeIntermediaria;
    private Unidade unidadeOperacional;

    @BeforeEach
    void setUp() {
        // Hierarquia: ADMIN -> COORD_11 -> ASSESSORIA_11
        unidadeRaiz = UnidadeTestBuilder.raiz().build();
        unidadeRaiz.setCodigo(1L);

        unidadeIntermediaria = UnidadeTestBuilder.intermediaria()
                .comSuperior(unidadeRaiz)
                .build();
        unidadeIntermediaria.setCodigo(2L);

        unidadeOperacional = UnidadeTestBuilder.operacional()
                .comSuperior(unidadeIntermediaria)
                .build();
        unidadeOperacional.setCodigo(3L);
    }

    @Nested
    @DisplayName("buscarArvoreHierarquica")
    class BuscarArvoreHierarquica {

        @Test
        @DisplayName("deve retornar árvore hierárquica completa")
        void deveRetornarArvoreCompleta() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(
                    unidadeRaiz, unidadeIntermediaria, unidadeOperacional
            ));
            
            UnidadeDto dtoRaiz = criarDtoComSubunidades(1L, "ADMIN");
            UnidadeDto dtoInter = criarDtoComSubunidades(2L, "COORD_11");
            UnidadeDto dtoOper = criarDtoComSubunidades(3L, "ASSESSORIA_11");
            
            when(usuarioMapper.toUnidadeDto(unidadeRaiz, true)).thenReturn(dtoRaiz);
            when(usuarioMapper.toUnidadeDto(unidadeIntermediaria, true)).thenReturn(dtoInter);
            when(usuarioMapper.toUnidadeDto(unidadeOperacional, true)).thenReturn(dtoOper);

            // Act
            List<UnidadeDto> resultado = service.buscarArvoreHierarquica();

            // Assert
            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().getCodigo()).isEqualTo(1L);
            assertThat(resultado.getFirst().getSubunidades()).hasSize(1);
            assertThat(resultado.getFirst().getSubunidades().getFirst().getCodigo()).isEqualTo(2L);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há unidades")
        void deveRetornarListaVaziaQuandoNaoHaUnidades() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of());

            // Act
            List<UnidadeDto> resultado = service.buscarArvoreHierarquica();

            // Assert
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("deve lidar com múltiplas raízes")
        void deveLidarComMultiplasRaizes() {
            // Arrange
            Unidade raiz2 = UnidadeTestBuilder.raiz()
                    .comCodigo("RAIZ2")
                    .comSigla("RAIZ2")
                    .build();
            raiz2.setCodigo(10L);

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(
                    unidadeRaiz, raiz2
            ));
            
            UnidadeDto dto1 = criarDtoComSubunidades(1L, "ADMIN");
            UnidadeDto dto2 = criarDtoComSubunidades(10L, "RAIZ2");
            
            when(usuarioMapper.toUnidadeDto(unidadeRaiz, true)).thenReturn(dto1);
            when(usuarioMapper.toUnidadeDto(raiz2, true)).thenReturn(dto2);

            // Act
            List<UnidadeDto> resultado = service.buscarArvoreHierarquica();

            // Assert
            assertThat(resultado).hasSize(2);
        }
    }

    @Nested
    @DisplayName("buscarArvoreComElegibilidade")
    class BuscarArvoreComElegibilidade {

        @Test
        @DisplayName("deve aplicar função de elegibilidade corretamente")
        void deveAplicarFuncaoElegibilidade() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(
                    unidadeRaiz, unidadeIntermediaria
            ));

            Predicate<Unidade> elegibilidadeChecker = u -> 
                    u.getCodigo().equals(1L);

            when(usuarioMapper.toUnidadeDto(unidadeRaiz, true))
                    .thenReturn(criarDtoComSubunidades(1L, "ADMIN"));
            when(usuarioMapper.toUnidadeDto(unidadeIntermediaria, false))
                    .thenReturn(criarDtoComSubunidades(2L, "COORD_11"));

            // Act
            List<UnidadeDto> resultado = service.buscarArvoreComElegibilidade(elegibilidadeChecker);

            // Assert
            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("deve marcar todas como elegíveis quando função retorna true")
        void deveMarcarTodasElegiveisQuandoFuncaoRetornaTrue() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(
                    unidadeRaiz
            ));

            Predicate<Unidade> todasElegiveis = u -> true;

            when(usuarioMapper.toUnidadeDto(unidadeRaiz, true))
                    .thenReturn(criarDtoComSubunidades(1L, "ADMIN"));

            // Act
            List<UnidadeDto> resultado = service.buscarArvoreComElegibilidade(todasElegiveis);

            // Assert
            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("deve marcar todas como não elegíveis quando função retorna false")
        void deveMarcarTodasNaoElegiveisQuandoFuncaoRetornaFalse() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(unidadeRaiz));

            Predicate<Unidade> nenhumaElegivel = u -> false;

            when(usuarioMapper.toUnidadeDto(unidadeRaiz, false))
                    .thenReturn(criarDtoComSubunidades(1L, "ADMIN"));

            // Act
            List<UnidadeDto> resultado = service.buscarArvoreComElegibilidade(nenhumaElegivel);

            // Assert
            assertThat(resultado).hasSize(1);
        }
    }

    @Nested
    @DisplayName("buscarIdsDescendentes")
    class BuscarIdsDescendentes {

        @Test
        @DisplayName("deve retornar todos os descendentes de uma unidade")
        void deveRetornarTodosDescendentes() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(
                    unidadeRaiz, unidadeIntermediaria, unidadeOperacional
            ));

            // Act
            List<Long> resultado = service.buscarIdsDescendentes(1L);

            // Assert
            assertThat(resultado).containsExactlyInAnyOrder(2L, 3L);
        }

        @Test
        @DisplayName("deve retornar lista vazia when não há descendentes")
        void deveRetornarVazioQuandoNaoHaDescendentes() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(
                    unidadeOperacional
            ));

            // Act
            List<Long> resultado = service.buscarIdsDescendentes(3L);

            // Assert
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("deve retornar apenas filhos diretos when não há netos")
        void deveRetornarApenasFilhosDiretos() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(
                    unidadeRaiz, unidadeIntermediaria
            ));

            // Act
            List<Long> resultado = service.buscarIdsDescendentes(1L);

            // Assert
            assertThat(resultado).containsExactly(2L);
        }
    }

    @Nested
    @DisplayName("buscarDescendentes(com mapa)")
    class BuscarDescendentesComMapa {

        @Test
        @DisplayName("deve usar mapa pré-carregado para buscar descendentes")
        void deveUsarMapaPreCarregado() {
            // Arrange
            Map<Long, List<Long>> mapa = new HashMap<>();
            mapa.put(1L, List.of(2L, 3L));
            mapa.put(2L, List.of(4L));

            // Act
            List<Long> resultado = service.buscarDescendentes(1L, mapa);

            // Assert
            assertThat(resultado).containsExactlyInAnyOrder(2L, 3L, 4L);
        }

        @Test
        @DisplayName("deve retornar lista vazia when código não está no mapa")
        void deveRetornarVazioQuandoCodigoNaoEstaNoMapa() {
            // Arrange
            Map<Long, List<Long>> mapa = new HashMap<>();

            // Act
            List<Long> resultado = service.buscarDescendentes(999L, mapa);

            // Assert
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("deve lidar com mapa vazio")
        void deveLidarComMapaVazio() {
            // Arrange
            Map<Long, List<Long>> mapa = new HashMap<>();

            // Act
            List<Long> resultado = service.buscarDescendentes(1L, mapa);

            // Assert
            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarMapaHierarquia")
    class BuscarMapaHierarquia {

        @Test
        @DisplayName("deve construir mapa pai-filhos corretamente")
        void deveConstruirMapaPaiFilhos() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(
                    unidadeRaiz, unidadeIntermediaria, unidadeOperacional
            ));

            // Act
            Map<Long, List<Long>> mapa = service.buscarMapaHierarquia();

            // Assert
            assertThat(mapa).hasSize(2);
            assertThat(mapa.get(1L)).containsExactly(2L);
            assertThat(mapa.get(2L)).containsExactly(3L);
        }

        @Test
        @DisplayName("deve retornar mapa vazio when não há unidades")
        void deveRetornarMapaVazioQuandoNaoHaUnidades() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of());

            // Act
            Map<Long, List<Long>> mapa = service.buscarMapaHierarquia();

            // Assert
            assertThat(mapa).isEmpty();
        }

        @Test
        @DisplayName("deve lidar com unidades sem superior")
        void deveLidarComUnidadesSemSuperior() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(unidadeRaiz));

            // Act
            Map<Long, List<Long>> mapa = service.buscarMapaHierarquia();

            // Assert
            assertThat(mapa).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarArvore(codigo)")
    class BuscarArvorePorCodigo {

        @Test
        @DisplayName("deve retornar unidade com sua subárvore")
        void deveRetornarUnidadeComSubarvore() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(
                    unidadeRaiz, unidadeIntermediaria, unidadeOperacional
            ));

            UnidadeDto dtoRaiz = criarDtoComSubunidades(1L, "ADMIN");
            UnidadeDto dtoInter = criarDtoComSubunidades(2L, "COORD_11");
            UnidadeDto dtoOper = criarDtoComSubunidades(3L, "ASSESSORIA_11");

            when(usuarioMapper.toUnidadeDto(unidadeRaiz, true)).thenReturn(dtoRaiz);
            when(usuarioMapper.toUnidadeDto(unidadeIntermediaria, true)).thenReturn(dtoInter);
            when(usuarioMapper.toUnidadeDto(unidadeOperacional, true)).thenReturn(dtoOper);

            // Act
            UnidadeDto resultado = service.buscarArvore(2L);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigo()).isEqualTo(2L);
        }

        @Test
        @DisplayName("deve lançar exceção when unidade não é encontrada")
        void deveLancarExcecaoQuandoNaoEncontrada() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(unidadeRaiz));

            UnidadeDto dto = criarDtoComSubunidades(1L, "ADMIN");
            when(usuarioMapper.toUnidadeDto(any(), anyBoolean())).thenReturn(dto);

            when(repo.buscar(Unidade.class, 999L)).thenThrow(new ErroEntidadeNaoEncontrada("Unidade", 999L));

            // Act & Assert
            assertThatThrownBy(() -> service.buscarArvore(999L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Unidade")
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("deve encontrar unidade em profundidade")
        void deveEncontrarUnidadeEmProfundidade() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(
                    unidadeRaiz, unidadeIntermediaria, unidadeOperacional
            ));

            UnidadeDto dtoRaiz = criarDtoComSubunidades(1L, "ADMIN");
            UnidadeDto dtoInter = criarDtoComSubunidades(2L, "COORD_11");
            UnidadeDto dtoOper = criarDtoComSubunidades(3L, "ASSESSORIA_11");

            when(usuarioMapper.toUnidadeDto(unidadeRaiz, true)).thenReturn(dtoRaiz);
            when(usuarioMapper.toUnidadeDto(unidadeIntermediaria, true)).thenReturn(dtoInter);
            when(usuarioMapper.toUnidadeDto(unidadeOperacional, true)).thenReturn(dtoOper);

            // Act
            UnidadeDto resultado = service.buscarArvore(3L);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigo()).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("buscarSiglasSubordinadas")
    class BuscarSiglasSubordinadas {

        @Test
        @DisplayName("deve retornar todas as siglas subordinadas")
        void deveRetornarTodasSiglasSubordinadas() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(
                    unidadeRaiz, unidadeIntermediaria, unidadeOperacional
            ));

            // Create DTO hierarchy properly
            UnidadeDto dtoOper = criarDtoComSubunidades(3L, "ASSESSORIA_11");
            UnidadeDto dtoInter = criarDtoComSubunidades(2L, "COORD_11");
            dtoInter.setSubunidades(List.of(dtoOper));
            UnidadeDto dtoRaiz = criarDtoComSubunidades(1L, "ADMIN");
            dtoRaiz.setSubunidades(List.of(dtoInter));

            when(usuarioMapper.toUnidadeDto(unidadeRaiz, true)).thenReturn(dtoRaiz);
            when(usuarioMapper.toUnidadeDto(unidadeIntermediaria, true)).thenReturn(dtoInter);
            when(usuarioMapper.toUnidadeDto(unidadeOperacional, true)).thenReturn(dtoOper);

            // Act
            List<String> resultado = service.buscarSiglasSubordinadas("ADMIN");

            // Assert
            assertThat(resultado).containsExactly("ADMIN", "COORD_11", "ASSESSORIA_11");
        }

        @Test
        @DisplayName("deve lançar exceção when sigla não é encontrada")
        void deveLancarExcecaoQuandoSiglaNaoEncontrada() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(unidadeRaiz));

            when(usuarioMapper.toUnidadeDto(unidadeRaiz, true))
                    .thenReturn(criarDtoComSubunidades(1L, "ADMIN"));

            when(repo.buscarPorSigla(Unidade.class, "INEXISTENTE"))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Unidade", "INEXISTENTE"));

            // Act & Assert
            assertThatThrownBy(() -> service.buscarSiglasSubordinadas("INEXISTENTE"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Unidade")
                    .hasMessageContaining("INEXISTENTE");
        }

        @Test
        @DisplayName("deve retornar apenas a própria sigla when não há subordinadas")
        void deveRetornarApenasPropriaSiglaQuandoNaoHaSubordinadas() {
            // Arrange
            Unidade unidadeSemFilhos = UnidadeTestBuilder.operacional().build();
            unidadeSemFilhos.setCodigo(99L);
            
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(unidadeSemFilhos));

            when(usuarioMapper.toUnidadeDto(unidadeSemFilhos, true))
                    .thenReturn(criarDtoComSubunidades(99L, "ASSESSORIA_11"));

            // Act
            List<String> resultado = service.buscarSiglasSubordinadas("ASSESSORIA_11");

            // Assert
            assertThat(resultado).containsExactly("ASSESSORIA_11");
        }

        @Test
        @DisplayName("deve encontrar sigla em profundidade na hierarquia")
        void deveEncontrarSiglaEmProfundidade() {
            // Arrange
            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(
                    unidadeRaiz, unidadeIntermediaria, unidadeOperacional
            ));

            UnidadeDto dtoOper = criarDtoComSubunidades(3L, "ASSESSORIA_11");
            UnidadeDto dtoInter = criarDtoComSubunidades(2L, "COORD_11");
            dtoInter.setSubunidades(List.of(dtoOper));
            UnidadeDto dtoRaiz = criarDtoComSubunidades(1L, "ADMIN");
            dtoRaiz.setSubunidades(List.of(dtoInter));

            when(usuarioMapper.toUnidadeDto(unidadeRaiz, true)).thenReturn(dtoRaiz);
            when(usuarioMapper.toUnidadeDto(unidadeIntermediaria, true)).thenReturn(dtoInter);
            when(usuarioMapper.toUnidadeDto(unidadeOperacional, true)).thenReturn(dtoOper);

            // Act - buscar sigla da unidade intermediária (não raiz)
            List<String> resultado = service.buscarSiglasSubordinadas("COORD_11");

            // Assert
            assertThat(resultado).containsExactly("COORD_11", "ASSESSORIA_11");
        }
    }

    @Nested
    @DisplayName("buscarSiglaSuperior")
    class BuscarSiglaSuperior {

        @Test
        @DisplayName("deve retornar sigla da unidade superior")
        void deveRetornarSiglaUnidadeSuperior() {
            // Arrange
            when(repo.buscarPorSigla(Unidade.class, "ASSESSORIA_11"))
                    .thenReturn(unidadeOperacional);

            // Act
            Optional<String> resultado = service.buscarSiglaSuperior("ASSESSORIA_11");

            // Assert
            assertThat(resultado).contains("COORD_11");
        }

        @Test
        @DisplayName("deve retornar Optional vazio when não há unidade superior")
        void deveRetornarVazioQuandoNaoHaSuperior() {
            // Arrange
            when(repo.buscarPorSigla(Unidade.class, "ADMIN"))
                    .thenReturn(unidadeRaiz);

            // Act
            Optional<String> resultado = service.buscarSiglaSuperior("ADMIN");

            // Assert
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("deve lançar exceção when sigla não existe")
        void deveLancarExcecaoQuandoSiglaNaoExiste() {
            // Arrange
            String sigla = "INEXISTENTE";
            when(repo.buscarPorSigla(Unidade.class, sigla))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Unidade", sigla));

            // Act & Assert
            assertThatThrownBy(() -> service.buscarSiglaSuperior(sigla))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Unidade")
                    .hasMessageContaining(sigla);
        }
    }

    @Nested
    @DisplayName("buscarSubordinadas")
    class BuscarSubordinadas {

        @Test
        @DisplayName("deve retornar unidades subordinadas diretas")
        void deveRetornarSubordinadasDiretas() {
            // Arrange
            when(unidadeRepo.findByUnidadeSuperiorCodigo(1L))
                    .thenReturn(List.of(unidadeIntermediaria));

            UnidadeDto dto = criarDtoComSubunidades(2L, "COORD_11");
            when(usuarioMapper.toUnidadeDto(unidadeIntermediaria, true)).thenReturn(dto);

            // Act
            List<UnidadeDto> resultado = service.buscarSubordinadas(1L);

            // Assert
            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().getCodigo()).isEqualTo(2L);
        }

        @Test
        @DisplayName("deve retornar lista vazia when não há subordinadas")
        void deveRetornarVazioQuandoNaoHaSubordinadas() {
            // Arrange
            when(unidadeRepo.findByUnidadeSuperiorCodigo(3L))
                    .thenReturn(List.of());

            // Act
            List<UnidadeDto> resultado = service.buscarSubordinadas(3L);

            // Assert
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("deve retornar múltiplas subordinadas")
        void deveRetornarMultiplasSubordinadas() {
            // Arrange
            Unidade filha1 = UnidadeTestBuilder.operacional()
                    .comCodigo("ASSESSORIA_1")
                    .comSigla("ASSESSORIA_1")
                    .build();
            filha1.setCodigo(10L);

            Unidade filha2 = UnidadeTestBuilder.operacional()
                    .comCodigo("ASSESSORIA_2")
                    .comSigla("ASSESSORIA_2")
                    .build();
            filha2.setCodigo(11L);

            when(unidadeRepo.findByUnidadeSuperiorCodigo(1L))
                    .thenReturn(List.of(filha1, filha2));

            UnidadeDto dto1 = criarDtoComSubunidades(10L, "ASSESSORIA_1");
            UnidadeDto dto2 = criarDtoComSubunidades(11L, "ASSESSORIA_2");

            when(usuarioMapper.toUnidadeDto(filha1, true)).thenReturn(dto1);
            when(usuarioMapper.toUnidadeDto(filha2, true)).thenReturn(dto2);

            // Act
            List<UnidadeDto> resultado = service.buscarSubordinadas(1L);

            // Assert
            assertThat(resultado).hasSize(2);
        }
    }

    @Nested
    @DisplayName("montarComSubunidades - método package-private")
    class MontarComSubunidades {

        @Test
        @DisplayName("deve montar DTO com subunidades recursivamente")
        void deveMontarComSubunidadesRecursivamente() {
            // Arrange
            UnidadeDto raiz = criarDtoComSubunidades(1L, "RAIZ");
            UnidadeDto filho = criarDtoComSubunidades(2L, "FILHO");
            UnidadeDto neto = criarDtoComSubunidades(3L, "NETO");

            Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>();
            mapaFilhas.put(1L, List.of(filho));
            mapaFilhas.put(2L, List.of(neto));
            mapaFilhas.put(3L, List.of());

            // Act
            UnidadeDto resultado = service.montarComSubunidades(raiz, mapaFilhas);

            // Assert
            assertThat(resultado.getSubunidades()).hasSize(1);
            assertThat(resultado.getSubunidades().getFirst().getSubunidades()).hasSize(1);
        }

        @Test
        @DisplayName("deve retornar DTO sem modificação when não há filhas")
        void deveRetornarSemModificacaoQuandoNaoHaFilhas() {
            // Arrange
            UnidadeDto dto = criarDtoComSubunidades(1L, "FOLHA");
            Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>();

            // Act
            UnidadeDto resultado = service.montarComSubunidades(dto, mapaFilhas);

            // Assert
            assertThat(resultado.getSubunidades()).isEmpty();
        }

        @Test
        @DisplayName("deve retornar DTO sem modificação when lista de filhas é vazia")
        void deveRetornarSemModificacaoQuandoListaFilhasVazia() {
            // Arrange
            UnidadeDto dto = criarDtoComSubunidades(1L, "FOLHA");
            Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>();
            mapaFilhas.put(1L, List.of());

            // Act
            UnidadeDto resultado = service.montarComSubunidades(dto, mapaFilhas);

            // Assert
            assertThat(resultado.getSubunidades()).isEmpty();
        }
    }

    // ==================== Métodos Auxiliares ====================

    private UnidadeDto criarDtoComSubunidades(Long codigo, String sigla) {
        return UnidadeDto.builder()
                .codigo(codigo)
                .sigla(sigla)
                .nome("Unidade " + sigla)
                .subunidades(new ArrayList<>())
                .isElegivel(true)
                .build();
    }
}
