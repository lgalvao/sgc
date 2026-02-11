package sgc.seguranca.login;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.comum.erros.ErroAutenticacao;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.organizacao.service.UsuarioPerfilService;
import sgc.seguranca.login.dto.EntrarRequest;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("LoginFacade Tests")
class LoginFacadeTest {

    @Mock private UsuarioFacade usuarioService;
    @Mock private GerenciadorJwt gerenciadorJwt;
    @Mock private ClienteAcessoAd clienteAcessoAd;
    @Mock private UnidadeFacade unidadeService;
    @Mock private UsuarioMapper usuarioMapper;
    @Mock private UsuarioPerfilService usuarioPerfilService;

    @InjectMocks
    private LoginFacade loginFacade;

    // --- Testes de Autenticação ---

    @Test
    @DisplayName("Deve autenticar com sucesso em ambiente de testes (mockado via property)")
    void deveAutenticarEmAmbienteTestes() {
        // Por padrão, ambienteTestes é true no código, mas vamos garantir
        ReflectionTestUtils.setField(loginFacade, "ambienteTestes", true);

        boolean resultado = loginFacade.autenticar("123", "senha");

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve delegar para ClienteAcessoAd quando não estiver em ambiente de testes")
    void deveDelegarParaLdapEmProducao() {
        ReflectionTestUtils.setField(loginFacade, "ambienteTestes", false);

        when(clienteAcessoAd.autenticar("789", "senha")).thenReturn(true);

        boolean resultado = loginFacade.autenticar("789", "senha");

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando ClienteAcessoAd falha na autenticação (Lança ErroAutenticacao)")
    void deveRetornarFalseQuandoFalhaAd() {
        ReflectionTestUtils.setField(loginFacade, "ambienteTestes", false);

        when(clienteAcessoAd.autenticar("789", "senha")).thenThrow(new ErroAutenticacao("Erro AD"));

        boolean resultado = loginFacade.autenticar("789", "senha");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando ClienteAcessoAd é nulo em produção")
    void deveRetornarFalseQuandoClienteAdNulo() {
        LoginFacade localFacade = new LoginFacade(usuarioService, gerenciadorJwt, null, unidadeService, usuarioMapper, usuarioPerfilService);
        ReflectionTestUtils.setField(localFacade, "ambienteTestes", false);

        boolean resultado = localFacade.autenticar("123", "senha");

        assertThat(resultado).isFalse();
    }

    // --- Testes de Autorização (Entrar) ---

    @Test
    @DisplayName("Deve negar acesso se perfil não corresponder à atribuição do usuário")
    void deveNegarSePerfilNaoCorresponder() {
        // Request pede ADMIN na unidade 1
        EntrarRequest req = new EntrarRequest("123", "ADMIN", 1L);

        // Usuario tem GESTOR na unidade 1
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSituacao(SituacaoUnidade.ATIVA);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.GESTOR);
        up.setUnidade(unidade);
        up.setUnidadeCodigo(1L);
        up.setUsuario(usuario);

        when(usuarioService.carregarUsuarioParaAutenticacao("123")).thenReturn(usuario);
        when(usuarioPerfilService.buscarPorUsuario("123")).thenReturn(List.of(up));
        
        when(usuarioMapper.toUnidadeDtoComElegibilidadeCalculada(unidade))
            .thenReturn(UnidadeDto.builder().codigo(1L).build());

        assertThatThrownBy(() -> loginFacade.entrar(req))
                .isInstanceOf(ErroAcessoNegado.class)
                .hasMessageContaining("Usuário não tem permissão");
    }

    @Test
    @DisplayName("Deve permitir ADMIN acessar qualquer unidade")
    void devePermitirAdminEmQualquerUnidade() {
        // Request pede ADMIN na unidade 1
        EntrarRequest req = new EntrarRequest("123", "ADMIN", 1L);

        // Usuario tem ADMIN na unidade 2
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(2L);
        unidade.setSituacao(SituacaoUnidade.ATIVA);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.ADMIN);
        up.setUnidade(unidade);
        up.setUnidadeCodigo(2L);
        up.setUsuario(usuario);

        when(usuarioService.carregarUsuarioParaAutenticacao("123")).thenReturn(usuario);
        when(usuarioPerfilService.buscarPorUsuario("123")).thenReturn(List.of(up));
        when(usuarioMapper.toUnidadeDtoComElegibilidadeCalculada(unidade))
            .thenReturn(UnidadeDto.builder().codigo(2L).build());
        when(gerenciadorJwt.gerarToken(anyString(), any(Perfil.class), anyLong()))
            .thenReturn("token-fake");

        String token = loginFacade.entrar(req);
        assertThat(token)
            .isNotNull()
            .isEqualTo("token-fake");
    }

    @Test
    @DisplayName("Deve negar acesso se unidade não corresponder à atribuição do usuário")
    void deveNegarSeUnidadeNaoCorresponder() {
        // Request pede GESTOR na unidade 1
        EntrarRequest req = new EntrarRequest("123", "GESTOR", 1L);

        // Usuario tem GESTOR na unidade 2
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(2L);
        unidade.setSituacao(SituacaoUnidade.ATIVA);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.GESTOR);
        up.setUnidade(unidade);
        up.setUnidadeCodigo(2L);
        up.setUsuario(usuario);

        when(usuarioService.carregarUsuarioParaAutenticacao("123")).thenReturn(usuario);
        when(usuarioPerfilService.buscarPorUsuario("123")).thenReturn(List.of(up));

        when(usuarioMapper.toUnidadeDtoComElegibilidadeCalculada(unidade))
            .thenReturn(UnidadeDto.builder().codigo(2L).build());

        assertThatThrownBy(() -> loginFacade.entrar(req))
                .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("Deve negar acesso se unidade estiver inativa")
    void deveNegarAcessoComUnidadeInativa() {
        EntrarRequest req = new EntrarRequest("123", "ADMIN", 1L);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSituacao(SituacaoUnidade.INATIVA);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.ADMIN);
        up.setUnidade(unidade);
        up.setUnidadeCodigo(1L);
        up.setUsuario(usuario);

        when(usuarioService.carregarUsuarioParaAutenticacao("123")).thenReturn(usuario);
        when(usuarioPerfilService.buscarPorUsuario("123")).thenReturn(List.of(up));

        // A lista filtrada será vazia, logo authorized=false
        assertThatThrownBy(() -> loginFacade.entrar(req))
                .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("Deve negar acesso se usuário não tiver nenhuma atribuição")
    void deveNegarAcessoSemAtribuicao() {
        EntrarRequest req = new EntrarRequest("123", "ADMIN", 1L);
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");

        when(usuarioService.carregarUsuarioParaAutenticacao("123")).thenReturn(usuario);
        when(usuarioPerfilService.buscarPorUsuario(anyString())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> loginFacade.entrar(req))
                .isInstanceOf(ErroAcessoNegado.class);
    }

    // --- Testes de Buscar Autorizações (Auxiliar) ---

    @Test
    @DisplayName("Deve lançar ErroAutenticacao quando usuário não é encontrado ao buscar autorizações")
    void deveLancarErroQuandoUsuarioNaoEncontrado() {
        when(usuarioService.carregarUsuarioParaAutenticacao("999")).thenReturn(null);
        
        assertThatThrownBy(() -> loginFacade.autorizar("999"))
                .isInstanceOf(ErroAutenticacao.class)
                .hasMessageContaining("Credenciais inválidas");
    }
}
