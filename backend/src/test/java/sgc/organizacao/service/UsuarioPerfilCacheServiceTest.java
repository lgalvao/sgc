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

    @MockitoBean
    private UsuarioPerfilRepo usuarioPerfilRepo;

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
        when(usuarioPerfilRepo.findByUsuarioTitulo("123")).thenReturn(List.of(
                new UsuarioPerfil("123", 1L, Perfil.ADMIN)
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
        verify(usuarioPerfilRepo, times(1)).findByUsuarioTitulo("123");
    }

    @Test
    @DisplayName("Deve ignorar perfis de outros usuarios")
    void deveIgnorarPerfisDeOutrosUsuarios() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of());
        when(usuarioPerfilRepo.findByUsuarioTitulo("999")).thenReturn(List.of(
                new UsuarioPerfil("999", 1L, Perfil.ADMIN)
        ));

        List<UsuarioPerfilAutorizacaoLeitura> perfis = usuarioPerfilCacheService.buscarAutorizacoesPerfil("999");

        assertThat(perfis)
                .extracting(UsuarioPerfilAutorizacaoLeitura::perfil)
                .containsExactly(Perfil.ADMIN);
        verify(usuarioPerfilRepo).findByUsuarioTitulo("999");
    }
}
