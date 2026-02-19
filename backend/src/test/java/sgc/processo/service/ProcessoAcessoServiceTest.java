package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.service.ConsultasSubprocessoService;
import sgc.testutils.UnidadeTestBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoAcessoService")
class ProcessoAcessoServiceTest {

    @InjectMocks
    private ProcessoAcessoService processoAcessoService;

    @Mock
    private UnidadeFacade unidadeService;

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
                PerfilDto.builder().unidadeCodigo(100L).build()
        ));

        // Mock hierarquia: 100 -> 101 -> 102
        Unidade u100 = UnidadeTestBuilder.umaDe().comCodigo("100").build();
        Unidade u101 = UnidadeTestBuilder.umaDe().comCodigo("101").comSuperior(u100).build();
        Unidade u102 = UnidadeTestBuilder.umaDe().comCodigo("102").comSuperior(u101).build();

        when(unidadeService.buscarTodasEntidadesComHierarquia()).thenReturn(List.of(u100, u101, u102));

        when(queryService.verificarAcessoUnidadeAoProcesso(eq(1L), anyList())).thenAnswer(invocation -> {
            List<Long> ids = invocation.getArgument(1);
            return ids.contains(100L) && ids.contains(101L) && ids.contains(102L);
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

        when(unidadeService.buscarTodasEntidadesComHierarquia()).thenReturn(List.of(u1, u2, u3, u4, u5));

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
        // U1 -> U2 -> U1
        Unidade u1 = UnidadeTestBuilder.umaDe().comCodigo("1").build();
        Unidade u2 = UnidadeTestBuilder.umaDe().comCodigo("2").build();

        u1.setUnidadeSuperior(u2);
        u2.setUnidadeSuperior(u1);

        when(unidadeService.buscarTodasEntidadesComHierarquia()).thenReturn(List.of(u1, u2));

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

        // Mock: Retorna Unidade 100 (Sem acesso) e depois Unidade 200 (Com acesso)
        // A implementação com bug pegaria apenas o primeiro (100) e negaria o acesso.
        when(usuarioService.buscarPerfisUsuario("multi_perfil_user")).thenReturn(List.of(
                PerfilDto.builder().unidadeCodigo(100L).build(),
                PerfilDto.builder().unidadeCodigo(200L).build()
        ));

        Unidade u100 = UnidadeTestBuilder.umaDe().comCodigo("100").build();
        Unidade u200 = UnidadeTestBuilder.umaDe().comCodigo("200").build();

        when(unidadeService.buscarTodasEntidadesComHierarquia()).thenReturn(List.of(u100, u200));

        // Mock verification:
        // Se a lista de IDs conter 200, acesso é permitido. Se tiver apenas 100, negado.
        when(queryService.verificarAcessoUnidadeAoProcesso(eq(1L), anyList())).thenAnswer(invocation -> {
            List<Long> ids = invocation.getArgument(1);
            return ids.contains(200L);
        });

        assertThat(processoAcessoService.checarAcesso(auth, 1L)).isTrue();
    }
}
