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
    @DisplayName("Deve cachear autorizacoes por titulo eleitoral")
    void deveCachearAutorizacoesPorTituloEleitoral() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of(
                new UnidadeHierarquiaLeitura(
                        1L,
                        "Secretaria",
                        "SEASP",
                        null,
                        TipoUnidade.OPERACIONAL,
                        SituacaoUnidade.ATIVA,
                        null
                )
        ));
        when(cacheViewsOrganizacaoService.listarTodosPerfisUnidade()).thenReturn(List.of(
                new UsuarioPerfilLeitura("123", 1L, Perfil.ADMIN)
        ));

        UsuarioPerfilAutorizacaoLeitura leituraEsperada = new UsuarioPerfilAutorizacaoLeitura(
                "123", Perfil.ADMIN,
                1L,
                "Secretaria",
                "SEASP",
                TipoUnidade.OPERACIONAL,
                SituacaoUnidade.ATIVA
        );

        List<UsuarioPerfilAutorizacaoLeitura> primeiraConsulta = usuarioPerfilCacheService.buscarAutorizacoesPerfil("123");
        List<UsuarioPerfilAutorizacaoLeitura> segundaConsulta = usuarioPerfilCacheService.buscarAutorizacoesPerfil("123");

        assertThat(primeiraConsulta).containsExactly(leituraEsperada);
        assertThat(segundaConsulta).containsExactly(leituraEsperada);
        verify(cacheViewsOrganizacaoService, times(1)).listarTodasUnidades();
        verify(cacheViewsOrganizacaoService, times(1)).listarTodosPerfisUnidade();
    }

    @Test
    @DisplayName("Deve ignorar perfis de outros usuarios")
    void deveIgnorarPerfisDeOutrosUsuarios() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of());
        when(cacheViewsOrganizacaoService.listarTodosPerfisUnidade()).thenReturn(List.of(
                new UsuarioPerfilLeitura("123", 1L, Perfil.CHEFE),
                new UsuarioPerfilLeitura("999", 1L, Perfil.ADMIN)
        ));

        List<UsuarioPerfilAutorizacaoLeitura> perfis = usuarioPerfilCacheService.buscarAutorizacoesPerfil("999");

        assertThat(perfis)
                .extracting(UsuarioPerfilAutorizacaoLeitura::perfil)
                .containsExactly(Perfil.ADMIN);
        verify(cacheViewsOrganizacaoService).listarTodosPerfisUnidade();
    }
}
