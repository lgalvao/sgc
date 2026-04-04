package sgc.seguranca.login;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.seguranca.*;
import sgc.seguranca.dto.*;

import java.lang.reflect.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginFacade")
@SuppressWarnings("NullAway.Init")
class LoginFacadeTest {

    @Mock
    private UsuarioFacade usuarioFacade;
    @Mock
    private GerenciadorJwt gerenciadorJwt;
    @Mock
    private ClienteAcessoAd clienteAcessoAd;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private UsuarioService usuarioServiceInterno;

    private LoginFacade loginFacade;

    @BeforeEach
    void setUp() {
        loginFacade = new LoginFacade(
                usuarioFacade,
                gerenciadorJwt,
                clienteAcessoAd,
                unidadeService,
                usuarioServiceInterno
        );
    }

    @Test
    @DisplayName("autenticar deve retornar false se clienteAcessoAd for null")
    void autenticar_ClienteAdNull() {
        LoginFacade facadeSemAd = new LoginFacade(usuarioFacade, gerenciadorJwt, null, unidadeService, usuarioServiceInterno);
        assertThat(facadeSemAd.autenticar("123", "senha")).isFalse();
    }

    @Test
    @DisplayName("autenticar deve chamar clienteAcessoAd")
    void autenticar_Sucesso() {
        doNothing().when(clienteAcessoAd).autenticar("123", "senha");
        assertThat(loginFacade.autenticar("123", "senha")).isTrue();
    }

    @Test
    @DisplayName("autenticar deve retornar false em caso de ErroAutenticacao")
    void autenticar_ErroAutenticacao() {
        doThrow(new ErroAutenticacao("Falha")).when(clienteAcessoAd).autenticar("123", "senha");
        assertThat(loginFacade.autenticar("123", "senha")).isFalse();
    }

    @Test
    @DisplayName("entrar deve falhar se usuário não encontrado")
    void entrar_UsuarioNaoEncontrado() {
        when(usuarioFacade.carregarUsuarioParaAutenticacao("123")).thenReturn(null);
        EntrarRequest req = new EntrarRequest("ADMIN", 1L);
        assertThatThrownBy(() -> loginFacade.entrar(req, "123"))
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

        when(usuarioServiceInterno.buscarAutorizacoesPerfil("123")).thenReturn(List.of(
                new UsuarioPerfilAutorizacaoLeitura("123", Perfil.ADMIN, 1L, null, "U1", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA)
        ));
        when(gerenciadorJwt.gerarToken("123", Perfil.ADMIN, 1L)).thenReturn("token");
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(unidade);

        EntrarRequest req = new EntrarRequest("ADMIN", 1L);
        assertThat(loginFacade.entrar(req, "123")).isEqualTo("token");
    }

    @Test
    @DisplayName("entrar deve falhar ADMIN se não tem perfil")
    void entrar_AdminFalha() {
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        when(usuarioFacade.carregarUsuarioParaAutenticacao("123")).thenReturn(user);
        when(usuarioServiceInterno.buscarAutorizacoesPerfil("123")).thenReturn(List.of());

        EntrarRequest req = new EntrarRequest("ADMIN", 1L);
        assertThatThrownBy(() -> loginFacade.entrar(req, "123"))
                .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("entrar deve falhar ADMIN quando autorizações pré-carregadas não incluem admin")
    void entrar_AdminFalhaComAutorizacoesPreCarregadasSemAdmin() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("U1");
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(unidade);

        EntrarRequest req = new EntrarRequest("ADMIN", 1L);
        List<PerfilUnidadeDto> autorizacoes = List.of(
                new PerfilUnidadeDto(Perfil.GESTOR, sgc.organizacao.dto.UnidadeDto.builder().codigo(1L).sigla("U1").build())
        );

        assertThatThrownBy(() -> loginFacade.entrar(req, "123", autorizacoes))
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

        when(usuarioServiceInterno.buscarAutorizacoesPerfil("123")).thenReturn(List.of(
                new UsuarioPerfilAutorizacaoLeitura("123", Perfil.GESTOR, 1L, null, "U1", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA)
        ));
        when(gerenciadorJwt.gerarToken("123", Perfil.GESTOR, 1L)).thenReturn("token");
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(unidade);

        EntrarRequest req = new EntrarRequest("GESTOR", 1L);
        assertThat(loginFacade.entrar(req, "123")).isEqualTo("token");
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

        when(usuarioServiceInterno.buscarAutorizacoesPerfil("123")).thenReturn(List.of(
                new UsuarioPerfilAutorizacaoLeitura("123", Perfil.GESTOR, 2L, null, "U2", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA)
        ));

        EntrarRequest req = new EntrarRequest("GESTOR", 1L);
        assertThatThrownBy(() -> loginFacade.entrar(req, "123"))
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

        when(usuarioServiceInterno.buscarAutorizacoesPerfil("123")).thenReturn(List.of(
                new UsuarioPerfilAutorizacaoLeitura("123", Perfil.GESTOR, 1L, "Unidade 1", "U1", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA)
        ));

        List<PerfilUnidadeDto> result = loginFacade.buscarAutorizacoesUsuario("123");
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().perfil()).isEqualTo(Perfil.GESTOR);
    }

    @Test
    @DisplayName("buscarAutorizacoesUsuario deve filtrar unidades inativas")
    void buscarAutorizacoesUsuario_DeveFiltrarUnidadesInativas() {
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        when(usuarioFacade.carregarUsuarioParaAutenticacao("123")).thenReturn(user);

        Unidade unidadeAtiva = new Unidade();
        unidadeAtiva.setCodigo(1L);
        unidadeAtiva.setSigla("U1");
        unidadeAtiva.setSituacao(SituacaoUnidade.ATIVA);
        unidadeAtiva.setTipo(TipoUnidade.OPERACIONAL);

        Unidade unidadeInativa = new Unidade();
        unidadeInativa.setCodigo(2L);
        unidadeInativa.setSigla("U2");
        unidadeInativa.setSituacao(SituacaoUnidade.INATIVA);
        unidadeInativa.setTipo(TipoUnidade.OPERACIONAL);

        when(usuarioServiceInterno.buscarAutorizacoesPerfil("123")).thenReturn(List.of(
                new UsuarioPerfilAutorizacaoLeitura("123", Perfil.GESTOR, 1L, "Unidade 1", "U1", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA),
                new UsuarioPerfilAutorizacaoLeitura("123", Perfil.CHEFE, 2L, "Unidade 2", "U2", TipoUnidade.OPERACIONAL, SituacaoUnidade.INATIVA)
        ));

        List<PerfilUnidadeDto> result = loginFacade.buscarAutorizacoesUsuario("123");

        assertThat(result).singleElement().satisfies(perfil -> {
            assertThat(perfil.perfil()).isEqualTo(Perfil.GESTOR);
            assertThat(perfil.unidade().getCodigo()).isEqualTo(1L);
        });
    }

    @Test
    @DisplayName("deve lançar erro quando unidade para autorização estiver ausente")
    void deveLancarErroQuandoUnidadeAusenteNaAutorizacao() throws Exception {
        Method metodo = LoginFacade.class.getDeclaredMethod("toUnidadeDtoObrigatoria", UsuarioPerfilAutorizacaoLeitura.class);
        metodo.setAccessible(true);

        assertThatThrownBy(() -> metodo.invoke(loginFacade, new Object[]{null}))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("Unidade ausente na autorização de login");
    }
}
