package sgc.seguranca.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.comum.erros.ErroAutenticacao;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.*;
import sgc.organizacao.service.UsuarioService;
import sgc.seguranca.LoginFacade;
import sgc.seguranca.dto.EntrarRequest;
import sgc.seguranca.dto.PerfilUnidadeDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginFacade")
class LoginFacadeTest {

    @Mock
    private UsuarioFacade usuarioFacade;
    @Mock
    private GerenciadorJwt gerenciadorJwt;
    @Mock
    private ClienteAcessoAd clienteAcessoAd;
    @Mock
    private OrganizacaoFacade OrganizacaoFacade;
    @Mock
    private UsuarioService usuarioServiceInterno;

    private LoginFacade loginFacade;

    @BeforeEach
    void setUp() {
        loginFacade = new LoginFacade(
            usuarioFacade, 
            gerenciadorJwt, 
            clienteAcessoAd, 
            OrganizacaoFacade, 
            usuarioServiceInterno
        );
        ReflectionTestUtils.setField(loginFacade, "ambienteTestes", false);
    }

    @Test
    @DisplayName("autenticar deve retornar true em ambiente de testes")
    void autenticar_AmbienteTestes() {
        ReflectionTestUtils.setField(loginFacade, "ambienteTestes", true);
        assertThat(loginFacade.autenticar("123", "senha")).isTrue();
    }

    @Test
    @DisplayName("autenticar deve retornar false se clienteAcessoAd for null")
    void autenticar_ClienteAdNull() {
        LoginFacade facadeSemAd = new LoginFacade(usuarioFacade, gerenciadorJwt, null, OrganizacaoFacade, usuarioServiceInterno);
        ReflectionTestUtils.setField(facadeSemAd, "ambienteTestes", false);
        assertThat(facadeSemAd.autenticar("123", "senha")).isFalse();
    }

    @Test
    @DisplayName("autenticar deve chamar clienteAcessoAd")
    void autenticar_Sucesso() {
        when(clienteAcessoAd.autenticar("123", "senha")).thenReturn(true);
        assertThat(loginFacade.autenticar("123", "senha")).isTrue();
    }

    @Test
    @DisplayName("autenticar deve retornar false em caso de ErroAutenticacao")
    void autenticar_ErroAutenticacao() {
        when(clienteAcessoAd.autenticar("123", "senha")).thenThrow(new ErroAutenticacao("Falha"));
        assertThat(loginFacade.autenticar("123", "senha")).isFalse();
    }

    @Test
    @DisplayName("entrar deve falhar se usuário não encontrado")
    void entrar_UsuarioNaoEncontrado() {
        when(usuarioFacade.carregarUsuarioParaAutenticacao("123")).thenReturn(null);
        EntrarRequest req = new EntrarRequest("123", "ADMIN", 1L);
        assertThatThrownBy(() -> loginFacade.entrar(req))
                .isInstanceOf(ErroAutenticacao.class);
    }

    @Test
    @DisplayName("entrar deve permitir ADMIN")
    void entrar_AdminSucesso() {
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        when(usuarioFacade.carregarUsuarioParaAutenticacao("123")).thenReturn(user);

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        
        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.ADMIN);
        up.setUnidade(unidade);
        
        when(usuarioServiceInterno.buscarPerfis("123")).thenReturn(List.of(up));
        when(gerenciadorJwt.gerarToken("123", Perfil.ADMIN, 1L)).thenReturn("token");
        when(OrganizacaoFacade.unidadePorCodigo(1L)).thenReturn(unidade);

        EntrarRequest req = new EntrarRequest("123", "ADMIN", 1L);
        assertThat(loginFacade.entrar(req)).isEqualTo("token");
    }

    @Test
    @DisplayName("entrar deve falhar ADMIN se não tem perfil")
    void entrar_AdminFalha() {
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        when(usuarioFacade.carregarUsuarioParaAutenticacao("123")).thenReturn(user);
        when(usuarioServiceInterno.buscarPerfis("123")).thenReturn(List.of());

        EntrarRequest req = new EntrarRequest("123", "ADMIN", 1L);
        assertThatThrownBy(() -> loginFacade.entrar(req))
                .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("entrar deve permitir GESTOR na unidade correta")
    void entrar_GestorSucesso() {
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        when(usuarioFacade.carregarUsuarioParaAutenticacao("123")).thenReturn(user);

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        
        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.GESTOR);
        up.setUnidade(unidade);
        
        when(usuarioServiceInterno.buscarPerfis("123")).thenReturn(List.of(up));
        when(gerenciadorJwt.gerarToken("123", Perfil.GESTOR, 1L)).thenReturn("token");
        when(OrganizacaoFacade.unidadePorCodigo(1L)).thenReturn(unidade);

        EntrarRequest req = new EntrarRequest("123", "GESTOR", 1L);
        assertThat(loginFacade.entrar(req)).isEqualTo("token");
    }

    @Test
    @DisplayName("entrar deve falhar se unidade diferente")
    void entrar_GestorUnidadeErrada() {
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        when(usuarioFacade.carregarUsuarioParaAutenticacao("123")).thenReturn(user);

        Unidade unidade = new Unidade();
        unidade.setCodigo(2L);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        
        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.GESTOR);
        up.setUnidade(unidade);
        
        when(usuarioServiceInterno.buscarPerfis("123")).thenReturn(List.of(up));

        EntrarRequest req = new EntrarRequest("123", "GESTOR", 1L);
        assertThatThrownBy(() -> loginFacade.entrar(req))
                .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("autorizar deve retornar lista de perfis")
    void buscarAutorizacoesUsuario_Sucesso() {
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        when(usuarioFacade.carregarUsuarioParaAutenticacao("123")).thenReturn(user);

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("U1");
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        
        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.GESTOR);
        up.setUnidade(unidade);
        
        when(usuarioServiceInterno.buscarPerfis("123")).thenReturn(List.of(up));

        List<PerfilUnidadeDto> result = loginFacade.buscarAutorizacoesUsuario("123");
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().perfil()).isEqualTo(Perfil.GESTOR);
    }
}
