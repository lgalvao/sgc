package sgc.feedback;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.*;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.*;
import sgc.feedback.dto.*;
import sgc.integracao.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        UUID feedbackId = UUID.fromString(objectMapper.treeToValue(objectMapper.readTree(responseJson).get("codigo"), String.class));

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
