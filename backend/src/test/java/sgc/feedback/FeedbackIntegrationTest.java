package sgc.feedback;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import sgc.feedback.dto.FeedbackListagemDto;
import sgc.integracao.BaseIntegrationTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("FeedbackIntegrationTest")
class FeedbackIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FeedbackService feedbackService;

    private static Path tempDir;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws IOException {
        tempDir = Files.createTempDirectory("sgc-feedback-it-");
        registry.add("sgc.feedback.screenshot-dir", tempDir::toString);
    }

    @Test
    @DisplayName("deve realizar o ciclo completo: registrar feedback com captura e depois ler a imagem")
    @WithMockUser(username = "123456789012")
    void deveRegistrarELerCaptura() throws Exception {
        // 1. Registrar um feedback com screenshot via API
        String payloadJson = """
                {"tipo":"bug","nota":"Teste de integração de captura de tela","metadados":{"rotaCaminho":"/teste-it"}}
                """;
        MockMultipartFile dataPart = new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE, payloadJson.getBytes());
        MockMultipartFile screenshotPart = new MockMultipartFile("screenshot", "screenshot.webp", "image/webp", "bytes-da-imagem-it".getBytes());

        String responseJson = mockMvc.perform(multipart("/api/feedback")
                        .file(dataPart)
                        .file(screenshotPart)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID feedbackId = UUID.fromString(objectMapper.readTree(responseJson).get("id").textValue());

        // 2. Verificar se o registro aparece na listagem (como ADMIN)
        List<FeedbackListagemDto> listagem = feedbackService.listarRecentes(10);
        FeedbackListagemDto registrado = listagem.stream()
                .filter(f -> f.codigo().equals(feedbackId))
                .findFirst()
                .orElseThrow();

        assertThat(registrado.caminhoScreenshot()).isNotNull();

        // 3. Tentar baixar a screenshot via API (como ADMIN)
        byte[] imagemBaixada = mockMvc.perform(get("/api/feedback/" + feedbackId + "/screenshot")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentType()).isEqualTo("image/webp"))
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(imagemBaixada).isEqualTo("bytes-da-imagem-it".getBytes());
    }
}
