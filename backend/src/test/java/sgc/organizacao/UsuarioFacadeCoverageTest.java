package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.comum.repo.RepositorioComum;
import sgc.organizacao.model.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para UsuarioFacade")
class UsuarioFacadeCoverageTest {

    @InjectMocks
    private UsuarioFacade usuarioFacade;

    @Mock private UsuarioRepo usuarioRepo;
    @Mock private UsuarioPerfilRepo usuarioPerfilRepo;
    @Mock private AdministradorRepo administradorRepo;
    @Mock private RepositorioComum repo;
    @Mock private UnidadeRepo unidadeRepo;

    @Test
    @DisplayName("Deve retornar vazio se não houver autenticação ou for anônima")
    void deveRetornarVazioSeSemAutenticacao() {
        SecurityContext ctx = mock(SecurityContext.class);
        SecurityContextHolder.setContext(ctx);

        // Caso 1: authentication null
        when(ctx.getAuthentication()).thenReturn(null);
        assertThat(usuarioFacade.obterUsuarioAutenticadoOuNull()).isNull();

        // Caso 2: authentication nao autenticada
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(ctx.getAuthentication()).thenReturn(auth);
        assertThat(usuarioFacade.obterUsuarioAutenticadoOuNull()).isNull();

        // Caso 3: AnonymousAuthenticationToken
        AnonymousAuthenticationToken anon = mock(AnonymousAuthenticationToken.class);
        when(anon.isAuthenticated()).thenReturn(true);
        when(ctx.getAuthentication()).thenReturn(anon);
        assertThat(usuarioFacade.obterUsuarioAutenticadoOuNull()).isNull();
    }

    @Test
    @DisplayName("Deve retornar lista vazia se usuário não encontrado ao buscar unidades de responsabilidade")
    void deveRetornarVaziaSeUsuarioNaoEncontradoNoResponsavel() {
        when(usuarioRepo.findByIdWithAtribuicoes("T")).thenReturn(Optional.empty());
        assertThat(usuarioFacade.buscarUnidadesOndeEhResponsavel("T")).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar nulo se principal for desconhecido")
    void deveExtrairTituloUsuarioDesconhecido() {
        assertThat(usuarioFacade.extrairTituloUsuario(null)).isNull();
        assertThat(usuarioFacade.extrairTituloUsuario(123)).isEqualTo("123");
    }

    @Test
    @DisplayName("Deve buscar por id delegando para repositório comum")
    void deveBuscarPorId() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        when(repo.buscar(Usuario.class, "T")).thenReturn(u);
        
        Usuario result = usuarioFacade.buscarPorId("T");
        assertThat(result.getTituloEleitoral()).isEqualTo("T");
    }
}
