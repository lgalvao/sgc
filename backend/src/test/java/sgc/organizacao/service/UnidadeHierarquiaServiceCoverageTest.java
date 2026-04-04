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

    @Mock
    private ResponsabilidadeRepo responsabilidadeRepo;

    @InjectMocks
    private UnidadeHierarquiaService target;

    @Test
    @DisplayName("Deve cobrir as linhas [48, 49, 50, 52, 53, 54, 55] do método buscarArvoreComElegibilidade")
    void deveCobrirBuscarArvoreComElegibilidade() {
        // Mocking para requerMapaVigente = true
        when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(10L));
        
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(List.of(
                new UnidadeHierarquiaLeitura(10L, "U1", "U1", null, OPERACIONAL, SituacaoUnidade.ATIVA, null),
                new UnidadeHierarquiaLeitura(20L, "U2", "U2", null, INTERMEDIARIA, SituacaoUnidade.ATIVA, null),
                new UnidadeHierarquiaLeitura(30L, "U3", "U3", null, OPERACIONAL, SituacaoUnidade.ATIVA, null),
                new UnidadeHierarquiaLeitura(40L, "U4", "U4", null, OPERACIONAL, SituacaoUnidade.ATIVA, null)
        ));
        when(responsabilidadeRepo.listarLeiturasPorCodigosUnidade(List.of(10L, 20L, 30L, 40L))).thenReturn(List.of(
                new ResponsabilidadeLeitura(10L, "111"),
                new ResponsabilidadeLeitura(30L, "333"),
                new ResponsabilidadeLeitura(40L, "444")
        ));

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
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(List.of(
                new UnidadeHierarquiaLeitura(1L, "RAIZ", "RAIZ", null, RAIZ, SituacaoUnidade.ATIVA, null),
                new UnidadeHierarquiaLeitura(2L, "SUB", "SUB", null, OPERACIONAL, SituacaoUnidade.ATIVA, 1L)
        ));
        
        List<String> siglas = target.buscarSiglasSubordinadas("SUB");
        assertThat(siglas).contains("SUB");
        
        List<String> siglasRaiz = target.buscarSiglasSubordinadas("RAIZ");
        assertThat(siglasRaiz).contains("RAIZ", "SUB");
    }

    @Test
    @DisplayName("buscarArvoreHierarquica deve chamar montarHierarquia com elegibilidadeChecker null")
    void deveChamarMontarHierarquiaComNullChecker() {
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(List.of(
                new UnidadeHierarquiaLeitura(10L, "U1", "U1", null, OPERACIONAL, SituacaoUnidade.ATIVA, null)
        ));
        
        List<UnidadeDto> result = target.buscarArvoreHierarquica();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().isElegivel()).isTrue();
    }

    @Test
    @DisplayName("montarHierarquia deve ignorar unidade quando não encontrar DTO no mapa")
    void deveIgnorarUnidadeQuandoDtoNaoEncontradoNoMapa() {
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(List.of(
                new UnidadeHierarquiaLeitura(10L, "Unidade Mutável", "UM", null, OPERACIONAL, SituacaoUnidade.ATIVA, 11L)
        ));

        List<UnidadeDto> resultado = target.buscarArvoreHierarquica();

        assertThat(resultado).isEmpty();
    }
}
