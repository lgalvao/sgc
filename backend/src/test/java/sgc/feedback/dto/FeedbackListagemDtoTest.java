package sgc.feedback.dto;

import org.junit.jupiter.api.*;
import sgc.feedback.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FeedbackListagemDto")
class FeedbackListagemDtoTest {

    @Test
    @DisplayName("deve mapear corretamente de FeedbackRegistro")
    void deveMapearDeRegistro() {
        UUID id = UUID.randomUUID();
        OffsetDateTime agora = OffsetDateTime.now();
        FeedbackRegistro registro = FeedbackRegistro.builder()
                .id(id)
                .tipo(FeedbackTipo.BUG)
                .nota("Uma nota de teste")
                .metadataJson("{\"foo\":\"bar\"}")
                .caminhoScreenshot("arquivo.webp")
                .usuarioId("12345")
                .usuarioNome("João Silva")
                .enviadoEm(agora)
                .rota("/minha-rota")
                .status(FeedbackStatus.NOVO)
                .build();

        FeedbackListagemDto dto = FeedbackListagemDto.from(registro, true);

        assertThat(dto.codigo()).isEqualTo(id);
        assertThat(dto.tipo()).isEqualTo(FeedbackTipo.BUG);
        assertThat(dto.nota()).isEqualTo("Uma nota de teste");
        assertThat(dto.metadataJson()).isEqualTo("{\"foo\":\"bar\"}");
        assertThat(dto.caminhoScreenshot()).isEqualTo("arquivo.webp");
        assertThat(dto.screenshotDisponivel()).isTrue();
        assertThat(dto.usuarioCodigo()).isEqualTo("12345");
        assertThat(dto.usuarioNome()).isEqualTo("João Silva");
        assertThat(dto.enviadoEm()).isEqualTo(agora);
        assertThat(dto.rota()).isEqualTo("/minha-rota");
        assertThat(dto.status()).isEqualTo(FeedbackStatus.NOVO);
    }
}
