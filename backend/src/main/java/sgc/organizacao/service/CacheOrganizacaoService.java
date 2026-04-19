package sgc.organizacao.service;

import lombok.*;
import org.springframework.cache.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.support.*;

@Service
@RequiredArgsConstructor
public class CacheOrganizacaoService {
    private static final String EVENTO_CACHE_ATUALIZADO = "org-cache-refreshed";

    private final CacheManager cacheManager;
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
        cacheManager.getCacheNames().forEach(nome -> {
            Cache cache = cacheManager.getCache(nome);
            if (cache != null) {
                cache.clear();
            }
        });
        registroSseEmitter.transmitir(EVENTO_CACHE_ATUALIZADO);
    }
}
