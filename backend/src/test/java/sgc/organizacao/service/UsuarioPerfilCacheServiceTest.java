package sgc.organizacao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import sgc.comum.config.CacheConfig;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.UsuarioPerfilAutorizacaoLeitura;
import sgc.organizacao.model.UsuarioPerfilRepo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        UsuarioPerfilAutorizacaoLeitura leitura = new UsuarioPerfilAutorizacaoLeitura(
                "123",
                Perfil.ADMIN,
                1L,
                "Secretaria",
                "SEASP",
                null,
                null
        );
        when(usuarioPerfilRepo.listarAutorizacoesPorUsuarioTitulo("123")).thenReturn(List.of(leitura));

        List<UsuarioPerfilAutorizacaoLeitura> primeiraConsulta = usuarioPerfilCacheService.buscarAutorizacoesPerfil("123");
        List<UsuarioPerfilAutorizacaoLeitura> segundaConsulta = usuarioPerfilCacheService.buscarAutorizacoesPerfil("123");

        assertThat(primeiraConsulta).containsExactly(leitura);
        assertThat(segundaConsulta).containsExactly(leitura);
        verify(usuarioPerfilRepo, times(1)).listarAutorizacoesPorUsuarioTitulo("123");
    }
}
