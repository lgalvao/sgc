package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.cache.*;
import org.springframework.transaction.support.*;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheOrganizacaoServiceTest {
    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cacheOrganizacao;

    @Mock
    private RegistroSseEmitter registroSseEmitter;

    @InjectMocks
    private CacheOrganizacaoService cacheOrganizacaoService;

    @AfterEach
    void limparSincronizacao() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("deve invalidar imediatamente quando não houver transação ativa")
    void deveInvalidarImediatamenteSemTransacaoAtiva() {
        configurarCaches();

        cacheOrganizacaoService.invalidarAposCommit();

        verify(cacheOrganizacao).clear();
        verify(registroSseEmitter).transmitir("org-cache-refreshed");
    }

    @Test
    @DisplayName("deve aguardar afterCommit quando houver sincronização ativa")
    void deveAguardarAfterCommitComSincronizacaoAtiva() {
        TransactionSynchronizationManager.initSynchronization();
        configurarCaches();

        cacheOrganizacaoService.invalidarAposCommit();

        verifyNoInteractions(cacheOrganizacao, registroSseEmitter);

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

        verify(cacheOrganizacao).clear();
        verify(registroSseEmitter).transmitir("org-cache-refreshed");
    }

    @Test
    @DisplayName("deve ignorar cache nulo durante a invalidação")
    void deveIgnorarCacheNuloDuranteInvalidacao() {
        when(cacheManager.getCacheNames()).thenReturn(List.of("inexistente"));
        when(cacheManager.getCache("inexistente")).thenReturn(null);

        cacheOrganizacaoService.invalidarAposCommit();

        verify(registroSseEmitter).transmitir("org-cache-refreshed");
    }

    private void configurarCaches() {
        when(cacheManager.getCacheNames()).thenReturn(List.of("organizacao"));
        when(cacheManager.getCache("organizacao")).thenReturn(cacheOrganizacao);
    }
}
