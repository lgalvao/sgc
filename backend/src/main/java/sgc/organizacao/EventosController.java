package sgc.organizacao;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import sgc.organizacao.service.RegistroSseEmitter;

/**
 * Endpoint SSE para notificações de atualização de cache.
 *
 * <p>Clientes Vue se conectam a este endpoint para receber eventos
 * quando os caches organizacionais são atualizados, permitindo que
 * os stores Pinia recarreguem seus dados automaticamente.
 */
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventosController {

    private final RegistroSseEmitter registroSseEmitter;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter assinar() {
        return registroSseEmitter.registrar();
    }
}
