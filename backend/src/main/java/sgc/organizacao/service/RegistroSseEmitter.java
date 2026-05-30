package sgc.organizacao.service;

import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.web.servlet.mvc.method.annotation.*;

import java.io.*;
import java.util.concurrent.*;

/**
 * Registro centralizado de emissores SSE.
 *
 * <p>Mantém a lista de clientes conectados e provê métodos para
 * registrar novos emissores e transmitir eventos a todos eles.
 */
@Component
@Slf4j
public class RegistroSseEmitter {

    /** Timeout zero indica conexão permanente (sem expiração automática). */
    private static final long SEM_TIMEOUT = 0L;

    private final CopyOnWriteArrayList<SseEmitter> emissores = new CopyOnWriteArrayList<>();

    @FunctionalInterface
    public interface SseSender {
        void send(SseEmitter emitter, String evento) throws IOException;
    }

    private final SseSender sender;

    public RegistroSseEmitter() {
        this.sender = (emitter, evento) -> emitter.send(SseEmitter.event().name(evento).data(""));
    }

    // Construtor package-private para testes
    RegistroSseEmitter(SseSender sender) {
        this.sender = sender;
    }

    /**
     * Registra um novo emissor SSE para o cliente conectado.
     */
    public SseEmitter registrar() {
        SseEmitter emitter = new SseEmitter(SEM_TIMEOUT);
        emissores.add(emitter);
        emitter.onCompletion(() -> emissores.remove(emitter));
        emitter.onTimeout(() -> emissores.remove(emitter));
        emitter.onError(e -> emissores.remove(emitter));
        return emitter;
    }

    /**
     * Transmite um evento para todos os clientes conectados.
     * Emissores com falha são removidos automaticamente.
     */
    public void transmitir(String evento) {
        for (SseEmitter emitter : emissores) {
            try {
                sender.send(emitter, evento);
            } catch (IOException e) {
                emissores.remove(emitter);
                log.debug("Emissor SSE removido após erro de envio: {}", e.getMessage());
            }
        }
    }

    // Método package-private observável para testes (Padrão 3)
    int obterQuantidadeEmissores() {
        return emissores.size();
    }
}
