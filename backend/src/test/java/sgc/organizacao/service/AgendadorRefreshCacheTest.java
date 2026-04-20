package sgc.organizacao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendadorRefreshCacheTest {

    @Mock
    private CacheViewsOrganizacaoService cacheViewsOrganizacaoService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private RegistroSseEmitter registroSseEmitter;

    @InjectMocks
    private AgendadorRefreshCache agendadorRefreshCache;

    @Test
    @DisplayName("deve atualizar tudo com sucesso")
    void deveAtualizarTudoComSucesso() {
        Cache cacheMock = mock(Cache.class);
        when(cacheManager.getCache(anyString())).thenReturn(cacheMock);

        agendadorRefreshCache.atualizarTudo();

        verify(cacheViewsOrganizacaoService).evictarUnidades();
        verify(cacheViewsOrganizacaoService).evictarUsuarios();
        verify(cacheViewsOrganizacaoService).evictarResponsabilidades();
        verify(cacheViewsOrganizacaoService).evictarPerfisUnidade();

        verify(cacheMock, times(9)).clear(); // 9 derived caches are cleared

        verify(cacheViewsOrganizacaoService).listarTodasUnidades();
        verify(cacheViewsOrganizacaoService).listarTodosUsuarios();
        verify(cacheViewsOrganizacaoService).listarTodasResponsabilidades();
        verify(cacheViewsOrganizacaoService, never()).listarTodosPerfisUnidade();

        verify(registroSseEmitter).transmitir("org-cache-refreshed");
    }

    @Test
    @DisplayName("nao deve quebrar se cache derivado nao for encontrado")
    void naoDeveQuebrarSeCacheDerivadoNaoEncontrado() {
        when(cacheManager.getCache(anyString())).thenReturn(null);

        agendadorRefreshCache.evictarTodosCaches();

        verify(cacheManager, times(9)).getCache(anyString());
    }

    @Test
    @DisplayName("deve lidar com excecoes durante atualizacao")
    void deveLidarComExcecoesDuranteAtualizacao() {
        doThrow(new RuntimeException("Erro forçado")).when(cacheViewsOrganizacaoService).evictarUnidades();

        agendadorRefreshCache.atualizarTudo();

        verify(cacheViewsOrganizacaoService).evictarUnidades();
        verify(cacheViewsOrganizacaoService, never()).evictarUsuarios();
        verify(registroSseEmitter, never()).transmitir(anyString());
    }
}
