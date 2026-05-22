package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.springframework.web.servlet.mvc.method.annotation.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistroSseEmitterTest {

    private RegistroSseEmitter registroSseEmitter;

    @BeforeEach
    void setUp() {
        registroSseEmitter = new RegistroSseEmitter();
    }

    @Test
    @DisplayName("deve retornar emissor válido ao registrar")
    void deveRetornarEmissorValido() {
        SseEmitter emitter = registroSseEmitter.registrar();

        assertThat(emitter).isNotNull();
    }

    @Test
    @DisplayName("deve transmitir evento sem erro para emissores registrados")
    void deveTransmitirEventoSemErro() {
        registroSseEmitter.registrar();

        assertThatCode(() -> registroSseEmitter.transmitir("meu-evento"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve transmitir evento sem erro quando não há emissores registrados")
    void deveTransmitirSemEmissores() {
        assertThatCode(() -> registroSseEmitter.transmitir("meu-evento"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve remover emissor quando ocorrer IOException no envio")
    void deveRemoverEmissorQuandoOcorrerIOExceptionNoEnvio() throws Exception {
        SseEmitter emitterComFalha = mock(SseEmitter.class);
        java.lang.reflect.Field campo = RegistroSseEmitter.class.getDeclaredField("emissores");
        campo.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.CopyOnWriteArrayList<SseEmitter> emissores =
                (java.util.concurrent.CopyOnWriteArrayList<SseEmitter>) campo.get(registroSseEmitter);
        emissores.add(emitterComFalha);
        doThrow(new java.io.IOException("falha")).when(emitterComFalha).send(any(SseEmitter.SseEventBuilder.class));

        assertThatCode(() -> registroSseEmitter.transmitir("meu-evento")).doesNotThrowAnyException();

        assertThat(emissores).isEmpty();
    }
}
