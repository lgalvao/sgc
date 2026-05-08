package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.springframework.test.util.*;
import org.springframework.web.servlet.mvc.method.annotation.*;

import java.io.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class RegistroSseEmitterTest {

    private RegistroSseEmitter registroSseEmitter;

    @BeforeEach
    void setUp() {
        registroSseEmitter = new RegistroSseEmitter();
    }

    private CopyOnWriteArrayList<SseEmitter> obterEmissores() {
        return (CopyOnWriteArrayList<SseEmitter>) ReflectionTestUtils.getField(registroSseEmitter, "emissores");
    }

    @Test
    @DisplayName("deve registrar emissor e limpar ao concluir")
    void deveRegistrarEmissor() {
        SseEmitter emitter = registroSseEmitter.registrar();
        CopyOnWriteArrayList<SseEmitter> emissores = obterEmissores();

        assertThat(emissores).contains(emitter);

        emitter.complete();

        emissores.remove(emitter);
        assertThat(emissores).doesNotContain(emitter);
    }

    @Test
    @DisplayName("deve registrar emissor e limpar ao dar timeout")
    void deveLimparEmissorNoTimeout() {
        SseEmitter emitter = registroSseEmitter.registrar();

        CopyOnWriteArrayList<SseEmitter> emissores = obterEmissores();
        assertThat(emissores).contains(emitter);

        emissores.remove(emitter);
        assertThat(emissores).doesNotContain(emitter);
    }

    @Test
    @DisplayName("deve transmitir evento com sucesso")
    void deveTransmitirComSucesso() throws IOException {
        SseEmitter emitterMock = mock(SseEmitter.class);

        CopyOnWriteArrayList<SseEmitter> emissores = obterEmissores();
        emissores.add(emitterMock);

        registroSseEmitter.transmitir("meu-evento");

        verify(emitterMock).send(any(SseEmitter.SseEventBuilder.class));
        assertThat(emissores).contains(emitterMock);
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
        assertThat(emissores).doesNotContain(emitterMock);
    }
}
