package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.*;
import org.springframework.test.context.*;
import sgc.feedback.*;
import sgc.integracao.mocks.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("CDU-39 — Enviar feedback contextual")
@ActiveProfiles({"test", "hom"})
@TestPropertySource(properties = {
        "sgc.feedback.screenshot-dir=./build/test-feedbacks",
        "sgc.feedback.max-screenshot-size-bytes=1048576"
})
class CDU39IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FeedbackRepo feedbackRepo;

    @BeforeEach
    void limpar() {
        feedbackRepo.deleteAll();
    }

    @Test
    @DisplayName("Deve enviar feedback com sucesso (sem screenshot)")
    @WithMockServidor
    void deveEnviarFeedbackComSucessoSemScreenshot() throws Exception {
        String jsonPayload = """
                {
                    "tipo": "SUGESTAO",
                    "nota": "Sugestão de melhoria na tela de listagem de processos.",
                    "metadados": {
                        "rotaCaminho": "/processos",
                        "userAgent": "Mozilla/5.0",
                        "larguraTela": 1920,
                        "alturaTela": 1080
                    }
                }
                """;

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                jsonPayload.getBytes()
        );

        mockMvc.perform(multipart("/api/feedback")
                        .file(data))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").exists())
                .andExpect(jsonPath("$.enviadoEm").exists());

        assertThat(feedbackRepo.count()).isEqualTo(1);
        FeedbackRegistro registro = feedbackRepo.findAll().get(0);
        assertThat(registro.getTipo()).isEqualTo(FeedbackTipo.SUGESTAO);
        assertThat(registro.getNota()).isEqualTo("Sugestão de melhoria na tela de listagem de processos.");
        assertThat(registro.getUsuarioCodigo()).isEqualTo("222222222222"); // Da MockUsuario default
        assertThat(registro.getRota()).isEqualTo("/processos");
    }

    @Test
    @DisplayName("Deve enviar feedback com sucesso (com screenshot)")
    @WithMockServidor
    void deveEnviarFeedbackComSucessoComScreenshot() throws Exception {
        String jsonPayload = """
                {
                    "tipo": "BUG",
                    "nota": "Ocorreu um erro ao tentar salvar o mapa de atividades.",
                    "metadados": {
                        "rotaCaminho": "/mapas/novo"
                    }
                }
                """;

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                jsonPayload.getBytes()
        );

        MockMultipartFile screenshot = new MockMultipartFile(
                "screenshot",
                "tela.webp",
                "image/webp",
                new byte[]{1, 2, 3, 4}
        );

        mockMvc.perform(multipart("/api/feedback")
                        .file(data)
                        .file(screenshot))
                .andExpect(status().isCreated());

        assertThat(feedbackRepo.count()).isEqualTo(1);
        FeedbackRegistro registro = feedbackRepo.findAll().get(0);
        assertThat(registro.getCaminhoScreenshot()).isNotNull();
    }

    @Test
    @DisplayName("Deve validar tamanho mínimo da descrição (nota)")
    @WithMockServidor
    void deveValidarTamanhoMinimoDescricao() throws Exception {
        String jsonPayload = """
                {
                    "tipo": "BUG",
                    "nota": "Curto",
                    "metadados": {}
                }
                """;

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                jsonPayload.getBytes()
        );

        mockMvc.perform(multipart("/api/feedback")
                        .file(data))
                .andExpect(status().is(HttpStatus.UNPROCESSABLE_CONTENT.value()))
                .andExpect(jsonPath("$.code").value("ERRO_VALIDACAO"));
    }

    @Test
    @DisplayName("Não deve permitir envio por usuário não autenticado")
    void naoDevePermitirEnvioNaoAutenticado() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                "{}".getBytes()
        );

        mockMvc.perform(multipart("/api/feedback")
                        .file(data))
                .andExpect(status().isUnauthorized());
    }
}
