package sgc.comum.cache;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.transaction.support.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheOrganizacaoServiceTest {
    @Mock
    private AgendadorRefreshCache agendadorRefreshCache;

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
        cacheOrganizacaoService.invalidarAposCommit();

        verify(agendadorRefreshCache).evictarTodosCaches();
        verify(registroSseEmitter).transmitir("org-cache-refreshed");
    }

    @Test
    @DisplayName("deve aguardar afterCommit quando houver sincronização ativa")
    void deveAguardarAfterCommitComSincronizacaoAtiva() {
        TransactionSynchronizationManager.initSynchronization();

        cacheOrganizacaoService.invalidarAposCommit();

        verifyNoInteractions(agendadorRefreshCache, registroSseEmitter);

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

        verify(agendadorRefreshCache).evictarTodosCaches();
        verify(registroSseEmitter).transmitir("org-cache-refreshed");
    }
}
