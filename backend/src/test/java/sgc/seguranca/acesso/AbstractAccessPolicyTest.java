package sgc.seguranca.acesso;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.*;
import sgc.organizacao.service.HierarquiaService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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

        // Expose protected methods for testing
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
        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.GESTOR);
        
        when(usuarioPerfilRepo.findByUsuarioTitulo("123")).thenReturn(List.of(up));

        boolean result = policy.testTemPerfilPermitido(usuario, EnumSet.of(Perfil.GESTOR, Perfil.ADMIN));
        assertTrue(result);
    }

    @Test
    @DisplayName("Deve retornar false se usuário não tem perfil permitido")
    void deveRetornarFalseSeNaoTemPerfil() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.SERVIDOR);
        
        when(usuarioPerfilRepo.findByUsuarioTitulo("123")).thenReturn(List.of(up));

        boolean result = policy.testTemPerfilPermitido(usuario, EnumSet.of(Perfil.GESTOR, Perfil.ADMIN));
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Deve considerar atribuições temporárias")
    void deveConsiderarAtribuicoesTemporarias() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        AtribuicaoTemporaria atribuicao = new AtribuicaoTemporaria();
        atribuicao.setPerfil(Perfil.ADMIN);
        
        // Setup temporaria valida
        atribuicao.setDataInicio(LocalDateTime.now().minusDays(1));
        atribuicao.setDataTermino(LocalDateTime.now().plusDays(1));
        
        // Mock unidade para atribuicao temporaria
        Unidade u = new Unidade();
        u.setCodigo(1L);
        atribuicao.setUnidade(u);
        
        usuario.setAtribuicoesTemporarias(new HashSet<>(Collections.singletonList(atribuicao)));
        
        when(usuarioPerfilRepo.findByUsuarioTitulo("123")).thenReturn(Collections.emptyList());

        boolean result = policy.testTemPerfilPermitido(usuario, EnumSet.of(Perfil.ADMIN));
        assertTrue(result);
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
}