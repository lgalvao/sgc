package sgc.organizacao;

import lombok.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.web.bind.annotation.*;
import sgc.comum.cache.*;

/**
 * Endpoint administrativo para invalidação e recarga manual dos caches.
 *
 * <p>Reservado para administradores. Após recarregar os caches, transmite
 * um evento SSE para que os clientes Vue atualizem seus stores Pinia.
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheAdminController {

    private final AgendadorRefreshCache agendadorRefreshCache;
    private final RegistroSseEmitter registroSseEmitter;

    @PostMapping("/evict")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> evictarTudo() {
        agendadorRefreshCache.evictarTodosCaches();
        agendadorRefreshCache.recarregarCaches();
        registroSseEmitter.transmitir("org-cache-refreshed");
        return ResponseEntity.noContent().build();
    }
}
