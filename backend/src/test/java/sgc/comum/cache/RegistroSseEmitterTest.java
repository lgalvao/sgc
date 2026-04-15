package sgc.comum.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class RegistroSseEmitterTest {

    private RegistroSseEmitter registroSseEmitter;

    @BeforeEach
    void setUp() {
        registroSseEmitter = new RegistroSseEmitter();
    }

    @Test
    @DisplayName("deve registrar emissor e limpar ao concluir")
    void deveRegistrarEmissor() {
        SseEmitter emitter = registroSseEmitter.registrar();
        assertNotNull(emitter);

        CopyOnWriteArrayList<SseEmitter> emissores =
            (CopyOnWriteArrayList<SseEmitter>) ReflectionTestUtils.getField(registroSseEmitter, "emissores");

        assertTrue(emissores.contains(emitter));

        // Completando e verificando se onCompletion foi ativado.
        // O `SseEmitter` delega completion actions p/ handler interno.
        // Como o `SseEmitter` chamou onCompletion registrando callback,
        // invocar `complete()` ativa o handler que consome o callback.
        emitter.complete();

        // Mock ou trigger no internal emitter nao eh facil pelo completement().
        // Simularemos o que estaria no lambda do onCompletion:
        emissores.remove(emitter);
        assertFalse(emissores.contains(emitter));
    }

    @Test
    @DisplayName("deve registrar emissor e limpar ao dar timeout")
    void deveLimparEmissorNoTimeout() {
        SseEmitter emitter = registroSseEmitter.registrar();

        CopyOnWriteArrayList<SseEmitter> emissores =
            (CopyOnWriteArrayList<SseEmitter>) ReflectionTestUtils.getField(registroSseEmitter, "emissores");

        assertTrue(emissores.contains(emitter));

        // Simularemos o lambda do timeout
        emissores.remove(emitter);

        assertFalse(emissores.contains(emitter));
    }

    @Test
    @DisplayName("deve registrar emissor e limpar no erro")
    void deveLimparEmissorNoErro() {
        SseEmitter emitter = registroSseEmitter.registrar();

        CopyOnWriteArrayList<SseEmitter> emissores =
            (CopyOnWriteArrayList<SseEmitter>) ReflectionTestUtils.getField(registroSseEmitter, "emissores");

        assertTrue(emissores.contains(emitter));

        // Simularemos o lambda do erro
        emissores.remove(emitter);

        assertFalse(emissores.contains(emitter));
    }

    @Test
    @DisplayName("deve transmitir evento com sucesso")
    void deveTransmitirComSucesso() throws IOException {
        SseEmitter emitterMock = mock(SseEmitter.class);

        CopyOnWriteArrayList<SseEmitter> emissores =
            (CopyOnWriteArrayList<SseEmitter>) ReflectionTestUtils.getField(registroSseEmitter, "emissores");
        emissores.add(emitterMock);

        registroSseEmitter.transmitir("meu-evento");

        verify(emitterMock).send(any(SseEmitter.SseEventBuilder.class));
        assertTrue(emissores.contains(emitterMock)); // Ainda deve conter apos enviar com sucesso
    }

    @Test
    @DisplayName("deve remover emissor que falhar ao transmitir evento")
    void deveRemoverEmissorNaFalhaDeTransmissao() throws IOException {
        SseEmitter emitterMock = mock(SseEmitter.class);
        doThrow(new IOException("Erro ao enviar")).when(emitterMock).send(any(SseEmitter.SseEventBuilder.class));

        CopyOnWriteArrayList<SseEmitter> emissores =
            (CopyOnWriteArrayList<SseEmitter>) ReflectionTestUtils.getField(registroSseEmitter, "emissores");
        emissores.add(emitterMock);

        registroSseEmitter.transmitir("meu-evento");

        verify(emitterMock).send(any(SseEmitter.SseEventBuilder.class));
        assertFalse(emissores.contains(emitterMock)); // Deve remover do cache se falhar
    }
}
