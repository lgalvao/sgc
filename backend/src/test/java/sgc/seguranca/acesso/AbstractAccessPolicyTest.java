package sgc.seguranca.acesso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.organizacao.model.AtribuicaoTemporaria;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AbstractAccessPolicyTest {

    // Concrete implementation for testing
    static class TestAccessPolicy extends AbstractAccessPolicy<Object> {
        @Override
        public boolean canExecute(Usuario usuario, Acao acao, Object recurso) {
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

    private final TestAccessPolicy policy = new TestAccessPolicy();

    @Test
    @DisplayName("Deve retornar true se usuário tem perfil permitido")
    void deveRetornarTrueSeTemPerfil() {
        Usuario usuario = new Usuario();
        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.GESTOR);
        usuario.setAtribuicoesPermanentes(new HashSet<>(Collections.singletonList(up)));

        boolean result = policy.testTemPerfilPermitido(usuario, EnumSet.of(Perfil.GESTOR, Perfil.ADMIN));
        assertTrue(result);
    }

    @Test
    @DisplayName("Deve retornar false se usuário não tem perfil permitido")
    void deveRetornarFalseSeNaoTemPerfil() {
        Usuario usuario = new Usuario();
        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.SERVIDOR);
        usuario.setAtribuicoesPermanentes(new HashSet<>(Collections.singletonList(up)));

        boolean result = policy.testTemPerfilPermitido(usuario, EnumSet.of(Perfil.GESTOR, Perfil.ADMIN));
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Deve considerar atribuições temporárias")
    void deveConsiderarAtribuicoesTemporarias() {
        Usuario usuario = new Usuario();
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