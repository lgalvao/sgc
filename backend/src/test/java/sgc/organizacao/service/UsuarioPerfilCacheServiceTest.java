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
    private UsuarioPerfilRepo usuarioPerfilRepo;

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
        when(usuarioPerfilRepo.listarAutorizacoesPorTitulo("123")).thenReturn(List.of(leitura));

        List<UsuarioPerfilAutorizacaoLeitura> primeiraConsulta = usuarioPerfilCacheService.buscarAutorizacoesPerfil("123");
        List<UsuarioPerfilAutorizacaoLeitura> segundaConsulta = usuarioPerfilCacheService.buscarAutorizacoesPerfil("123");

        assertThat(primeiraConsulta).containsExactly(leitura);
        assertThat(segundaConsulta).containsExactly(leitura);
        verify(usuarioPerfilRepo, times(1)).listarAutorizacoesPorTitulo("123");
    }

    @Test
    @DisplayName("Deve ignorar perfis de outros usuarios")
    void deveIgnorarPerfisDeOutrosUsuarios() {
        when(usuarioPerfilRepo.listarAutorizacoesPorTitulo("999")).thenReturn(List.of(
                new UsuarioPerfilAutorizacaoLeitura("999", Perfil.ADMIN, 1L, null, null, null, null)
        ));

        List<UsuarioPerfilAutorizacaoLeitura> perfis = usuarioPerfilCacheService.buscarAutorizacoesPerfil("999");

        assertThat(perfis)
                .extracting(UsuarioPerfilAutorizacaoLeitura::perfil)
                .containsExactly(Perfil.ADMIN);
        verify(usuarioPerfilRepo).listarAutorizacoesPorTitulo("999");
    }
}
