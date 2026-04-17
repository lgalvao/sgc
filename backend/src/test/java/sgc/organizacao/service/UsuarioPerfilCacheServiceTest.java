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
    @DisplayName("Deve cachear perfis por titulo eleitoral")
    void deveCachearPerfisPorTituloEleitoral() {
        when(usuarioPerfilRepo.listarPerfisPorUsuarioTitulo("123")).thenReturn(List.of(Perfil.ADMIN));

        List<Perfil> primeiraConsulta = usuarioPerfilCacheService.buscarPerfisPorUsuarioTitulo("123");
        List<Perfil> segundaConsulta = usuarioPerfilCacheService.buscarPerfisPorUsuarioTitulo("123");

        assertThat(primeiraConsulta).containsExactly(Perfil.ADMIN);
        assertThat(segundaConsulta).containsExactly(Perfil.ADMIN);
        verify(usuarioPerfilRepo, times(1)).listarPerfisPorUsuarioTitulo("123");
    }

    @Test
    @DisplayName("Deve cachear autorizacoes por titulo eleitoral")
    void deveCachearAutorizacoesPorTituloEleitoral() {
        UsuarioPerfilAutorizacaoLeitura leitura = UsuarioPerfilAutorizacaoLeitura.builder()
                .usuarioTitulo("123")
                .perfil(Perfil.ADMIN)
                .unidadeCodigo(1L)
                .unidadeNome("Secretaria")
                .unidadeSigla("SEASP")
                .build();
        when(usuarioPerfilRepo.listarAutorizacoesPorUsuarioTitulo("123")).thenReturn(List.of(leitura));

        List<UsuarioPerfilAutorizacaoLeitura> primeiraConsulta = usuarioPerfilCacheService.buscarAutorizacoesPerfil("123");
        List<UsuarioPerfilAutorizacaoLeitura> segundaConsulta = usuarioPerfilCacheService.buscarAutorizacoesPerfil("123");

        assertThat(primeiraConsulta).containsExactly(leitura);
        assertThat(segundaConsulta).containsExactly(leitura);
        verify(usuarioPerfilRepo, times(1)).listarAutorizacoesPorUsuarioTitulo("123");
    }
}
