package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("OrganizacaoFacade")
class OrganizacaoFacadeTest {

    @Mock
    private UsuarioFacade usuarioFacade;

    @Mock
    private UnidadeFacade unidadeFacade;

    @InjectMocks
    private OrganizacaoFacade facade;

    @Test
    @DisplayName("Deve obter usuário autenticado")
    void deveObterUsuarioAutenticado() {
        Usuario usuario = new Usuario();
        when(usuarioFacade.obterUsuarioAutenticado()).thenReturn(usuario);
        assertThat(facade.obterUsuarioAutenticado()).isEqualTo(usuario);
        verify(usuarioFacade).obterUsuarioAutenticado();
    }

    @Test
    @DisplayName("Deve extrair título do usuário")
    void deveExtrairTituloUsuario() {
        Object principal = new Object();
        when(usuarioFacade.extrairTituloUsuario(principal)).thenReturn("123");
        assertThat(facade.extrairTituloUsuario(principal)).isEqualTo("123");
        verify(usuarioFacade).extrairTituloUsuario(principal);
    }

    @Test
    @DisplayName("Deve buscar usuário por login")
    void deveBuscarPorLogin() {
        Usuario usuario = new Usuario();
        when(usuarioFacade.buscarPorLogin("user")).thenReturn(usuario);
        assertThat(facade.buscarPorLogin("user")).isEqualTo(usuario);
        verify(usuarioFacade).buscarPorLogin("user");
    }

    @Test
    @DisplayName("Deve buscar unidade por sigla")
    void deveBuscarUnidadePorSigla() {
        UnidadeDto dto = UnidadeDto.builder().sigla("U1").build();
        when(unidadeFacade.buscarPorSigla("U1")).thenReturn(dto);
        assertThat(facade.buscarUnidadePorSigla("U1")).isEqualTo(dto);
        verify(unidadeFacade).buscarPorSigla("U1");
    }
}
