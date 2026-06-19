package sgc.seguranca.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.comum.erros.ErroAutenticacao;
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.organizacao.model.*;
import sgc.organizacao.service.UnidadeService;
import sgc.organizacao.service.UsuarioService;
import sgc.seguranca.LoginAplicacaoService;
import sgc.seguranca.dto.EntrarRequest;
import sgc.seguranca.dto.PerfilUnidadeDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginAplicacaoService")
@SuppressWarnings("NullAway.Init")
class LoginAplicacaoServiceTest {

    @Mock
    private UsuarioAplicacaoService usuarioAplicacaoService;
    @Mock
    private GerenciadorJwt gerenciadorJwt;
    @Mock
    private ClienteAcessoAd clienteAcessoAd;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private UsuarioService usuarioServiceInterno;

    private LoginAplicacaoService loginAplicacaoService;

    @BeforeEach
    void setUp() {
        loginAplicacaoService = new LoginAplicacaoService(
                usuarioAplicacaoService,
                gerenciadorJwt,
                clienteAcessoAd,
                unidadeService,
                usuarioServiceInterno
        );
    }

    @Test
    @DisplayName("autenticar deve retornar false se clienteAcessoAd for null")
    void autenticar_ClienteAdNull() {
        LoginAplicacaoService loginAplicacaoServiceSemAd = new LoginAplicacaoService(usuarioAplicacaoService, gerenciadorJwt, null, unidadeService, usuarioServiceInterno);
        assertThat(loginAplicacaoServiceSemAd.autenticar("123", "senha")).isFalse();
    }

    @Test
    @DisplayName("autenticar deve chamar clienteAcessoAd")
    void autenticar_Sucesso() {
        doNothing().when(clienteAcessoAd).autenticar("123", "senha");
        assertThat(loginAplicacaoService.autenticar("123", "senha")).isTrue();
    }

    @Test
    @DisplayName("autenticar deve retornar false em caso de ErroAutenticacao")
    void autenticar_ErroAutenticacao() {
        doThrow(new ErroAutenticacao("Falha")).when(clienteAcessoAd).autenticar("123", "senha");
        assertThat(loginAplicacaoService.autenticar("123", "senha")).isFalse();
    }

    @Test
    @DisplayName("entrar deve falhar se usuário não encontrado")
    void entrar_UsuarioNaoEncontrado() {
        when(usuarioAplicacaoService.carregarUsuarioParaAutenticacao("123")).thenReturn(null);
        EntrarRequest req = new EntrarRequest("ADMIN", 1L);
        assertThatThrownBy(() -> loginAplicacaoService.entrar(req, "123"))
                .isInstanceOf(ErroAutenticacao.class);
    }

    @Test
    @DisplayName("entrar deve permitir ADMIN")
    void entrar_AdminSucesso() {
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        when(usuarioAplicacaoService.carregarUsuarioParaAutenticacao("123")).thenReturn(user);

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        when(usuarioServiceInterno.buscarAutorizacoesPerfil("123")).thenReturn(List.of(
                new UsuarioPerfilAutorizacaoLeitura("123", Perfil.ADMIN, 1L, "Unidade 1", "U1", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA)
        ));
        when(gerenciadorJwt.gerarToken("123", Perfil.ADMIN, 1L)).thenReturn("token");
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(unidade);

        EntrarRequest req = new EntrarRequest("ADMIN", 1L);
        assertThat(loginAplicacaoService.entrar(req, "123")).isEqualTo("token");
    }

    @Test
    @DisplayName("entrar deve falhar ADMIN se não tem perfil")
    void entrar_AdminFalha() {
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        when(usuarioAplicacaoService.carregarUsuarioParaAutenticacao("123")).thenReturn(user);
        when(usuarioServiceInterno.buscarAutorizacoesPerfil("123")).thenReturn(List.of());

        EntrarRequest req = new EntrarRequest("ADMIN", 1L);
        assertThatThrownBy(() -> loginAplicacaoService.entrar(req, "123"))
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
                new PerfilUnidadeDto(Perfil.GESTOR.name(), sgc.organizacao.dto.UnidadeResumoDto.builder().codigo(1L).nome("Unidade 1").sigla("U1").build())
        );

        assertThatThrownBy(() -> loginAplicacaoService.entrar(req, "123", autorizacoes))
                .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("entrar deve permitir GESTOR na unidade correta")
    void entrar_GestorSucesso() {
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        when(usuarioAplicacaoService.carregarUsuarioParaAutenticacao("123")).thenReturn(user);

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        when(usuarioServiceInterno.buscarAutorizacoesPerfil("123")).thenReturn(List.of(
                new UsuarioPerfilAutorizacaoLeitura("123", Perfil.GESTOR, 1L, "Unidade 1", "U1", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA)
        ));
        when(gerenciadorJwt.gerarToken("123", Perfil.GESTOR, 1L)).thenReturn("token");
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(unidade);

        EntrarRequest req = new EntrarRequest("GESTOR", 1L);
        assertThat(loginAplicacaoService.entrar(req, "123")).isEqualTo("token");
    }

    @Test
    @DisplayName("entrar deve falhar se unidade diferente")
    void entrar_GestorUnidadeErrada() {
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        when(usuarioAplicacaoService.carregarUsuarioParaAutenticacao("123")).thenReturn(user);

        Unidade unidade = new Unidade();
        unidade.setCodigo(2L);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        when(usuarioServiceInterno.buscarAutorizacoesPerfil("123")).thenReturn(List.of(
                new UsuarioPerfilAutorizacaoLeitura("123", Perfil.GESTOR, 2L, "Unidade 2", "U2", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA)
        ));

        EntrarRequest req = new EntrarRequest("GESTOR", 1L);
        assertThatThrownBy(() -> loginAplicacaoService.entrar(req, "123"))
                .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("autorizar deve retornar lista de perfis")
    void buscarAutorizacoesUsuario_Sucesso() {
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        when(usuarioAplicacaoService.carregarUsuarioParaAutenticacao("123")).thenReturn(user);

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("U1");
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        when(usuarioServiceInterno.buscarAutorizacoesPerfil("123")).thenReturn(List.of(
                new UsuarioPerfilAutorizacaoLeitura("123", Perfil.GESTOR, 1L, "Unidade 1", "U1", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA)
        ));

        List<PerfilUnidadeDto> result = loginAplicacaoService.buscarAutorizacoesUsuario("123");
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().perfil()).isEqualTo(Perfil.GESTOR.name());
    }

    @Test
    @DisplayName("buscarAutorizacoesUsuario deve filtrar unidades inativas")
    void buscarAutorizacoesUsuario_DeveFiltrarUnidadesInativas() {
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        when(usuarioAplicacaoService.carregarUsuarioParaAutenticacao("123")).thenReturn(user);

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

        List<PerfilUnidadeDto> result = loginAplicacaoService.buscarAutorizacoesUsuario("123");

        assertThat(result).singleElement().satisfies(perfil -> {
            assertThat(perfil.perfil()).isEqualTo(Perfil.GESTOR.name());
            assertThat(perfil.unidade().codigo()).isEqualTo(1L);
        });
    }

}
