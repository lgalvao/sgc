package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Controle de Acesso ao Mapa")
class MapaAcessoServiceTest {

    @InjectMocks
    private MapaAcessoService service;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private void setupSecurityContext(Usuario usuario, String... roles) {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuario);

        List<GrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .map(role -> (GrantedAuthority) () -> "ROLE_" + role)
                .toList();

        // Mock raw type issue workaround
        Collection rawAuthorities = authorities;
        when(authentication.getAuthorities()).thenReturn(rawAuthorities);
    }

    @Test
    @DisplayName("Chefe deve ter acesso em REVISAO_CADASTRO_EM_ANDAMENTO")
    void chefeDeveTerAcessoEmAndamento() {
        Usuario usuario = new Usuario();
        setupSecurityContext(usuario, "CHEFE");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);

        assertThatCode(() -> service.verificarAcessoImpacto(usuario, subprocesso))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Chefe deve ter acesso em NAO_INICIADO")
    void chefeDeveTerAcessoEmNaoIniciado() {
        Usuario usuario = new Usuario();
        setupSecurityContext(usuario, "CHEFE");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(NAO_INICIADO);

        assertThatCode(() -> service.verificarAcessoImpacto(usuario, subprocesso))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Chefe NÃO deve ter acesso em REVISAO_CADASTRO_DISPONIBILIZADA")
    void chefeNaoDeveTerAcessoEmDisponibilizada() {
        Usuario usuario = new Usuario();
        setupSecurityContext(usuario, "CHEFE");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);

        assertThatThrownBy(() -> service.verificarAcessoImpacto(usuario, subprocesso))
                .isInstanceOf(ErroAccessoNegado.class)
                .hasMessageContaining("O chefe da unidade só pode verificar os impactos");
    }

    @Test
    @DisplayName("Gestor deve ter acesso em REVISAO_CADASTRO_DISPONIBILIZADA")
    void gestorDeveTerAcessoEmDisponibilizada() {
        Usuario usuario = new Usuario();
        setupSecurityContext(usuario, "GESTOR");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);

        assertThatCode(() -> service.verificarAcessoImpacto(usuario, subprocesso))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Gestor NÃO deve ter acesso em REVISAO_CADASTRO_EM_ANDAMENTO")
    void gestorNaoDeveTerAcessoEmAndamento() {
        Usuario usuario = new Usuario();
        setupSecurityContext(usuario, "GESTOR");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);

        assertThatThrownBy(() -> service.verificarAcessoImpacto(usuario, subprocesso))
                .isInstanceOf(ErroAccessoNegado.class)
                .hasMessageContaining("O gestor só pode verificar os impactos");
    }

    @Test
    @DisplayName("Admin deve ter acesso em REVISAO_CADASTRO_DISPONIBILIZADA")
    void adminDeveTerAcessoEmDisponibilizada() {
        Usuario usuario = new Usuario();
        setupSecurityContext(usuario, "ADMIN");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);

        assertThatCode(() -> service.verificarAcessoImpacto(usuario, subprocesso))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Admin deve ter acesso em REVISAO_CADASTRO_HOMOLOGADA")
    void adminDeveTerAcessoEmHomologada() {
        Usuario usuario = new Usuario();
        setupSecurityContext(usuario, "ADMIN");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);

        assertThatCode(() -> service.verificarAcessoImpacto(usuario, subprocesso))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Admin deve ter acesso em REVISAO_MAPA_AJUSTADO")
    void adminDeveTerAcessoEmMapaAjustado() {
        Usuario usuario = new Usuario();
        setupSecurityContext(usuario, "ADMIN");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(REVISAO_MAPA_AJUSTADO);

        assertThatCode(() -> service.verificarAcessoImpacto(usuario, subprocesso))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Admin NÃO deve ter acesso em REVISAO_CADASTRO_EM_ANDAMENTO")
    void adminNaoDeveTerAcessoEmAndamento() {
        Usuario usuario = new Usuario();
        setupSecurityContext(usuario, "ADMIN");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);

        assertThatThrownBy(() -> service.verificarAcessoImpacto(usuario, subprocesso))
                .isInstanceOf(ErroAccessoNegado.class)
                .hasMessageContaining("O administrador só pode verificar os impactos");
    }
}
