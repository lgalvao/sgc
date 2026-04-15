package sgc.comum.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class RegistroSseEmitterTest {

    private RegistroSseEmitter registroSseEmitter;

    @BeforeEach
    void setUp() {
        registroSseEmitter = new RegistroSseEmitter();
    }

    @SuppressWarnings("unchecked")
    private CopyOnWriteArrayList<SseEmitter> obterEmissores() {
        return (CopyOnWriteArrayList<SseEmitter>) ReflectionTestUtils.getField(registroSseEmitter, "emissores");
    }

    @Test
    @DisplayName("deve registrar emissor e limpar ao concluir")
    void deveRegistrarEmissor() {
        SseEmitter emitter = registroSseEmitter.registrar();
        assertNotNull(emitter);

        CopyOnWriteArrayList<SseEmitter> emissores = obterEmissores();

        assertTrue(emissores.contains(emitter));

        emitter.complete();

        emissores.remove(emitter);
        assertFalse(emissores.contains(emitter));
    }

    @Test
    @DisplayName("deve registrar emissor e limpar ao dar timeout")
    void deveLimparEmissorNoTimeout() {
        SseEmitter emitter = registroSseEmitter.registrar();

        CopyOnWriteArrayList<SseEmitter> emissores = obterEmissores();
        assertTrue(emissores.contains(emitter));

        emissores.remove(emitter);
        assertFalse(emissores.contains(emitter));
    }

    @Test
    @DisplayName("deve transmitir evento com sucesso")
    void deveTransmitirComSucesso() throws IOException {
        SseEmitter emitterMock = mock(SseEmitter.class);

        CopyOnWriteArrayList<SseEmitter> emissores = obterEmissores();
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

        CopyOnWriteArrayList<SseEmitter> emissores = obterEmissores();
        emissores.add(emitterMock);

        registroSseEmitter.transmitir("meu-evento");

        verify(emitterMock).send(any(SseEmitter.SseEventBuilder.class));
        assertFalse(emissores.contains(emitterMock)); // Deve remover do cache se falhar
    }
}
