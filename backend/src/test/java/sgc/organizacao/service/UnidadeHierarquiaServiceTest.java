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
class UnidadeHierarquiaServiceTest {

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private ComumRepo repo;

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
        // A raiz não é elegível mas aparece na árvore
        assertThat(resultado.getFirst().isElegivel()).isFalse();
        // A operacional é elegível
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
}
