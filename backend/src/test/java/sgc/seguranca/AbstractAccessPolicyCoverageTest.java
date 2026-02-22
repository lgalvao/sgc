package sgc.seguranca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfilRepo;
import sgc.organizacao.service.HierarquiaService;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("AbstractAccessPolicy - Cobertura Adicional")
class AbstractAccessPolicyCoverageTest {

    @Mock
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Mock
    private HierarquiaService hierarquiaService;

    private TestAccessPolicy policy;

    static class TestAccessPolicy extends AbstractAccessPolicy<Object> {
        public TestAccessPolicy(UsuarioPerfilRepo usuarioPerfilRepo, HierarquiaService hierarquiaService) {
            super(usuarioPerfilRepo, hierarquiaService);
        }

        @Override
        public boolean canExecute(Usuario usuario, Acao acao, Object recurso) {
            return false;
        }

        public boolean callVerificaHierarquia(Usuario usuario, Unidade unidade, RequisitoHierarquia requisito) {
            return verificaHierarquia(usuario, unidade, requisito);
        }
    }

    @BeforeEach
    void setUp() {
        policy = new TestAccessPolicy(usuarioPerfilRepo, hierarquiaService);
    }

    @Test
    @DisplayName("verificaHierarquia deve retornar true para ADMIN quando requisito não é TITULAR_UNIDADE nem MESMA_UNIDADE")
    void deveRetornarTrueParaAdmin() {
        // Arrange
        Usuario admin = new Usuario();
        admin.setPerfilAtivo(Perfil.ADMIN);
        admin.setUnidadeAtivaCodigo(100L);
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);

        // Act & Assert
        // MESMA_OU_SUBORDINADA deve ser bypassado pelo ADMIN
        assertThat(policy.callVerificaHierarquia(admin, unidade, AbstractAccessPolicy.RequisitoHierarquia.MESMA_OU_SUBORDINADA)).isTrue();
        
        // MESMA_UNIDADE NÃO deve ser bypassado
        assertThat(policy.callVerificaHierarquia(admin, unidade, AbstractAccessPolicy.RequisitoHierarquia.MESMA_UNIDADE)).isFalse();
    }

    @Test
    @DisplayName("verificaHierarquia deve retornar true para requisito NENHUM")
    void deveRetornarTrueParaRequisitoNenhum() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.SERVIDOR);
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);

        // Act & Assert
        assertThat(policy.callVerificaHierarquia(usuario, unidade, AbstractAccessPolicy.RequisitoHierarquia.NENHUM)).isTrue();
    }

    @Test
    @DisplayName("verificaHierarquia deve respeitar TITULAR_UNIDADE mesmo para ADMIN")
    void deveVerificarTitularUnidadeParaAdmin() {
        // Arrange
        Usuario admin = new Usuario();
        admin.setPerfilAtivo(Perfil.ADMIN);
        admin.setTituloEleitoral("111111");
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setTituloTitular("222222"); // Outro titular

        // Act & Assert
        org.mockito.Mockito.when(hierarquiaService.isResponsavel(unidade, admin)).thenReturn(false);
        assertThat(policy.callVerificaHierarquia(admin, unidade, AbstractAccessPolicy.RequisitoHierarquia.TITULAR_UNIDADE)).isFalse();
        
        org.mockito.Mockito.when(hierarquiaService.isResponsavel(unidade, admin)).thenReturn(true);
        assertThat(policy.callVerificaHierarquia(admin, unidade, AbstractAccessPolicy.RequisitoHierarquia.TITULAR_UNIDADE)).isTrue();
    }
}
