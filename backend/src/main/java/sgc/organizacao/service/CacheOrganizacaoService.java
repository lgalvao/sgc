package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
