package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.cache.*;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - invalidação de cache")
class UsuarioServiceCacheInvalidacaoTest {
    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private AdministradorRepo administradorRepo;

    @Mock
    private UsuarioPerfilCacheService usuarioPerfilCacheService;

    @Mock
    private CacheOrganizacaoService cacheOrganizacaoService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("adicionarAdministrador deve solicitar invalidação organizacional")
    void adicionarAdministradorDeveSolicitarInvalidacaoOrganizacional() {
        when(administradorRepo.existsById("123")).thenReturn(false);

        usuarioService.adicionarAdministrador("123");

        verify(administradorRepo).save(argThat(administrador -> "123".equals(administrador.getUsuarioTitulo())));
        verify(cacheOrganizacaoService).invalidarAposCommit();
    }

    @Test
    @DisplayName("adicionarAdministrador não deve invalidar quando usuário já é administrador")
    void adicionarAdministradorNaoDeveInvalidarQuandoJaAdministrador() {
        when(administradorRepo.existsById("123")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.adicionarAdministrador("123"))
                .isInstanceOf(ErroValidacao.class);

        verifyNoInteractions(cacheOrganizacaoService);
    }

    @Test
    @DisplayName("removerAdministrador deve solicitar invalidação organizacional")
    void removerAdministradorDeveSolicitarInvalidacaoOrganizacional() {
        when(administradorRepo.count()).thenReturn(2L);

        usuarioService.removerAdministrador("123");

        verify(administradorRepo).deleteById("123");
        verify(cacheOrganizacaoService).invalidarAposCommit();
    }

    @Test
    @DisplayName("removerAdministrador não deve invalidar quando é o único administrador")
    void removerAdministradorNaoDeveInvalidarQuandoUnicoAdministrador() {
        when(administradorRepo.count()).thenReturn(1L);

        assertThatThrownBy(() -> usuarioService.removerAdministrador("123"))
                .isInstanceOf(ErroValidacao.class);

        verifyNoInteractions(cacheOrganizacaoService);
    }
}
