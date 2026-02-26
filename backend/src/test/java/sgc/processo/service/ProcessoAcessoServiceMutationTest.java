package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
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
