package sgc.seguranca.login;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroAutenticacao;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.seguranca.login.dto.EntrarRequest;
import sgc.seguranca.login.dto.PerfilUnidadeDto;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginFacadeCoverageTest")
class LoginFacadeCoverageTest {

    @Mock private UsuarioFacade usuarioService;
    @Mock private GerenciadorJwt gerenciadorJwt;
    @Mock private ClienteAcessoAd clienteAcessoAd;
    @Mock private UnidadeFacade unidadeService;

    @InjectMocks
    private LoginFacade facade;

    @Test
    @DisplayName("buscarAutorizacoesInterno - Usuario Nao Encontrado")
    void buscarAutorizacoesInterno_UsuarioNaoEncontrado() {
        String titulo = "123";
        // Setup authentication state
        when(clienteAcessoAd.autenticar(any(), any())).thenReturn(true);
        facade.autenticar(titulo, "pass");

        // Mock user not found
        when(usuarioService.carregarUsuarioParaAutenticacao(titulo)).thenReturn(null);

        assertThrows(ErroEntidadeNaoEncontrada.class, () -> facade.autorizar(titulo));
    }

    @Test
    @DisplayName("toUnidadeDto - Coverage for Branches")
    void toUnidadeDto_Coverage() {
        String titulo = "123";
        when(clienteAcessoAd.autenticar(any(), any())).thenReturn(true);
        facade.autenticar(titulo, "pass");

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);

        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setTipo(TipoUnidade.OPERACIONAL);

        Unidade u2 = new Unidade();
        u2.setCodigo(2L);
        u2.setUnidadeSuperior(u1);
        u2.setTipo(TipoUnidade.INTERMEDIARIA);

        UsuarioPerfil p1 = new UsuarioPerfil();
        p1.setPerfil(Perfil.SERVIDOR);
        p1.setUnidade(u1);
        p1.setUnidadeCodigo(1L);

        UsuarioPerfil p2 = new UsuarioPerfil();
        p2.setPerfil(Perfil.SERVIDOR);
        p2.setUnidade(u2);
        p2.setUnidadeCodigo(2L);

        usuario.setAtribuicoes(Set.of(p1, p2));

        when(usuarioService.carregarUsuarioParaAutenticacao(titulo)).thenReturn(usuario);

        List<PerfilUnidadeDto> result = facade.autorizar(titulo);

        assertEquals(2, result.size());

        UnidadeDto dto1 = result.stream().filter(r -> r.getUnidade().getCodigo().equals(1L)).findFirst().get().getUnidade();
        assertNull(dto1.getCodigoPai());
        assertTrue(dto1.isElegivel());

        UnidadeDto dto2 = result.stream().filter(r -> r.getUnidade().getCodigo().equals(2L)).findFirst().get().getUnidade();
        assertEquals(1L, dto2.getCodigoPai());
        assertFalse(dto2.isElegivel());
    }

    @Test
    @DisplayName("entrar - Sessao Expirada ou Invalida")
    void entrar_SessaoExpirada() {
        EntrarRequest request = EntrarRequest.builder()
                .tituloEleitoral("123")
                .perfil("SERVIDOR")
                .unidadeCodigo(1L)
                .build();

        assertThrows(ErroAutenticacao.class, () -> facade.entrar(request));
    }

    @Test
    @DisplayName("autorizar - Sem Autenticacao")
    void autorizar_SemAutenticacao() {
        assertThrows(ErroAutenticacao.class, () -> facade.autorizar("123"));
    }
}
