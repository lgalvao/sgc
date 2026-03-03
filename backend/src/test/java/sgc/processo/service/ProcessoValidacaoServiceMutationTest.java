package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoValidacaoService - Cobertura de Mutações")
class ProcessoValidacaoServiceMutationTest {

    @InjectMocks
    private ProcessoValidacaoService service;

    @Mock
    private UnidadeService unidadeService;

    @Mock
    private UsuarioFacade usuarioService;

    @Mock
    private SubprocessoValidacaoService validacaoService;

    @Mock
    private ProcessoRepo processoRepo;

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
