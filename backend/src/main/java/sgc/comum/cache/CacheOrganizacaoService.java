package sgc.comum.cache;

import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.support.*;

@Service
@RequiredArgsConstructor
public class CacheOrganizacaoService {
    private static final String EVENTO_CACHE_ATUALIZADO = "org-cache-refreshed";

    private final AgendadorRefreshCache agendadorRefreshCache;
    private final RegistroSseEmitter registroSseEmitter;

    public void invalidarAposCommit() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            invalidarAgora();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                invalidarAgora();
            }
        });
    }

    private void invalidarAgora() {
        agendadorRefreshCache.evictarTodosCaches();
        registroSseEmitter.transmitir(EVENTO_CACHE_ATUALIZADO);
    }
}
