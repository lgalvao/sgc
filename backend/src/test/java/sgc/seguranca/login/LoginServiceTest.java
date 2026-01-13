package sgc.seguranca.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroAutenticacao;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.UsuarioService;
import sgc.seguranca.login.dto.EntrarReq;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("LoginService - Gaps de Cobertura")
class LoginServiceTest {

    @Mock private UsuarioService usuarioService;
    @Mock private GerenciadorJwt gerenciadorJwt;
    @Mock private ClienteAcessoAd clienteAcessoAd;
    @Mock private UnidadeService unidadeService;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginService(usuarioService, gerenciadorJwt, clienteAcessoAd, unidadeService);
        ReflectionTestUtils.setField(loginService, "ambienteTestes", false);
    }

    @Test
    @DisplayName("Linhas 86, 89: Deve falhar autenticação se AD for null e não for ambiente de testes")
    void deveFalharAutenticacaoSemAdEmProducao() {
        LoginService serviceSemAd = new LoginService(usuarioService, gerenciadorJwt, null, unidadeService);
        ReflectionTestUtils.setField(serviceSemAd, "ambienteTestes", false);

        boolean result = serviceSemAd.autenticar("123", "senha");
        assertFalse(result);
    }

    @Test
    @DisplayName("Deve usar usuarioService para autenticar se AD for null e for ambiente de testes")
    void deveUsarUsuarioServiceEmAmbienteTestes() {
        LoginService serviceSemAd = new LoginService(usuarioService, gerenciadorJwt, null, unidadeService);
        ReflectionTestUtils.setField(serviceSemAd, "ambienteTestes", true);

        when(usuarioService.carregarUsuarioParaAutenticacao("123")).thenReturn(new sgc.organizacao.model.Usuario());

        boolean result = serviceSemAd.autenticar("123", "senha");

        // Verifica se chamou usuarioService
        org.mockito.Mockito.verify(usuarioService).carregarUsuarioParaAutenticacao("123");
        org.junit.jupiter.api.Assertions.assertTrue(result);

        // Testa falha também
        when(usuarioService.carregarUsuarioParaAutenticacao("999")).thenReturn(null);
        assertFalse(serviceSemAd.autenticar("999", "senha"));
    }

    @Test
    @DisplayName("Linhas 94-97: Deve lidar com ErroAutenticacao do ClienteAcessoAd")
    void deveLidarComErroAd() {
        when(clienteAcessoAd.autenticar("123", "senha")).thenThrow(new ErroAutenticacao("Falha AD"));
        
        boolean result = loginService.autenticar("123", "senha");
        assertFalse(result);
    }

    @Test
    @DisplayName("Linha 154: Deve negar acesso se usuário não tiver a atribuição solicitada em entrar")
    void deveNegarAcessoSemAtribuicao() {
        String titulo = "123";
        EntrarReq req = EntrarReq.builder()
                .tituloEleitoral(titulo)
                .perfil("GESTOR")
                .unidadeCodigo(1L)
                .build();

        // Prepara a autenticação recente
        when(clienteAcessoAd.autenticar(titulo, "senha")).thenReturn(true);
        loginService.autenticar(titulo, "senha");

        // Simula busca de autorizações retornando lista vazia ou sem o perfil/unidade
        sgc.organizacao.model.Usuario usuario = new sgc.organizacao.model.Usuario();
        usuario.setTituloEleitoral(titulo);
        usuario.setAtribuicoes(Collections.emptySet());
        
        when(usuarioService.carregarUsuarioParaAutenticacao(titulo)).thenReturn(usuario);

        assertThrows(ErroAccessoNegado.class, () -> loginService.entrar(req));
    }

    @Test
    @DisplayName("Deve negar acesso se perfil coincidir mas unidade não")
    void deveNegarAcessoSeUnidadeDiferente() {
        String titulo = "123";
        EntrarReq req = EntrarReq.builder()
                .tituloEleitoral(titulo)
                .perfil("GESTOR")
                .unidadeCodigo(99L) // Solicitando unidade 99
                .build();

        // Autentica
        when(clienteAcessoAd.autenticar(titulo, "senha")).thenReturn(true);
        loginService.autenticar(titulo, "senha");

        // Usuário tem GESTOR mas na unidade 1L
        sgc.organizacao.model.Usuario usuario = new sgc.organizacao.model.Usuario();
        usuario.setTituloEleitoral(titulo);

        sgc.organizacao.model.UsuarioPerfil atrib = new sgc.organizacao.model.UsuarioPerfil();
        atrib.setPerfil(sgc.organizacao.model.Perfil.GESTOR);
        sgc.organizacao.model.Unidade unidade = new sgc.organizacao.model.Unidade();
        unidade.setCodigo(1L);
        unidade.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL); // Set type to avoid NPE
        atrib.setUnidade(unidade);

        usuario.setAtribuicoes(java.util.Set.of(atrib));

        when(usuarioService.carregarUsuarioParaAutenticacao(titulo)).thenReturn(usuario);

        assertThrows(ErroAccessoNegado.class, () -> loginService.entrar(req));
    }

    @Test
    @DisplayName("Deve negar acesso se unidade coincidir mas perfil não")
    void deveNegarAcessoSePerfilDiferente() {
        String titulo = "123";
        EntrarReq req = EntrarReq.builder()
                .tituloEleitoral(titulo)
                .perfil("ADMIN") // Solicitando ADMIN
                .unidadeCodigo(1L)
                .build();

        // Autentica
        when(clienteAcessoAd.autenticar(titulo, "senha")).thenReturn(true);
        loginService.autenticar(titulo, "senha");

        // Usuário tem GESTOR na unidade 1L
        sgc.organizacao.model.Usuario usuario = new sgc.organizacao.model.Usuario();
        usuario.setTituloEleitoral(titulo);

        sgc.organizacao.model.UsuarioPerfil atrib = new sgc.organizacao.model.UsuarioPerfil();
        atrib.setPerfil(sgc.organizacao.model.Perfil.GESTOR);
        sgc.organizacao.model.Unidade unidade = new sgc.organizacao.model.Unidade();
        unidade.setCodigo(1L);
        unidade.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL); // Set type to avoid NPE
        atrib.setUnidade(unidade);

        usuario.setAtribuicoes(java.util.Set.of(atrib));

        when(usuarioService.carregarUsuarioParaAutenticacao(titulo)).thenReturn(usuario);

        assertThrows(ErroAccessoNegado.class, () -> loginService.entrar(req));
    }
}
