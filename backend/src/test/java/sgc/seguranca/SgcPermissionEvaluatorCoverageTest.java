package sgc.seguranca;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.security.core.Authentication;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.HierarquiaService;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SgcPermissionEvaluator - Cobertura adicional")
class SgcPermissionEvaluatorCoverageTest {

    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private ProcessoRepo processoRepo;
    @Mock private MapaRepo mapaRepo;
    @Mock private AtividadeRepo atividadeRepo;
    @Mock private HierarquiaService hierarquiaService;
    @Mock private Authentication authentication;

    @InjectMocks
    private SgcPermissionEvaluator evaluator;

    @Test
    @DisplayName("hasPermission: Deve retornar false se principal não for Usuario")
    void principalNaoUsuario() {
        when(authentication.getPrincipal()).thenReturn("not-a-user");
        boolean result = evaluator.hasPermission(authentication, 1L, "Subprocesso", "VISUALIZAR_SUBPROCESSO");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verificarHierarquia: Deve cobrir log de acesso negado (perfil nulo ou inesperado)")
    void perfilNulo() {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getPerfilAtivo()).thenReturn(null);
        when(usuario.getTituloEleitoral()).thenReturn("123");
        
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(Processo.builder().situacao(SituacaoProcesso.EM_ANDAMENTO).build());
        sp.setUnidade(Unidade.builder().codigo(10L).build());

        // Usando reflexão para chamar o método privado verificarHierarquia
        // Ou chamando um método público que chegue lá
        boolean result = evaluator.verificarPermissao(usuario, sp, AcaoPermissao.VISUALIZAR_SUBPROCESSO);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("hasPermission: Deve lidar com tipo de alvo desconhecido")
    void tipoAlvoDesconhecido() {
        Usuario usuario = mock(Usuario.class);
        when(authentication.getPrincipal()).thenReturn(usuario);
        boolean result = evaluator.hasPermission(authentication, 1L, "TipoInexistente", "VISUALIZAR_SUBPROCESSO");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verificarPermissao: Deve retornar false para alvo nulo")
    void alvoNulo() {
        Usuario usuario = new Usuario();
        boolean result = evaluator.verificarPermissao(usuario, null, AcaoPermissao.VISUALIZAR_SUBPROCESSO);
        assertThat(result).isFalse();
    }
}
