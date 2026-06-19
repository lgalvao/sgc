package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ProcessoService Security Test suite")
class ProcessoServiceSecurityTest extends ProcessoServiceTestBase {

    @Nested
    @DisplayName("Segurança e Controle de Acesso")
    class SecurityTests {
        @Test
        @DisplayName("Deve negar acesso quando usuário não autenticado")
        void deveNegarAcessoQuandoNaoAutenticado() {
            Authentication auth = mock(Authentication.class);
            // Assume permissionEvaluator handles this
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }
    }

    @Nested
    @DisplayName("Checagem de Acesso")
    class ChecagemAcesso {
        @Test
        @DisplayName("Deve retornar false se auth for nulo ou invalido")
        void deveRetornarFalseSeAuthInvalido() {
            assertThat(processoService.checarAcesso(null, 1L)).isFalse();

            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();

            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(new Object());
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar true se ADMIN")
        void deveRetornarTrueSeAdmin() {
            Authentication auth = mock(Authentication.class);
            Usuario user = new Usuario();
            user.setPerfilAtivo(Perfil.ADMIN);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(user);

            assertThat(processoService.checarAcesso(auth, 1L)).isTrue();
        }

        @Test
        @DisplayName("Deve retornar true se unidade esta no processo para GESTOR/CHEFE")
        void deveRetornarTrueSeUnidadeNoProcesso() {
            Authentication auth = mock(Authentication.class);
            Usuario user = new Usuario();
            user.setPerfilAtivo(Perfil.CHEFE);
            user.setUnidadeAtivaCodigo(10L);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(user);
            when(consultaService.listarEntidadesPorProcessoEUnidades(1L, List.of(10L))).thenReturn(List.of(new Subprocesso()));

            assertThat(processoService.checarAcesso(auth, 1L)).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false se unidade nao possui subprocesso acessivel")
        void deveRetornarFalseSemSubprocessoAcessivel() {
            Authentication auth = mock(Authentication.class);
            Usuario user = new Usuario();
            user.setPerfilAtivo(Perfil.CHEFE);
            user.setUnidadeAtivaCodigo(10L);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(user);
            when(consultaService.listarEntidadesPorProcessoEUnidades(1L, List.of(10L))).thenReturn(List.of());

            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }
    }
}
