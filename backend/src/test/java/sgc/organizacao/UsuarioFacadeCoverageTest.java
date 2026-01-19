package sgc.organizacao;

import org.junit.jupiter.api.AfterEach;
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
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.repo.RepositorioComum;
import sgc.organizacao.dto.ResponsavelDto;
import sgc.organizacao.model.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class UsuarioFacadeCoverageTest {

    @InjectMocks
    private UsuarioFacade facade;

    @Mock private UsuarioRepo usuarioRepo;
    @Mock private UsuarioPerfilRepo usuarioPerfilRepo;
    @Mock private AdministradorRepo administradorRepo;
    @Mock private UnidadeRepo unidadeRepo;
    @Mock private RepositorioComum repo;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void buscarPorLogin_DeveChamarBuscarPorLoginInterno() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");

        when(usuarioRepo.findByIdWithAtribuicoes("123")).thenReturn(Optional.of(usuario));
        when(usuarioPerfilRepo.findByUsuarioTitulo("123")).thenReturn(Collections.emptyList());

        Usuario result = facade.buscarPorLogin("123");
        assertEquals("123", result.getTituloEleitoral());
    }

    @Test
    void obterUsuarioAutenticado_DeveLancarErroSeNaoAutenticado() {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThrows(ErroAccessoNegado.class, () -> facade.obterUsuarioAutenticado());
    }

    @Test
    void obterUsuarioAutenticado_DeveLancarErroSeAnonymous() {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        Authentication auth = mock(AnonymousAuthenticationToken.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        assertThrows(ErroAccessoNegado.class, () -> facade.obterUsuarioAutenticado());
    }

    @Test
    void buscarResponsavelUnidade_DeveLancarErroSeSemChefes() {
        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L))).thenReturn(Collections.emptyList());

        assertThrows(ErroEntidadeNaoEncontrada.class, () -> facade.buscarResponsavelUnidade(1L));
    }

    @Test
    void buscarResponsaveisUnidades_DeveRetornarVazioSeSemChefes() {
        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L))).thenReturn(Collections.emptyList());

        Map<Long, ResponsavelDto> result = facade.buscarResponsaveisUnidades(List.of(1L));
        assertTrue(result.isEmpty());
    }

    @Test
    void buscarResponsaveisUnidades_DeveRetornarVazioSeListaVazia() {
        Map<Long, ResponsavelDto> result = facade.buscarResponsaveisUnidades(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void buscarPorId_DeveChamarRepo() {
        Usuario u = new Usuario();
        when(repo.buscar(Usuario.class, "123")).thenReturn(u);
        assertEquals(u, facade.buscarPorId("123"));
    }
}
