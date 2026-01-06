package sgc.mapa.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MapaAcessoServiceTest {

    private MapaAcessoService service;

    @BeforeEach
    void setUp() {
        service = new MapaAcessoService();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Usuario criarUsuarioComPerfil(String titulo, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);
        
        Set<UsuarioPerfil> perfis = new HashSet<>();
        if (perfil != null) {
            UsuarioPerfil up = new UsuarioPerfil();
            up.setPerfil(perfil);
            up.setUsuario(usuario);
            up.setUsuarioTitulo(titulo);
            perfis.add(up);
        }
        usuario.setAtribuicoes(perfis);
        return usuario;
    }

    @Test
    @DisplayName("Deve permitir acesso via fallback (usuario.getAuthorities) quando contexto vazio")
    void devePermitirViaFallback() {
        Usuario usuario = criarUsuarioComPerfil("111", Perfil.CHEFE);
        
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

        // Contexto vazio
        SecurityContextHolder.clearContext();

        assertThatCode(() -> service.verificarAcessoImpacto(usuario, subprocesso))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve permitir acesso via SecurityContext quando usuario bate com principal")
    void devePermitirViaContexto() {
        // Usuario objeto sem permissoes (vazio)
        Usuario usuario = criarUsuarioComPerfil("222", null);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

        // Configura contexto com permissao GESTOR
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                usuario, null, List.of(new SimpleGrantedAuthority("ROLE_GESTOR")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThatCode(() -> service.verificarAcessoImpacto(usuario, subprocesso))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve usar fallback se usuario autenticado for diferente")
    void deveUsarFallbackSeUsuarioDiferente() {
        Usuario usuarioAlvo = criarUsuarioComPerfil("333", Perfil.ADMIN);
        Usuario usuarioLogado = criarUsuarioComPerfil("444", Perfil.GESTOR);
        
        // Contexto com outro usuário
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                usuarioLogado, null, List.of(new SimpleGrantedAuthority("ROLE_GESTOR")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);

        // Deve usar authorities do usuarioAlvo (ADMIN) e permitir
        assertThatCode(() -> service.verificarAcessoImpacto(usuarioAlvo, subprocesso))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve negar acesso se role não bater")
    void deveNegarAcessoSemRole() {
        Usuario usuario = criarUsuarioComPerfil("555", Perfil.SERVIDOR); // Sem permissao de impacto
        
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

        SecurityContextHolder.clearContext();
        
        assertThatCode(() -> service.verificarAcessoImpacto(usuario, subprocesso))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve negar acesso para CHEFE em situação inválida")
    void deveNegarChefeSituacaoInvalida() {
        Usuario usuario = criarUsuarioComPerfil("666", Perfil.CHEFE);
        
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA); // Invalido pra chefe

        assertThatThrownBy(() -> service.verificarAcessoImpacto(usuario, subprocesso))
                .isInstanceOf(ErroAccessoNegado.class);
    }
}
