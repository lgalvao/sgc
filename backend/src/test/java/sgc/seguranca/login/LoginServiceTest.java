package sgc.seguranca.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroAutenticacao;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.model.UsuarioPerfilRepo;
import sgc.organizacao.model.UsuarioRepo;
import sgc.seguranca.login.dto.EntrarReq;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginService - Gaps de Cobertura")
class LoginServiceTest {

    @Mock private UsuarioRepo usuarioRepo;
    @Mock private UsuarioPerfilRepo usuarioPerfilRepo;
    @Mock private GerenciadorJwt gerenciadorJwt;
    @Mock private ClienteAcessoAd clienteAcessoAd;
    @Mock private UnidadeService unidadeService;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginService(usuarioRepo, usuarioPerfilRepo, gerenciadorJwt, clienteAcessoAd, unidadeService);
        ReflectionTestUtils.setField(loginService, "ambienteTestes", false);
    }

    @Test
    @DisplayName("Linhas 86, 89: Deve falhar autenticação se AD for null e não for ambiente de testes")
    void deveFalharAutenticacaoSemAdEmProducao() {
        LoginService serviceSemAd = new LoginService(usuarioRepo, usuarioPerfilRepo, gerenciadorJwt, null, unidadeService);
        ReflectionTestUtils.setField(serviceSemAd, "ambienteTestes", false);

        boolean result = serviceSemAd.autenticar("123", "senha");
        assertFalse(result);
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
        
        when(usuarioRepo.findByIdWithAtribuicoes(titulo)).thenReturn(java.util.Optional.of(usuario));
        when(usuarioPerfilRepo.findByUsuarioTitulo(titulo)).thenReturn(Collections.emptyList());

        assertThrows(ErroAccessoNegado.class, () -> loginService.entrar(req));
    }
}
