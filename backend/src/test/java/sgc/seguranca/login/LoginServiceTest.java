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
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.seguranca.login.dto.EntrarRequest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("LoginFacade - Gaps de Cobertura")
class LoginServiceTest {

    @Mock private UsuarioFacade usuarioService;
    @Mock private GerenciadorJwt gerenciadorJwt;
    @Mock private ClienteAcessoAd clienteAcessoAd;
    @Mock private UnidadeFacade unidadeService;
    @Mock private UsuarioMapper usuarioMapper;

    private LoginFacade loginFacade;

    @BeforeEach
    void setUp() {
        loginFacade = new LoginFacade(usuarioService, gerenciadorJwt, clienteAcessoAd, unidadeService, usuarioMapper);
        ReflectionTestUtils.setField(loginFacade, "ambienteTestes", false);
    }

    @Test
    @DisplayName("Linhas 86, 89: Deve falhar autenticação se AD for null e não for ambiente de testes")
    void deveFalharAutenticacaoSemAdEmProducao() {
        LoginFacade serviceSemAd = new LoginFacade(usuarioService, gerenciadorJwt, null, unidadeService, usuarioMapper);
        ReflectionTestUtils.setField(serviceSemAd, "ambienteTestes", false);

        boolean result = serviceSemAd.autenticar("123", "senha");
        assertFalse(result);
    }

    @Test
    @DisplayName("Linhas 94-97: Deve lidar com ErroAutenticacao do ClienteAcessoAd")
    void deveLidarComErroAd() {
        when(clienteAcessoAd.autenticar("123", "senha")).thenThrow(new ErroAutenticacao("Falha AD"));
        
        boolean result = loginFacade.autenticar("123", "senha");
        assertFalse(result);
    }

    @Test
    @DisplayName("Linha 154: Deve negar acesso se usuário não tiver a atribuição solicitada em entrar")
    void deveNegarAcessoSemAtribuicao() {
        String titulo = "123";
        EntrarRequest req = EntrarRequest.builder()
                .tituloEleitoral(titulo)
                .perfil("GESTOR")
                .unidadeCodigo(1L)
                .build();

        // Prepara a autenticação recente
        when(clienteAcessoAd.autenticar(titulo, "senha")).thenReturn(true);
        loginFacade.autenticar(titulo, "senha");

        // Simula busca de autorizações retornando lista vazia ou sem o perfil/unidade
        sgc.organizacao.model.Usuario usuario = new sgc.organizacao.model.Usuario();
        usuario.setTituloEleitoral(titulo);
        usuario.setAtribuicoes(Collections.emptySet());
        
        when(usuarioService.carregarUsuarioParaAutenticacao(titulo)).thenReturn(usuario);

        assertThrows(ErroAccessoNegado.class, () -> loginFacade.entrar(req));
    }

    @Test
    @DisplayName("Deve autenticar em ambiente de testes sem AD")
    void deveAutenticarEmAmbienteTestesSemAd() {
        LoginFacade serviceSemAd = new LoginFacade(usuarioService, gerenciadorJwt, null, unidadeService, usuarioMapper);
        ReflectionTestUtils.setField(serviceSemAd, "ambienteTestes", true);

        when(usuarioService.carregarUsuarioParaAutenticacao("123")).thenReturn(new sgc.organizacao.model.Usuario());

        boolean result = serviceSemAd.autenticar("123", "senha");

        // Deve retornar true pois encontrou usuário
        org.junit.jupiter.api.Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("Deve falhar autorização sem autenticação prévia")
    void deveFalharAutorizacaoSemAutenticacaoPrevia() {
        assertThrows(ErroAutenticacao.class, () -> loginFacade.autorizar("user_nao_autenticado"));
    }

    @Test
    @DisplayName("Deve falhar entrar com autenticação expirada")
    void deveFalharEntrarComAutenticacaoExpirada() {
        String titulo = "user_expirado";
        // Autentica
        when(clienteAcessoAd.autenticar(titulo, "senha")).thenReturn(true);
        loginFacade.autenticar(titulo, "senha");

        // Força expiração
        loginFacade.expireAllAuthenticationsForTest();

        EntrarRequest req = EntrarRequest.builder().tituloEleitoral(titulo).build();
        assertThrows(ErroAutenticacao.class, () -> loginFacade.entrar(req));
    }

    @Test
    @DisplayName("Deve limpar autenticações expiradas")
    void deveLimparAutenticacoesExpiradas() {
        String titulo = "user_clean";
        when(clienteAcessoAd.autenticar(titulo, "senha")).thenReturn(true);
        loginFacade.autenticar(titulo, "senha");

        // Força expiração
        loginFacade.expireAllAuthenticationsForTest();

        loginFacade.limparAutenticacoesExpiradas();

        assertThrows(ErroAutenticacao.class, () -> loginFacade.autorizar(titulo));
    }
}
