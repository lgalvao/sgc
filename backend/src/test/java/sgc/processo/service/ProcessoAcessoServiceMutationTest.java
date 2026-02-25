package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.PerfilDto;
import sgc.subprocesso.service.ConsultasSubprocessoService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoAcessoService - Cobertura de Mutações")
class ProcessoAcessoServiceMutationTest {

    @InjectMocks
    private ProcessoAcessoService service;

    @Mock
    private UsuarioFacade usuarioService;

    @Mock
    private ConsultasSubprocessoService queryService;

    @Test
    @DisplayName("checarAcesso deve retornar false se authentication for null")
    void checarAcesso_AuthenticationNull() {
        assertThat(service.checarAcesso(null, 1L)).isFalse();
    }

    @Test
    @DisplayName("checarAcesso deve retornar false se não estiver autenticado")
    void checarAcesso_NaoAutenticado() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        assertThat(service.checarAcesso(auth, 1L)).isFalse();
    }

    @Test
    @DisplayName("checarAcesso deve retornar false se name for null")
    void checarAcesso_NameNull() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(null);
        assertThat(service.checarAcesso(auth, 1L)).isFalse();
    }

    @Test
    @DisplayName("checarAcesso deve retornar false se não for GESTOR nem CHEFE")
    void checarAcesso_NaoGestorNemChefe() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(auth).getAuthorities();

        assertThat(service.checarAcesso(auth, 1L)).isFalse();
    }

    @Test
    @DisplayName("checarAcesso deve retornar false se perfis estiverem vazios")
    void checarAcesso_PerfisVazios() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_GESTOR"))).when(auth).getAuthorities();

        when(usuarioService.buscarPerfisUsuario("user")).thenReturn(Collections.emptyList());

        assertThat(service.checarAcesso(auth, 1L)).isFalse();
    }

    @Test
    @DisplayName("checarAcesso deve retornar false se unidades do usuário estiverem vazias")
    void checarAcesso_UnidadesVazias() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_GESTOR"))).when(auth).getAuthorities();

        PerfilDto perfil = new PerfilDto("123", null, "Unidade", "GESTOR", "Gestor");
        when(usuarioService.buscarPerfisUsuario("user")).thenReturn(List.of(perfil));

        assertThat(service.checarAcesso(auth, 1L)).isFalse();
    }
}
