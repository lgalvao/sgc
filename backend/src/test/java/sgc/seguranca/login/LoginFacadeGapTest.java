package sgc.seguranca.login;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.*;
import sgc.seguranca.login.dto.EntrarRequest;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("LoginFacade - Gap Logic Tests")
class LoginFacadeGapTest {

    @Mock private UsuarioFacade usuarioService;
    @Mock private GerenciadorJwt gerenciadorJwt;
    @Mock private ClienteAcessoAd clienteAcessoAd;
    @Mock private UnidadeFacade unidadeService;
    @Mock private UsuarioMapper usuarioMapper;

    @InjectMocks
    private LoginFacade loginFacade;

    @Test
    @DisplayName("Deve negar acesso se perfil não corresponder (branch coverage)")
    void deveNegarSePerfilNaoCorresponder() throws Exception {
        // Setup autenticacao recente
        injectAutenticacaoRecente("123");

        // Request pede ADMIN na unidade 1
        EntrarRequest req = new EntrarRequest("123", "ADMIN", 1L);

        // Usuario tem GESTOR na unidade 1
        Usuario usuario = criarUsuarioComAtribuicao("123", Perfil.GESTOR, 1L);
        when(usuarioService.carregarUsuarioParaAutenticacao("123")).thenReturn(usuario);
        
        // Mock mapper (para evitar NPE no stream map)
        when(usuarioMapper.toUnidadeDtoComElegibilidadeCalculada(usuario.getTodasAtribuicoes().iterator().next().getUnidade()))
            .thenReturn(sgc.organizacao.dto.UnidadeDto.builder().codigo(1L).build());

        assertThatThrownBy(() -> loginFacade.entrar(req))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("Deve negar acesso se unidade não corresponder (branch coverage)")
    void deveNegarSeUnidadeNaoCorresponder() throws Exception {
        // Setup autenticacao recente
        injectAutenticacaoRecente("123");

        // Request pede ADMIN na unidade 1
        EntrarRequest req = new EntrarRequest("123", "ADMIN", 1L);

        // Usuario tem ADMIN na unidade 2
        Usuario usuario = criarUsuarioComAtribuicao("123", Perfil.ADMIN, 2L);
        when(usuarioService.carregarUsuarioParaAutenticacao("123")).thenReturn(usuario);

        // Mock mapper
        when(usuarioMapper.toUnidadeDtoComElegibilidadeCalculada(usuario.getTodasAtribuicoes().iterator().next().getUnidade()))
            .thenReturn(sgc.organizacao.dto.UnidadeDto.builder().codigo(2L).build());

        assertThatThrownBy(() -> loginFacade.entrar(req))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    private void injectAutenticacaoRecente(String titulo) throws Exception {
        java.lang.reflect.Field field = LoginFacade.class.getDeclaredField("autenticacoesRecentes");
        field.setAccessible(true);
        Map<String, LocalDateTime> map = (Map<String, LocalDateTime>) field.get(loginFacade);
        map.put(titulo, LocalDateTime.now());
    }

    private Usuario criarUsuarioComAtribuicao(String titulo, Perfil perfil, Long codUnidade) {
        Usuario u = new Usuario();
        u.setTituloEleitoral(titulo);
        u.setAtribuicoes(new HashSet<>());
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(codUnidade);
        unidade.setSituacao(SituacaoUnidade.ATIVA);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(perfil);
        up.setUnidade(unidade);
        up.setUsuario(u);

        u.getAtribuicoes().add(up);
        return u;
    }
}
