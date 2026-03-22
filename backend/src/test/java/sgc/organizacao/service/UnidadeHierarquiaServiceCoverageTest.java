package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.model.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.organizacao.model.TipoUnidade.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnidadeHierarquiaService - Cobertura de Testes")
class UnidadeHierarquiaServiceCoverageTest {

    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private ComumRepo repo;

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
        
        Unidade u2 = new Unidade();
        u2.setCodigo(20L);
        u2.setTipo(INTERMEDIARIA);
        u2.setSigla("U2");

        Unidade u3 = new Unidade(); // Sem mapa
        u3.setCodigo(30L);
        u3.setTipo(OPERACIONAL);
        u3.setSigla("U3");

        Unidade u4 = new Unidade(); // Bloqueada
        u4.setCodigo(40L);
        u4.setTipo(OPERACIONAL);
        u4.setSigla("U4");

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1, u2, u3, u4));

        List<UnidadeDto> result = target.buscarArvoreComElegibilidade(true, Set.of(40L));
        
        assertThat(result).hasSize(4);
        // u1 (elegível), u2 (intermediária - false), u3 (sem mapa - false), u4 (bloqueada - false)
        assertThat(result.stream().filter(UnidadeDto::isElegivel).count()).isEqualTo(1);
        assertThat(result.stream().filter(UnidadeDto::isElegivel).findFirst().get().getCodigo()).isEqualTo(10L);

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
        
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(uRaiz, uSub));
        
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
        
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
        
        List<UnidadeDto> result = target.buscarArvoreHierarquica();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isElegivel()).isTrue();
    }
}
