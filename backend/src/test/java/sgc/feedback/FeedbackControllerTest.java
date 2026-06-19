package sgc.feedback;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.RestExceptionHandler;
import sgc.feedback.dto.FeedbackListagemDto;
import sgc.feedback.dto.FeedbackRespostaDto;
import sgc.seguranca.SgcPermissionEvaluator;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeedbackController.class)
@ActiveProfiles("hom")
@Import(RestExceptionHandler.class)
@DisplayName("FeedbackController")
class FeedbackControllerTest {

    private static final String URL = "/api/feedback";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private FeedbackService feedbackService;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    @Test
    @DisplayName("POST /api/feedback - deve registrar feedback e retornar 201")
    @WithMockUser
    void deveRegistrarFeedbackComSucesso() throws Exception {
        UUID codigo = UUID.randomUUID();
        OffsetDateTime agora = OffsetDateTime.now();
        when(feedbackService.registrar(any(), any())).thenReturn(new FeedbackRespostaDto(codigo, agora));

        String payloadJson = """
                {"tipo":"bug","nota":"Encontrei um problema ao realizar o cadastro de atividades","metadados":{"rotaCaminho":"/processos/1"}}
                """;
        MockMultipartFile dataPart = new MockMultipartFile("data", "", org.springframework.http.MediaType.APPLICATION_JSON_VALUE, payloadJson.getBytes());

        mockMvc.perform(multipart(URL)
                        .file(dataPart)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(codigo.toString()));
    }

    @Test
    @DisplayName("POST /api/feedback - deve retornar 422 quando nota é muito curta")
    @WithMockUser
    void deveRetornar422PorNotaMuitoCurta() throws Exception {
        String payloadJson = """
                {"tipo":"bug","nota":"curto","metadados":null}
                """;
        MockMultipartFile dataPart = new MockMultipartFile("data", "", org.springframework.http.MediaType.APPLICATION_JSON_VALUE, payloadJson.getBytes());

        mockMvc.perform(multipart(URL)
                        .file(dataPart)
                        .with(csrf()))
                .andExpect(status().is(org.hamcrest.Matchers.is(422)));
    }

    @Test
    @DisplayName("POST /api/feedback - deve retornar 422 quando tipo é inválido")
    @WithMockUser
    void deveRetornar422PorTipoInvalido() throws Exception {
        String payloadJson = """
                {"tipo":"tipo_inexistente","nota":"Nota suficientemente longa para passar na validação","metadados":null}
                """;
        MockMultipartFile dataPart = new MockMultipartFile("data", "", org.springframework.http.MediaType.APPLICATION_JSON_VALUE, payloadJson.getBytes());

        mockMvc.perform(multipart(URL)
                        .file(dataPart)
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/feedback - deve aceitar screenshot junto ao payload")
    @WithMockUser
    void deveRegistrarFeedbackComScreenshot() throws Exception {
        UUID codigo = UUID.randomUUID();
        when(feedbackService.registrar(any(), any())).thenReturn(new FeedbackRespostaDto(codigo, OffsetDateTime.now()));

        String payloadJson = """
                {"tipo":"sugestao","nota":"Sugestão de melhoria para a interface do usuário","metadados":null}
                """;
        MockMultipartFile dataPart = new MockMultipartFile("data", "", org.springframework.http.MediaType.APPLICATION_JSON_VALUE, payloadJson.getBytes());
        MockMultipartFile screenshotPart = new MockMultipartFile("screenshot", "screenshot.webp", "image/webp", "fake-image".getBytes());

        mockMvc.perform(multipart(URL)
                        .file(dataPart)
                        .file(screenshotPart)
                        .with(csrf()))
                .andExpect(status().isCreated());

        verify(feedbackService).registrar(argThat(p -> p.tipo() == FeedbackTipo.SUGESTAO), any());
    }

    @Test
    @DisplayName("POST /api/feedback - deve propagar ErroValidacao do service")
    @WithMockUser
    void devePropagrarErroValidacaoDoService() throws Exception {
        when(feedbackService.registrar(any(), any())).thenThrow(new sgc.comum.erros.ErroValidacao("screenshot excede o tamanho máximo"));

        String payloadJson = """
                {"tipo":"bug","nota":"Nota longa o suficiente para passar validação local","metadados":null}
                """;
        MockMultipartFile dataPart = new MockMultipartFile("data", "", org.springframework.http.MediaType.APPLICATION_JSON_VALUE, payloadJson.getBytes());

        mockMvc.perform(multipart(URL)
                        .file(dataPart)
                        .with(csrf()))
                .andExpect(status().is(org.hamcrest.Matchers.is(422)));
    }

    @Test
    @DisplayName("GET /api/feedback/listar - deve retornar lista para admin")
    @WithMockUser(roles = "ADMIN")
    void deveListarFeedbacksParaAdmin() throws Exception {
        UUID codigo = UUID.randomUUID();
        when(feedbackService.listarRecentes(100)).thenReturn(List.of(
                new FeedbackListagemDto(
                        codigo,
                        FeedbackTipo.BUG,
                        "Bug relatado",
                        null,
                        null,
                        false,
                        "123",
                        "João",
                        OffsetDateTime.now(),
                        "/painel",
                        FeedbackStatus.NOVO
                )
        ));

        mockMvc.perform(get(URL + "/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(codigo.toString()))
                .andExpect(jsonPath("$[0].tipo").value("bug"));
    }

    @Test
    @DisplayName("GET /api/feedback/{codigo}/screenshot - deve retornar bytes da imagem para admin")
    @WithMockUser(roles = "ADMIN")
    void deveRetornarScreenshotParaAdmin() throws Exception {
        UUID codigo = UUID.randomUUID();
        byte[] imagem = "fake-image-bytes".getBytes();
        when(feedbackService.obterScreenshot(codigo)).thenReturn(imagem);

        mockMvc.perform(get(URL + "/" + codigo + "/screenshot"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"))
                .andExpect(content().bytes(imagem));
    }
}
