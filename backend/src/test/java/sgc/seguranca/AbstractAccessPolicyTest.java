package sgc.seguranca;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfilRepo;
import sgc.organizacao.service.HierarquiaService;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AbstractAccessPolicyTest {

    @Mock
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Mock
    private HierarquiaService hierarquiaService;

    // Concrete implementation for testing
    static class TestAccessPolicy extends AbstractAccessPolicy<Object> {
        public TestAccessPolicy(UsuarioPerfilRepo usuarioPerfilRepo, HierarquiaService hierarquiaService) {
            super(usuarioPerfilRepo, hierarquiaService);
        }
        
        @Override
        public boolean canExecute(@NonNull Usuario usuario, @NonNull Acao acao, Object recurso) {
            return false;
        }

        public boolean testTemPerfilPermitido(Usuario usuario, EnumSet<Perfil> perfis) {
            return temPerfilPermitido(usuario, perfis);
        }

        public void testDefinirMotivoNegacao(Usuario usuario, EnumSet<Perfil> perfis, Acao acao) {
            definirMotivoNegacao(usuario, perfis, acao);
        }

        public void testDefinirMotivoNegacao(String motivo) {
            definirMotivoNegacao(motivo);
        }
        
        public String testFormatarPerfis(EnumSet<Perfil> perfis) {
            return formatarPerfis(perfis);
        }

        public String testObterMotivoNegacaoHierarquia(Usuario usuario, Unidade unidade, AbstractAccessPolicy.RequisitoHierarquia requisito) {
            return obterMotivoNegacaoHierarquia(usuario, unidade, requisito);
        }
    }

    private TestAccessPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new TestAccessPolicy(usuarioPerfilRepo, hierarquiaService);
    }

    @Test
    @DisplayName("Deve retornar true se usuário tem perfil permitido")
    void deveRetornarTrueSeTemPerfil() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        usuario.setPerfilAtivo(Perfil.GESTOR);

        boolean result = policy.testTemPerfilPermitido(usuario, EnumSet.of(Perfil.GESTOR, Perfil.ADMIN));
        assertTrue(result);
    }

    @Test
    @DisplayName("Deve retornar false se usuário não tem perfil permitido")
    void deveRetornarFalseSeNaoTemPerfil() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        usuario.setPerfilAtivo(Perfil.SERVIDOR);

        boolean result = policy.testTemPerfilPermitido(usuario, EnumSet.of(Perfil.GESTOR, Perfil.ADMIN));
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Deve formatar perfis corretamente")
    void deveFormatarPerfis() {
        String result = policy.testFormatarPerfis(EnumSet.of(Perfil.ADMIN, Perfil.GESTOR));
        assertTrue(result.contains("ADMIN"));
        assertTrue(result.contains("GESTOR"));
    }

    @Test
    @DisplayName("Deve definir motivo de negação formatado")
    void deveDefinirMotivoNegacaoFormatado() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("12345");
        policy.testDefinirMotivoNegacao(usuario, EnumSet.of(Perfil.ADMIN), Acao.CRIAR_PROCESSO);
        
        String motivo = policy.getMotivoNegacao();
        assertNotNull(motivo);
        assertTrue(motivo.contains("12345"));
        assertTrue(motivo.contains("ADMIN"));
        assertTrue(motivo.contains(Acao.CRIAR_PROCESSO.getDescricao()));
    }

    @Test
    @DisplayName("Deve definir motivo de negação customizado")
    void deveDefinirMotivoNegacaoCustom() {
        String custom = "Erro customizado";
        policy.testDefinirMotivoNegacao(custom);
        assertEquals(custom, policy.getMotivoNegacao());
    }

    @Test
    @DisplayName("Deve retornar motivo genérico para requisito de hierarquia NENHUM")
    void deveRetornarMotivoGenericoParaHierarquiaNenhum() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("12345");
        Unidade unidade = new Unidade();
        unidade.setSigla("UNITESTE");

        String result = policy.testObterMotivoNegacaoHierarquia(usuario, unidade, AbstractAccessPolicy.RequisitoHierarquia.NENHUM);
        assertEquals("Erro inesperado na verificação de hierarquia", result);
    }
}