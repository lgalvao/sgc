package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.model.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.testutils.*;

import java.util.*;
import java.util.function.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link UnidadeHierarquiaService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UnidadeHierarquiaService")
@SuppressWarnings("NullAway.Init")
class UnidadeHierarquiaServiceTest {

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private ComumRepo repo;

    @Mock
    private UnidadeService unidadeService;

    @InjectMocks
    private UnidadeHierarquiaService service;

    private Unidade unidadeRaiz;
    private Unidade unidadeIntermediaria;
    private Unidade unidadeOperacional;

    @BeforeEach
    void setUp() {
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

    @Test
    @DisplayName("Deve buscar árvore hierárquica completa")
    void deveBuscarArvoreHierarquica() {
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(unidadeRaiz, unidadeIntermediaria, unidadeOperacional));

        List<UnidadeDto> resultado = service.buscarArvoreHierarquica();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getSigla()).isEqualTo(unidadeRaiz.getSigla());
        assertThat(resultado.getFirst().getSubunidades()).hasSize(1);
        assertThat(resultado.getFirst().getSubunidades().getFirst().getSigla()).isEqualTo(unidadeIntermediaria.getSigla());
    }

    @Test
    @DisplayName("Deve buscar árvore com elegibilidade")
    void deveBuscarComElegibilidade() {
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(unidadeRaiz, unidadeIntermediaria, unidadeOperacional));

        Predicate<Unidade> soOperacional = u -> u.getCodigo().equals(3L);
        List<UnidadeDto> resultado = service.buscarArvoreComElegibilidade(soOperacional);

        assertThat(resultado).hasSize(1);

        assertThat(resultado.getFirst().isElegivel()).isFalse();

        UnidadeDto operacionalDto = resultado.getFirst().getSubunidades().getFirst().getSubunidades().getFirst();
        assertThat(operacionalDto.isElegivel()).isTrue();
    }

    @Test
    @DisplayName("Deve buscar IDs descendentes")
    void deveBuscarIdsDescendentes() {
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(unidadeRaiz, unidadeIntermediaria, unidadeOperacional));

        List<Long> descendentes = service.buscarIdsDescendentes(1L);

        assertThat(descendentes).containsExactlyInAnyOrder(2L, 3L);
    }

    @Test
    @DisplayName("Deve buscar árvore por código (nível profundo)")
    void deveBuscarArvorePorCodigo() {
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(unidadeRaiz, unidadeIntermediaria, unidadeOperacional));

        UnidadeDto resultado = service.buscarArvore(3L);

        assertThat(resultado.getSigla()).isEqualTo(unidadeOperacional.getSigla());
    }

    @Test
    @DisplayName("Deve buscar siglas subordinadas (a partir da raiz)")
    void deveBuscarSiglasSubordinadas() {
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(unidadeRaiz, unidadeIntermediaria, unidadeOperacional));

        List<String> siglas = service.buscarSiglasSubordinadas(unidadeRaiz.getSigla());

        assertThat(siglas).containsExactlyInAnyOrder(
                unidadeRaiz.getSigla(),
                unidadeIntermediaria.getSigla(),
                unidadeOperacional.getSigla()
        );
    }

    @Test
    @DisplayName("buscarArvore deve buscar no repo se não encontrar na hierarquia")
    void buscarArvore_DeveBuscarNoRepoSeNaoEncontrarNaHierarquia() {
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(unidadeRaiz));
        Unidade extra = Unidade.builder()
                .codigo(99L)
                .sigla("EXTRA")
                .tipo(TipoUnidade.OPERACIONAL)
                .build();
        when(repo.buscar(Unidade.class, 99L)).thenReturn(extra);

        UnidadeDto resultado = service.buscarArvore(99L);

        assertThat(resultado.getSigla()).isEqualTo("EXTRA");
    }

    @Test
    @DisplayName("buscarSiglasSubordinadas deve retornar vazio se não encontrar sigla")
    void buscarSiglasSubordinadas_DeveRetornarVazioSeNaoEncontrar() {
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(unidadeRaiz));
        Unidade extra = Unidade.builder()
                .codigo(999L)
                .sigla("INEXISTENTE")
                .tipo(TipoUnidade.OPERACIONAL)
                .build();
        when(repo.buscarPorSigla(Unidade.class, "INEXISTENTE")).thenReturn(extra);

        List<String> resultado = service.buscarSiglasSubordinadas("INEXISTENTE");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar sigla superior")
    void deveBuscarSiglaSuperior() {
        when(repo.buscarPorSigla(Unidade.class, unidadeIntermediaria.getSigla())).thenReturn(unidadeIntermediaria);

        Optional<String> superior = service.buscarSiglaSuperior(unidadeIntermediaria.getSigla());

        assertThat(superior).isPresent().contains(unidadeRaiz.getSigla());
    }

    @Test
    @DisplayName("Deve buscar subordinadas diretas")
    void deveBuscarSubordinadasDiretas() {
        when(unidadeRepo.findByUnidadeSuperiorCodigo(1L)).thenReturn(List.of(unidadeIntermediaria));

        List<UnidadeDto> result = service.buscarSubordinadas(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getSigla()).isEqualTo(unidadeIntermediaria.getSigla());
    }

    @Nested
    @DisplayName("Cobertura Adicional de Casos de Borda")
    class CoberturaAdicional {

        @Test
        @DisplayName("Deve retornar DTO sem subunidades quando lista de filhas é null")
        void deveRetornarDtoSemSubunidadesQuandoFilhasNull() {
            UnidadeDto dto = UnidadeDto.builder().codigo(1L).sigla("U1").build();
            Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>(); // Não tem a chave 1L
            
            UnidadeDto resultado = service.montarComSubunidades(dto, mapaFilhas);
            
            assertThat(resultado.getSubunidades()).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar IllegalStateException quando UnidadeDto.fromEntity retorna null")
        void deveLancarErroQuandoDtoNull() {
            // Simular repositório retornando lista com elemento nulo
            Unidade nullUnidade = null;
            when(unidadeRepo.findByUnidadeSuperiorCodigo(999L)).thenReturn(Collections.singletonList(nullUnidade));
            
            assertThatThrownBy(() -> service.buscarSubordinadas(999L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Unidade ausente");
        }

        @Test
        @DisplayName("Deve filtrar unidades na árvore de acordo com elegibilidade complexa")
        void deveFiltrarComElegibilidadeComplexa() {
            unidadeRaiz.setTipo(TipoUnidade.RAIZ);
            unidadeIntermediaria.setTipo(TipoUnidade.INTERMEDIARIA); // Deve ser filtrada
            unidadeOperacional.setTipo(TipoUnidade.OPERACIONAL);

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(unidadeRaiz, unidadeIntermediaria, unidadeOperacional));
            // Mockar que apenas a raiz tem mapa
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(unidadeRaiz.getCodigo()));
            
            // Caso 1: Requer mapa, mas só a raiz tem
            List<UnidadeDto> res1 = service.buscarArvoreComElegibilidade(true, Set.of());
            assertThat(res1.getFirst().isElegivel()).isTrue(); // RAIZ tem mapa
            UnidadeDto operacionalDto = res1.getFirst().getSubunidades().getFirst().getSubunidades().getFirst();
            assertThat(operacionalDto.isElegivel()).isFalse(); // Sem mapa

            // Caso 2: Bloqueada
            List<UnidadeDto> res2 = service.buscarArvoreComElegibilidade(false, Set.of(unidadeRaiz.getCodigo()));
            assertThat(res2.getFirst().isElegivel()).isFalse(); // Bloqueada
        }
    }
}

