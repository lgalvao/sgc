package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.beans.factory.*;
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
    private UnidadeService unidadeService;

    @Mock
    private ResponsabilidadeRepo responsabilidadeRepo;

    @Mock
    private ObjectProvider<UnidadeHierarquiaService> selfProvider;

    @InjectMocks
    private UnidadeHierarquiaService service;

    private Unidade unidadeRaiz;
    private Unidade unidadeIntermediaria;
    private Unidade unidadeOperacional;

    @BeforeEach
    void setUp() {
        unidadeRaiz = UnidadeTestBuilder.raiz().build();
        unidadeRaiz.setCodigo(1L);
        unidadeRaiz.setResponsabilidade(criarResponsabilidade(1L));

        unidadeIntermediaria = UnidadeTestBuilder.intermediaria()
                .comSuperior(unidadeRaiz)
                .build();
        unidadeIntermediaria.setCodigo(2L);
        unidadeIntermediaria.setResponsabilidade(criarResponsabilidade(2L));

        unidadeOperacional = UnidadeTestBuilder.operacional()
                .comSuperior(unidadeIntermediaria)
                .build();
        unidadeOperacional.setCodigo(3L);
        unidadeOperacional.setResponsabilidade(criarResponsabilidade(3L));
    }

    @Test
    @DisplayName("Deve buscar árvore hierárquica completa")
    void deveBuscarArvoreHierarquica() {
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(hierarquiaBasica());

        List<UnidadeDto> resultado = service.buscarArvoreHierarquica();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getSigla()).isEqualTo(unidadeRaiz.getSigla());
        assertThat(resultado.getFirst().getSubunidades()).hasSize(1);
        assertThat(resultado.getFirst().getSubunidades().getFirst().getSigla()).isEqualTo(unidadeIntermediaria.getSigla());
    }

    @Test
    @DisplayName("Deve buscar árvore com elegibilidade")
    void deveBuscarComElegibilidade() {
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(hierarquiaBasica());
        when(responsabilidadeRepo.listarLeiturasPorCodigosUnidade(List.of(1L, 2L, 3L)))
                .thenReturn(responsabilidadesBasicas());

        Predicate<UnidadeElegibilidadeInfo> soOperacional = info -> info.codigo().equals(3L);
        List<UnidadeDto> resultado = service.buscarArvoreComElegibilidade(soOperacional);

        assertThat(resultado).hasSize(1);

        assertThat(resultado.getFirst().isElegivel()).isFalse();

        UnidadeDto operacionalDto = resultado.getFirst().getSubunidades().getFirst().getSubunidades().getFirst();
        assertThat(operacionalDto.isElegivel()).isTrue();
    }

    @Test
    @DisplayName("Deve buscar IDs descendentes")
    void deveBuscarIdsDescendentes() {
        when(selfProvider.getObject()).thenReturn(service);
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(hierarquiaBasica());

        List<Long> descendentes = service.buscarIdsDescendentes(1L);

        assertThat(descendentes).containsExactlyInAnyOrder(2L, 3L);
    }

    @Test
    @DisplayName("Deve buscar árvore por código (nível profundo)")
    void deveBuscarArvorePorCodigo() {
        when(selfProvider.getObject()).thenReturn(service);
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(hierarquiaBasica());

        UnidadeDto resultado = service.buscarArvore(3L);

        assertThat(resultado.getSigla()).isEqualTo(unidadeOperacional.getSigla());
    }

    @Test
    @DisplayName("Deve buscar siglas subordinadas (a partir da raiz)")
    void deveBuscarSiglasSubordinadas() {
        when(selfProvider.getObject()).thenReturn(service);
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(hierarquiaBasica());

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
        when(selfProvider.getObject()).thenReturn(service);
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(List.of(toLeitura(unidadeRaiz)));
        Unidade extra = Unidade.builder()
                .codigo(99L)
                .nome("Unidade Extra")
                .sigla("EXTRA")
                .tipo(TipoUnidade.OPERACIONAL)
                .build();
        when(unidadeService.buscarPorCodigo(99L)).thenReturn(extra);

        UnidadeDto resultado = service.buscarArvore(99L);

        assertThat(resultado.getSigla()).isEqualTo("EXTRA");
    }

    @Test
    @DisplayName("buscarSiglasSubordinadas deve retornar vazio se não encontrar sigla")
    void buscarSiglasSubordinadas_DeveRetornarVazioSeNaoEncontrar() {
        when(selfProvider.getObject()).thenReturn(service);
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(List.of(toLeitura(unidadeRaiz)));
        Unidade extra = Unidade.builder()
                .codigo(999L)
                .nome("Unidade Inexistente")
                .sigla("INEXISTENTE")
                .tipo(TipoUnidade.OPERACIONAL)
                .build();
        when(unidadeService.buscarPorSigla("INEXISTENTE")).thenReturn(extra);

        List<String> resultado = service.buscarSiglasSubordinadas("INEXISTENTE");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar sigla superior")
    void deveBuscarSiglaSuperior() {
        when(selfProvider.getObject()).thenReturn(service);
        when(unidadeService.buscarPorSigla(unidadeIntermediaria.getSigla())).thenReturn(unidadeIntermediaria);
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(hierarquiaBasica());
        when(unidadeRepo.buscarSiglaPorCodigo(unidadeRaiz.getCodigo()))
                .thenReturn(Optional.of(unidadeRaiz.getSigla()));

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

    @Test
    @DisplayName("buscarCodigosSuperiores deve usar apenas o mapa filho pai")
    void buscarCodigosSuperiores_DeveUsarApenasMapaFilhoPai() {
        when(selfProvider.getObject()).thenReturn(service);
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(hierarquiaBasica());

        List<Long> resultado = service.buscarCodigosSuperiores(3L);

        assertThat(resultado).containsExactly(2L, 1L);
        verifyNoInteractions(unidadeService);
    }

    @Test
    @DisplayName("buscarCodigoPai deve retornar vazio quando relacao nao existe no mapa")
    void buscarCodigoPai_DeveRetornarNullQuandoRelacaoNaoExisteNoMapa() {
        when(selfProvider.getObject()).thenReturn(service);
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(List.of(toLeitura(unidadeRaiz)));

        Long resultado = service.buscarCodigoPai(99L);

        assertThat(resultado).isNull();
        verifyNoInteractions(unidadeService);
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
            List<Unidade> subordinadas = new ArrayList<>();
            subordinadas.add(null);
            when(unidadeRepo.findByUnidadeSuperiorCodigo(999L)).thenReturn(subordinadas);
            
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

            when(unidadeRepo.listarEstruturasAtivas()).thenReturn(hierarquiaBasica());
            when(responsabilidadeRepo.listarLeiturasPorCodigosUnidade(List.of(1L, 2L, 3L)))
                    .thenReturn(responsabilidadesBasicas());
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

        @Test
        @DisplayName("Deve marcar como inelegível unidade sem responsável efetivo")
        void deveMarcarInelegivelSemResponsavelEfetivo() {
            unidadeOperacional.setResponsabilidade(null);
            when(unidadeRepo.listarEstruturasAtivas()).thenReturn(hierarquiaBasica());
            when(responsabilidadeRepo.listarLeiturasPorCodigosUnidade(List.of(1L, 2L, 3L)))
                    .thenReturn(List.of(
                            new ResponsabilidadeLeitura(1L, "RESP-1"),
                            new ResponsabilidadeLeitura(2L, "RESP-2")
                    ));

            List<UnidadeDto> resultado = service.buscarArvoreComElegibilidade(false, Set.of());

            UnidadeDto operacionalDto = resultado.getFirst().getSubunidades().getFirst().getSubunidades().getFirst();
            assertThat(operacionalDto.isElegivel()).isFalse();
        }

        @Test
        @DisplayName("Deve marcar como inelegível unidade com responsável em branco")
        void deveMarcarInelegivelComResponsavelEmBranco() {
            Responsabilidade responsabilidade = criarResponsabilidade(3L);
            responsabilidade.setUsuarioTitulo("   ");
            unidadeOperacional.setResponsabilidade(responsabilidade);

            when(unidadeRepo.listarEstruturasAtivas()).thenReturn(hierarquiaBasica());
            when(responsabilidadeRepo.listarLeiturasPorCodigosUnidade(List.of(1L, 2L, 3L)))
                    .thenReturn(List.of(
                            new ResponsabilidadeLeitura(1L, "RESP-1"),
                            new ResponsabilidadeLeitura(2L, "RESP-2"),
                            new ResponsabilidadeLeitura(3L, "   ")
                    ));

            List<UnidadeDto> resultado = service.buscarArvoreComElegibilidade(false, Set.of());

            UnidadeDto operacionalDto = resultado.getFirst().getSubunidades().getFirst().getSubunidades().getFirst();
            assertThat(operacionalDto.isElegivel()).isFalse();
        }
    }

    private Responsabilidade criarResponsabilidade(Long codigoUnidade) {
        Responsabilidade responsabilidade = new Responsabilidade();
        responsabilidade.setUnidadeCodigo(codigoUnidade);
        responsabilidade.setUsuarioTitulo("RESP-" + codigoUnidade);
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("RESP-" + codigoUnidade);
        usuario.setNome("Responsável " + codigoUnidade);
        responsabilidade.setUsuario(usuario);
        return responsabilidade;
    }

    private List<UnidadeHierarquiaLeitura> hierarquiaBasica() {
        return List.of(
                toLeitura(unidadeRaiz),
                toLeitura(unidadeIntermediaria),
                toLeitura(unidadeOperacional)
        );
    }

    private List<ResponsabilidadeLeitura> responsabilidadesBasicas() {
        return List.of(
                new ResponsabilidadeLeitura(1L, "RESP-1"),
                new ResponsabilidadeLeitura(2L, "RESP-2"),
                new ResponsabilidadeLeitura(3L, "RESP-3")
        );
    }

    private UnidadeHierarquiaLeitura toLeitura(Unidade unidade) {
        return new UnidadeHierarquiaLeitura(
                unidade.getCodigo(),
                unidade.getNome(),
                unidade.getSigla(),
                unidade.getTituloTitular(),
                unidade.getTipo(),
                unidade.getSituacao(),
                unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null
        );
    }
}
