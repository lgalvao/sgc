package sgc.organizacao;

import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.*;
import sgc.comum.cache.*;

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
    public SseEmitter assinar() {
        return registroSseEmitter.registrar();
    }
}
