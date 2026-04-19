package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import sgc.comum.config.*;
import sgc.organizacao.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = {CacheConfig.class, UsuarioPerfilCacheService.class})
@DisplayName("UsuarioPerfilCacheService")
class UsuarioPerfilCacheServiceTest {
    @Autowired
    private UsuarioPerfilCacheService usuarioPerfilCacheService;

    @MockitoBean
    private CacheViewsOrganizacaoService cacheViewsOrganizacaoService;

    @Test
    @DisplayName("Deve cachear perfis por titulo eleitoral")
    void deveCachearPerfisPorTituloEleitoral() {
        when(cacheViewsOrganizacaoService.listarTodosPerfisUnidade())
                .thenReturn(List.of(new UsuarioPerfilLeitura("123", 1L, Perfil.ADMIN)));

        List<Perfil> primeiraConsulta = usuarioPerfilCacheService.buscarPerfisPorUsuarioTitulo("123");
        List<Perfil> segundaConsulta = usuarioPerfilCacheService.buscarPerfisPorUsuarioTitulo("123");

        assertThat(primeiraConsulta).containsExactly(Perfil.ADMIN);
        assertThat(segundaConsulta).containsExactly(Perfil.ADMIN);
        verify(cacheViewsOrganizacaoService, times(1)).listarTodosPerfisUnidade();
    }

    @Test
    @DisplayName("Deve cachear autorizacoes por titulo eleitoral")
    void deveCachearAutorizacoesPorTituloEleitoral() {
        UsuarioPerfilAutorizacaoLeitura leitura = new UsuarioPerfilAutorizacaoLeitura(
                "123",
                Perfil.ADMIN,
                1L,
                "Secretaria",
                "SEASP",
                TipoUnidade.OPERACIONAL,
                SituacaoUnidade.ATIVA
        );
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of(UnidadeHierarquiaLeitura.builder()
                .codigo(1L)
                .nome("Secretaria")
                .sigla("SEASP")
                .tipo(TipoUnidade.OPERACIONAL)
                .situacao(SituacaoUnidade.ATIVA)
                .build()));
        when(cacheViewsOrganizacaoService.listarTodosPerfisUnidade())
                .thenReturn(List.of(new UsuarioPerfilLeitura("123", 1L, Perfil.ADMIN)));

        List<UsuarioPerfilAutorizacaoLeitura> primeiraConsulta = usuarioPerfilCacheService.buscarAutorizacoesPerfil("123");
        List<UsuarioPerfilAutorizacaoLeitura> segundaConsulta = usuarioPerfilCacheService.buscarAutorizacoesPerfil("123");

        assertThat(primeiraConsulta).containsExactly(leitura);
        assertThat(segundaConsulta).containsExactly(leitura);
        verify(cacheViewsOrganizacaoService, times(1)).listarTodasUnidades();
        verify(cacheViewsOrganizacaoService, times(1)).listarTodosPerfisUnidade();
    }

    @Test
    @DisplayName("Deve ignorar perfis de outros usuarios")
    void deveIgnorarPerfisDeOutrosUsuarios() {
        when(cacheViewsOrganizacaoService.listarTodosPerfisUnidade())
                .thenReturn(List.of(
                        new UsuarioPerfilLeitura("999", 1L, Perfil.ADMIN),
                        new UsuarioPerfilLeitura("456", 1L, Perfil.GESTOR)
                ));

        List<Perfil> perfis = usuarioPerfilCacheService.buscarPerfisPorUsuarioTitulo("999");

        assertThat(perfis).containsExactly(Perfil.ADMIN);
    }
}
