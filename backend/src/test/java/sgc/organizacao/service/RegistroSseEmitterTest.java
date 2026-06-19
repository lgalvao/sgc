package sgc.organizacao.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

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
        assertThat(registroSseEmitter.obterQuantidadeEmissores()).isEqualTo(1);
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
    void deveRemoverEmissorQuandoOcorrerIOExceptionNoEnvio() {
        // Usamos o construtor package-private para injetar um SseSender que lança IOException
        RegistroSseEmitter.SseSender senderComFalha = (emitter, evento) -> {
            throw new IOException("falha simulada no envio");
        };

        RegistroSseEmitter sseComFalha = new RegistroSseEmitter(senderComFalha);
        sseComFalha.registrar();

        assertThat(sseComFalha.obterQuantidadeEmissores()).isEqualTo(1);

        assertThatCode(() -> sseComFalha.transmitir("meu-evento"))
                .doesNotThrowAnyException();

        assertThat(sseComFalha.obterQuantidadeEmissores()).isZero();
    }
}
