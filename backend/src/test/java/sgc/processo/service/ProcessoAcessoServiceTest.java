package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.service.*;
import sgc.testutils.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoAcessoService")
class ProcessoAcessoServiceTest {

    @InjectMocks
    private ProcessoAcessoService processoAcessoService;

    @Mock
    private OrganizacaoFacade unidadeService;

    @Mock
    private UsuarioFacade usuarioService;

    @Mock
    private ConsultasSubprocessoService queryService;

    @Test
    @DisplayName("Deve negar acesso se authentication for null")
    void deveNegarAcessoSeAuthNull() {
        assertThat(processoAcessoService.checarAcesso(null, 1L)).isFalse();
    }

    @Test
    @DisplayName("Deve negar acesso se usuário não for GESTOR nem CHEFE")
    void deveNegarAcessoSeNaoGestorOuChefe() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user");
        when(auth.getAuthorities()).thenAnswer(m -> List.of(new SimpleGrantedAuthority("ROLE_USER")));

        assertThat(processoAcessoService.checarAcesso(auth, 1L)).isFalse();
    }

    @Test
    @DisplayName("Deve negar acesso se usuário não tem unidade vinculada")
    void deveNegarAcessoSemUnidade() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user");
        when(auth.getAuthorities()).thenAnswer(m -> List.of(new SimpleGrantedAuthority("ROLE_GESTOR")));

        when(usuarioService.buscarPerfisUsuario("user")).thenReturn(List.of());

        assertThat(processoAcessoService.checarAcesso(auth, 1L)).isFalse();
    }

    @Test
    @DisplayName("Deve permitir acesso se unidade do usuário ou descendente tem acesso ao processo")
    void devePermitirAcessoComHierarquia() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("gestor");
        when(auth.getAuthorities()).thenAnswer(m -> List.of(new SimpleGrantedAuthority("ROLE_GESTOR")));

        when(usuarioService.buscarPerfisUsuario("gestor")).thenReturn(List.of(
                PerfilDto.builder().unidadeCodigo(1L).build()
        ));

        // Mock hierarquia: 1 -> 101 -> 102
        Unidade u100 = UnidadeTestBuilder.umaDe().comCodigo("1").build();
        Unidade u101 = UnidadeTestBuilder.umaDe().comCodigo("101").comSuperior(u100).build();
        Unidade u102 = UnidadeTestBuilder.umaDe().comCodigo("102").comSuperior(u101).build();

        when(unidadeService.unidadesComHierarquia()).thenReturn(List.of(u100, u101, u102));

        when(queryService.verificarAcessoUnidadeAoProcesso(eq(1L), anyList())).thenAnswer(invocation -> {
            List<Long> ids = invocation.getArgument(1);
            return ids.contains(1L) && ids.contains(101L) && ids.contains(102L);
        });

        assertThat(processoAcessoService.checarAcesso(auth, 1L)).isTrue();
    }

    @Test
    @DisplayName("Deve encontrar corretamente todos os descendentes")
    void deveBuscarCodigosDescendentes() {
        Unidade u1 = UnidadeTestBuilder.umaDe().comCodigo("1").build();
        Unidade u2 = UnidadeTestBuilder.umaDe().comCodigo("2").comSuperior(u1).build();
        Unidade u3 = UnidadeTestBuilder.umaDe().comCodigo("3").comSuperior(u1).build();
        Unidade u4 = UnidadeTestBuilder.umaDe().comCodigo("4").comSuperior(u2).build();
        Unidade u5 = UnidadeTestBuilder.umaDe().comCodigo("5").build(); // Independente

        when(unidadeService.unidadesComHierarquia()).thenReturn(List.of(u1, u2, u3, u4, u5));

        List<Long> descendentes = processoAcessoService.buscarCodigosDescendentes(1L);

        assertThat(descendentes).hasSize(4).containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
    }

    @Test
    @DisplayName("Deve negar acesso se authentication não estiver autenticado")
    void deveNegarAcessoSeAuthNaoAutenticado() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        assertThat(processoAcessoService.checarAcesso(auth, 1L)).isFalse();
    }

    @Test
    @DisplayName("Deve negar acesso se username for null")
    void deveNegarAcessoSeUsernameNull() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(null);

        assertThat(processoAcessoService.checarAcesso(auth, 1L)).isFalse();
    }

    @Test
    @DisplayName("Deve negar acesso se unidade do usuário for nula")
    void deveNegarAcessoSeUnidadeDoUsuarioForNula() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("gestor");
        when(auth.getAuthorities()).thenAnswer(m -> List.of(new SimpleGrantedAuthority("ROLE_GESTOR")));

        when(usuarioService.buscarPerfisUsuario("gestor")).thenReturn(List.of(
                PerfilDto.builder().unidadeCodigo(null).build()
        ));

        assertThat(processoAcessoService.checarAcesso(auth, 1L)).isFalse();
    }

    @Test
    @DisplayName("Deve lidar com ciclos na hierarquia (evitar loop infinito)")
    void deveEvitarCicloInifinitoEmHierarquia() {

        Unidade u1 = UnidadeTestBuilder.umaDe().comCodigo("1").build();
        Unidade u2 = UnidadeTestBuilder.umaDe().comCodigo("2").build();

        u1.setUnidadeSuperior(u2);
        u2.setUnidadeSuperior(u1);

        when(unidadeService.unidadesComHierarquia()).thenReturn(List.of(u1, u2));

        List<Long> descendentes = processoAcessoService.buscarCodigosDescendentes(1L);

        // Deve retornar ambos e parar
        assertThat(descendentes).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("Deve permitir acesso quando usuário tem múltiplos perfis e um deles permite")
    void devePermitirAcessoQuandoUsuarioTemMultiplosPerfisEUmDelesPermite() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("multi_perfil_user");
        when(auth.getAuthorities()).thenAnswer(m -> List.of(new SimpleGrantedAuthority("ROLE_GESTOR")));


        // A implementação com bug pegaria apenas o primeiro (1) e negaria o acesso.
        when(usuarioService.buscarPerfisUsuario("multi_perfil_user")).thenReturn(List.of(
                PerfilDto.builder().unidadeCodigo(1L).build(),
                PerfilDto.builder().unidadeCodigo(200L).build()
        ));

        Unidade u100 = UnidadeTestBuilder.umaDe().comCodigo("1").build();
        Unidade u200 = UnidadeTestBuilder.umaDe().comCodigo("200").build();

        when(unidadeService.unidadesComHierarquia()).thenReturn(List.of(u100, u200));


        // Se a lista de IDs conter 200, acesso é permitido. Se tiver apenas 100, negado.
        when(queryService.verificarAcessoUnidadeAoProcesso(eq(1L), anyList())).thenAnswer(invocation -> {
            List<Long> ids = invocation.getArgument(1);
            return ids.contains(200L);
        });

        assertThat(processoAcessoService.checarAcesso(auth, 1L)).isTrue();
    }
}
