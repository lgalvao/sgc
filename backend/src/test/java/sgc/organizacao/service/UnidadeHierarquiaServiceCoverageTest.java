package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.organizacao.model.TipoUnidade.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnidadeHierarquiaService - Cobertura de Testes")
@SuppressWarnings("NullAway.Init")
class UnidadeHierarquiaServiceCoverageTest {

    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private UnidadeService unidadeService;

    @InjectMocks
    private UnidadeHierarquiaService target;

    @Test
    @DisplayName("Deve cobrir as linhas [48, 49, 50, 52, 53, 54, 55] do método buscarArvoreComElegibilidade")
    void deveCobrirBuscarArvoreComElegibilidade() {
        // Mocking para requerMapaVigente = true
        when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(10L));
        
        Unidade u1 = new Unidade();
        u1.setCodigo(10L);
        u1.setTipo(OPERACIONAL);
        u1.setSigla("U1");
        Responsabilidade responsabilidadeU1 = new Responsabilidade(10L, null, "111", null, null, null, null, new Usuario());
        responsabilidadeU1.getUsuario().setTituloEleitoral("111");
        u1.setResponsabilidade(responsabilidadeU1);
        
        Unidade u2 = new Unidade();
        u2.setCodigo(20L);
        u2.setTipo(INTERMEDIARIA);
        u2.setSigla("U2");

        Unidade u3 = new Unidade(); // Sem mapa
        u3.setCodigo(30L);
        u3.setTipo(OPERACIONAL);
        u3.setSigla("U3");
        Responsabilidade responsabilidadeU3 = new Responsabilidade(30L, null, "333", null, null, null, null, new Usuario());
        responsabilidadeU3.getUsuario().setTituloEleitoral("333");
        u3.setResponsabilidade(responsabilidadeU3);

        Unidade u4 = new Unidade(); // Bloqueada
        u4.setCodigo(40L);
        u4.setTipo(OPERACIONAL);
        u4.setSigla("U4");
        Responsabilidade responsabilidadeU4 = new Responsabilidade(40L, null, "444", null, null, null, null, new Usuario());
        responsabilidadeU4.getUsuario().setTituloEleitoral("444");
        u4.setResponsabilidade(responsabilidadeU4);

        when(unidadeRepo.listarTodasComHierarquia()).thenReturn(List.of(u1, u2, u3, u4));

        List<UnidadeDto> result = target.buscarArvoreComElegibilidade(true, Set.of(40L));
        
        assertThat(result).hasSize(4);
        // u1 (elegível), u2 (intermediária - false), u3 (sem mapa - false), u4 (bloqueada - false)
        assertThat(result.stream().filter(UnidadeDto::isElegivel).count()).isEqualTo(1);
        assertThat(result.stream().filter(UnidadeDto::isElegivel).findFirst().orElseThrow().getCodigo()).isEqualTo(10L);

        // Mocking para requerMapaVigente = false
        List<UnidadeDto> result2 = target.buscarArvoreComElegibilidade(false, Set.of(40L));
        assertThat(result2).hasSize(4);
        // u1 (elegível), u2 (intermediária - false), u3 (elegível), u4 (bloqueada - false)
        assertThat(result2.stream().filter(UnidadeDto::isElegivel).count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve cobrir as linhas [222] do método buscarNaHierarquiaPorSigla")
    void deveCobrirBuscarNaHierarquiaPorSigla() {
        Unidade uRaiz = new Unidade();
        uRaiz.setCodigo(1L);
        uRaiz.setSigla("RAIZ");
        uRaiz.setTipo(RAIZ);
        
        Unidade uSub = new Unidade();
        uSub.setCodigo(2L);
        uSub.setSigla("SUB");
        uSub.setTipo(OPERACIONAL);
        uSub.setUnidadeSuperior(uRaiz);
        
        when(unidadeRepo.listarTodasComHierarquia()).thenReturn(List.of(uRaiz, uSub));
        
        List<String> siglas = target.buscarSiglasSubordinadas("SUB");
        assertThat(siglas).contains("SUB");
        
        List<String> siglasRaiz = target.buscarSiglasSubordinadas("RAIZ");
        assertThat(siglasRaiz).contains("RAIZ", "SUB");
    }

    @Test
    @DisplayName("buscarArvoreHierarquica deve chamar montarHierarquia com elegibilidadeChecker null")
    void deveChamarMontarHierarquiaComNullChecker() {
        Unidade u1 = new Unidade();
        u1.setCodigo(10L);
        u1.setTipo(OPERACIONAL);
        u1.setSigla("U1");
        
        when(unidadeRepo.listarTodasComHierarquia()).thenReturn(List.of(u1));
        
        List<UnidadeDto> result = target.buscarArvoreHierarquica();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().isElegivel()).isTrue();
    }

    @Test
    @DisplayName("montarHierarquia deve ignorar unidade quando não encontrar DTO no mapa")
    void deveIgnorarUnidadeQuandoDtoNaoEncontradoNoMapa() {
        Unidade unidadeMutavel = mock(Unidade.class);
        when(unidadeMutavel.getCodigo()).thenReturn(10L, 10L, 11L);
        when(unidadeMutavel.getNome()).thenReturn("Unidade Mutável");
        when(unidadeMutavel.getSigla()).thenReturn("UM");
        when(unidadeMutavel.getTipo()).thenReturn(OPERACIONAL);
        when(unidadeMutavel.getUnidadeSuperior()).thenReturn(null);
        when(unidadeRepo.listarTodasComHierarquia()).thenReturn(List.of(unidadeMutavel));

        List<UnidadeDto> resultado = target.buscarArvoreHierarquica();

        assertThat(resultado).isEmpty();
    }
}
