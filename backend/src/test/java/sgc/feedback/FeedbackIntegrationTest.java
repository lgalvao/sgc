package sgc.feedback;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import sgc.feedback.dto.FeedbackListagemDto;
import sgc.integracao.BaseIntegrationTest;
import sgc.integracao.mocks.WithMockAdmin;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@DisplayName("Feedback — integração")
class FeedbackIntegrationTest extends BaseIntegrationTest {

    private static final Path DIRETORIO_SCREENSHOTS =
            Path.of("backend", "build", "feedback-it-" + UUID.randomUUID());

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private FeedbackRepo feedbackRepo;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws IOException {
        Files.createDirectories(DIRETORIO_SCREENSHOTS);
        registry.add("sgc.feedback.screenshot-dir", () -> DIRETORIO_SCREENSHOTS.toString());
    }

    @BeforeEach
    void limparEstadoDosFeedbacks() throws Exception {
        feedbackRepo.deleteAll();
        if (Files.exists(DIRETORIO_SCREENSHOTS)) {
            try (Stream<Path> caminhos = Files.walk(DIRETORIO_SCREENSHOTS)) {
                caminhos.sorted(Comparator.reverseOrder())
                        .filter(path -> !path.equals(DIRETORIO_SCREENSHOTS))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
            }
        }
        Files.createDirectories(DIRETORIO_SCREENSHOTS);
    }

    @Test
    @DisplayName("deve realizar o ciclo completo de registrar e ler screenshot")
    @WithMockUser(username = "123456789012")
    void deveRegistrarELerCaptura() throws Exception {
        String payloadJson = """
                {"tipo":"bug","nota":"Teste de integração de captura de tela","metadados":{"rotaCaminho":"/teste-it"}}
                """;
        MockMultipartFile screenshotPart = new MockMultipartFile(
                "screenshot",
                "screenshot.webp",
                "image/webp",
                "bytes-da-imagem-it".getBytes(StandardCharsets.UTF_8)
        );

        UUID codigoFeedback = registrarFeedback(payloadJson, screenshotPart);

        List<FeedbackListagemDto> listagem = feedbackService.listarRecentes(10);
        FeedbackListagemDto registrado = listagem.stream()
                .filter(feedback -> feedback.codigo().equals(codigoFeedback))
                .findFirst()
                .orElseThrow();

        assertThat(registrado.caminhoScreenshot()).isNotBlank();
        assertThat(registrado.screenshotDisponivel()).isTrue();

        byte[] imagemBaixada = mockMvc.perform(get("/api/feedback/{codigo}/screenshot", codigoFeedback)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentType()).isEqualTo("image/webp"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertThat(imagemBaixada).isEqualTo("bytes-da-imagem-it".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("deve retornar 422 quando a nota excede 2000 caracteres")
    @WithMockUser(username = "123456789012")
    void deveRetornar422QuandoNotaExcede2000Caracteres() throws Exception {
        String payloadJson = """
                {"tipo":"bug","nota":"%s","metadados":{"rotaCaminho":"/teste-it"}}
                """.formatted("a".repeat(2001));

        mockMvc.perform(multipart("/api/feedback")
                        .file(criarParteData(payloadJson))
                        .with(csrf()))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("deve retornar 422 quando a nota não é informada")
    @WithMockUser(username = "123456789012")
    void deveRetornar422QuandoNotaNaoInformada() throws Exception {
        String payloadJson = """
                {"tipo":"bug","metadados":{"rotaCaminho":"/teste-it"}}
                """;

        mockMvc.perform(multipart("/api/feedback")
                        .file(criarParteData(payloadJson))
                        .with(csrf()))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("deve retornar 404 ao obter screenshot de feedback inexistente")
    @WithMockAdmin
    void deveRetornar404QuandoFeedbackNaoExisteAoObterScreenshot() throws Exception {
        mockMvc.perform(get("/api/feedback/{codigo}/screenshot", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deve retornar 404 quando o arquivo de screenshot não existe no disco")
    @WithMockAdmin
    void deveRetornar404QuandoArquivoScreenshotNaoExisteNoDisco() throws Exception {
        FeedbackRegistro feedback = feedbackRepo.save(FeedbackRegistro.builder()
                .tipo(FeedbackTipo.BUG)
                .nota("Feedback sem arquivo físico no disco")
                .metadataJson("{}")
                .caminhoScreenshot("arquivo-ausente.webp")
                .usuarioCodigo("111111111111")
                .usuarioNome("Admin Teste")
                .enviadoEm(OffsetDateTime.now())
                .rota("/feedback-ausente")
                .status(FeedbackStatus.NOVO)
                .build());

        mockMvc.perform(get("/api/feedback/{codigo}/screenshot", feedback.getCodigo()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deve listar recentes vazio quando não há feedbacks")
    void deveListarRecentesVazioQuandoNaoHaFeedbacks() {
        assertThat(feedbackService.listarRecentes(10)).isEmpty();
    }

    @Test
    @DisplayName("deve registrar feedback sem screenshot")
    @WithMockUser(username = "123456789012")
    void deveRegistrarFeedbackSemScreenshot() throws Exception {
        String payloadJson = """
                {"tipo":"sugestao","nota":"Feedback sem screenshot para validar campo opcional","metadados":{"rotaCaminho":"/sem-screenshot"}}
                """;

        UUID codigoFeedback = registrarFeedback(payloadJson, null);
        FeedbackListagemDto registrado = feedbackService.listarRecentes(10).stream()
                .filter(feedback -> feedback.codigo().equals(codigoFeedback))
                .findFirst()
                .orElseThrow();

        assertThat(registrado.caminhoScreenshot()).isNull();
        assertThat(registrado.screenshotDisponivel()).isFalse();
    }

    private UUID registrarFeedback(String payloadJson, MockMultipartFile screenshot) throws Exception {
        var requisicao = multipart("/api/feedback")
                .file(criarParteData(payloadJson))
                .with(csrf());

        if (screenshot != null) {
            requisicao.file(screenshot);
        }

        String resposta = mockMvc.perform(requisicao)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(resposta).get("codigo").stringValue());
    }

    private MockMultipartFile criarParteData(String payloadJson) {
        return new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                payloadJson.getBytes(StandardCharsets.UTF_8)
        );
    }
}
