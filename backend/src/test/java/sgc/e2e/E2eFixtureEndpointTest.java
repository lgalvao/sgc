package sgc.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import sgc.feedback.FeedbackRegistro;
import sgc.feedback.FeedbackRepo;
import sgc.feedback.FeedbackTipo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.model.*;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("e2e")
@DisplayName("Testes de Endpoint de Fixture E2E")
class E2eFixtureEndpointTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private FeedbackRepo feedbackRepo;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        // Resetar banco para garantir massa de dados (seed.sql)
        mockMvc.perform(post("/e2e/reset-database")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve permitir criar processo de mapeamento via fixture")
    void devePermitirCriarProcessoMapeamentoViaFixture() throws Exception {
        // Preparar requisição
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        "Processo fixture teste mapeamento", "ASSESSORIA_11", false, 30);

        // Executar e Validar
        mockMvc.perform(
                        post("/e2e/fixtures/processo-mapeamento")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo fixture teste mapeamento"))
                .andExpect(jsonPath("$.tipo").value("MAPEAMENTO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.CRIADO.name()));
    }

    @Test
    @DisplayName("Deve permitir criar e iniciar processo de mapeamento via fixture")
    void devePermitirCriarEIniciarProcessoMapeamentoViaFixture() throws Exception {
        // Preparar requisição
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        "Processo fixture teste mapeamento iniciado",
                        "ASSESSORIA_11",
                        true, // iniciar = true
                        30);

        // Executar e Validar
        mockMvc.perform(post("/e2e/fixtures/processo-mapeamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.descricao").value("Processo fixture teste mapeamento iniciado"))
                .andExpect(jsonPath("$.tipo").value("MAPEAMENTO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.EM_ANDAMENTO.name()));
    }

    @Test
    @DisplayName("Deve retornar erro quando unidade não existe")
    void deveRetornarErroQuandoUnidadeNaoExiste() throws Exception {
        // Preparar requisição com unidade inexistente
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        "Processo fixture teste", "UNIDADE_INEXISTENTE", false, 30);

        // Executar e Validar
        mockMvc.perform(post("/e2e/fixtures/processo-mapeamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Deve gerar descrição automática quando não fornecida")
    void deveGerarDescricaoAutomaticaQuandoNaoFornecida() throws Exception {
        // Preparar requisição sem descrição
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        null, // sem descrição
                        "ASSESSORIA_21",
                        false,
                        30);

        // Executar e Validar
        mockMvc.perform(post("/e2e/fixtures/processo-mapeamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao", containsString("Processo fixture E2E")))
                .andExpect(jsonPath("$.descricao", containsString("MAPEAMENTO")));
    }

    @Test
    @DisplayName("Deve permitir criar processo de mapeamento com cadastro homologado via fixture")
    void devePermitirCriarProcessoMapeamentoComCadastroHomologadoViaFixture() throws Exception {
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        "Processo fixture cadastro homologado", "ASSESSORIA_12", true, 30);

        mockMvc.perform(post("/e2e/fixtures/processo-mapeamento-com-cadastro-homologado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo fixture cadastro homologado"))
                .andExpect(jsonPath("$.tipo").value("MAPEAMENTO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.EM_ANDAMENTO.name()));
    }

    @Test
    @DisplayName("Deve permitir criar processo de mapeamento com mapa disponibilizado via fixture")
    void devePermitirCriarProcessoMapeamentoComMapaDisponibilizadoViaFixture() throws Exception {
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        "Processo fixture mapa disponibilizado", "ASSESSORIA_12", true, 30);

        mockMvc.perform(post("/e2e/fixtures/processo-mapeamento-com-mapa-disponibilizado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo fixture mapa disponibilizado"))
                .andExpect(jsonPath("$.tipo").value("MAPEAMENTO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.EM_ANDAMENTO.name()));
    }

    @Test
    @DisplayName("Deve permitir criar processo de mapeamento com mapa validado via fixture")
    void devePermitirCriarProcessoMapeamentoComMapaValidadoViaFixture() throws Exception {
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        "Processo fixture mapa validado", "ASSESSORIA_12", true, 30);

        mockMvc.perform(post("/e2e/fixtures/processo-mapeamento-com-mapa-validado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo fixture mapa validado"))
                .andExpect(jsonPath("$.tipo").value("MAPEAMENTO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.EM_ANDAMENTO.name()));
    }

    @Test
    @DisplayName("Deve permitir criar processo de mapeamento com mapa homologado via fixture")
    void devePermitirCriarProcessoMapeamentoComMapaHomologadoViaFixture() throws Exception {
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        "Processo fixture mapa homologado", "ASSESSORIA_12", true, 30);

        mockMvc.perform(post("/e2e/fixtures/processo-mapeamento-com-mapa-homologado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo fixture mapa homologado"))
                .andExpect(jsonPath("$.tipo").value("MAPEAMENTO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.EM_ANDAMENTO.name()));
    }

    @Test
    @DisplayName("Deve permitir criar processo de revisão com cadastro homologado via fixture")
    void devePermitirCriarProcessoRevisaoComCadastroHomologadoViaFixture() throws Exception {
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        "Processo fixture revisão cadastro homologado", "ASSESSORIA_12", true, 30);

        mockMvc.perform(post("/e2e/fixtures/processo-revisao-com-cadastro-homologado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo fixture revisão cadastro homologado"))
                .andExpect(jsonPath("$.tipo").value("REVISAO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.EM_ANDAMENTO.name()));
    }

    @Test
    @DisplayName("Deve permitir criar processo de revisão com mapa homologado via fixture")
    void devePermitirCriarProcessoRevisaoComMapaHomologadoViaFixture() throws Exception {
        E2eController.ProcessoFixtureRequest processoMapeamento =
                new E2eController.ProcessoFixtureRequest(
                        "Processo base mapa vigente", "ASSESSORIA_12", true, 30);
        E2eController.ProcessoFixtureRequest processoRevisao =
                new E2eController.ProcessoFixtureRequest(
                        "Processo fixture revisão homologada", "ASSESSORIA_12", true, 30);

        mockMvc.perform(post("/e2e/fixtures/processo-finalizado-com-atividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processoMapeamento)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/e2e/fixtures/processo-revisao-com-mapa-homologado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processoRevisao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo fixture revisão homologada"))
                .andExpect(jsonPath("$.tipo").value("REVISAO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.EM_ANDAMENTO.name()));

        Subprocesso subprocesso = subprocessoRepo.buscarPorProcessoEUnidadeComFetch(401L, 4L).orElseThrow();
        assertThat(subprocesso.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO);
        assertThat(movimentacaoRepo.buscarUltimaPorSubprocesso(subprocesso.getCodigo()))
                .isPresent()
                .get()
                .extracting(Movimentacao::getDescricao)
                .isEqualTo("Mapa homologado via fixture");
    }

    @Test
    @DisplayName("Deve permitir criar processo de diagnóstico via fixture")
    void devePermitirCriarProcessoDiagnosticoViaFixture() throws Exception {
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        "Processo fixture diagnóstico", "ASSESSORIA_12", true, 30);

        mockMvc.perform(post("/e2e/fixtures/processo-diagnostico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo fixture diagnóstico"))
                .andExpect(jsonPath("$.tipo").value("DIAGNOSTICO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.EM_ANDAMENTO.name()));
    }

    @Test
    @DisplayName("Deve permitir criar notificação fixture com falha definitiva")
    void devePermitirCriarNotificacaoFixture() throws Exception {
        E2eController.NotificacaoFixtureRequest request =
                new E2eController.NotificacaoFixtureRequest(
                        "falha@tre-pe.jus.br",
                        "SGC: Notificação fixture",
                        "<p>Preview</p>",
                        "DIAGNOSTICO_CONSENSO_APROVADO",
                        "FALHA_DEFINITIVA",
                        "ASSESSORIA_12",
                        null,
                        "SMTP indisponível");

        mockMvc.perform(post("/e2e/fixtures/notificacao-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destinatario").value("falha@tre-pe.jus.br"))
                .andExpect(jsonPath("$.tipoNotificacao").value("DIAGNOSTICO_CONSENSO_APROVADO"))
                .andExpect(jsonPath("$.situacao").value("FALHA_DEFINITIVA"));
    }

    @Test
    @DisplayName("Deve permitir criar feedback fixture com screenshot")
    void devePermitirCriarFeedbackFixture() throws Exception {
        E2eController.FeedbackFixtureRequest request =
                new E2eController.FeedbackFixtureRequest(
                        "SUGESTAO",
                        "<p>Feedback fixture</p>",
                        "/painel",
                        "{\"rotaCaminho\":\"/painel\"}",
                        true,
                        "191919",
                        "Administrador 1");

        String conteudo = mockMvc.perform(post("/e2e/fixtures/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rota").value("/painel"))
                .andExpect(jsonPath("$.screenshotDisponivel").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String codigo = objectMapper.readTree(conteudo).get("codigo").asString();
        assertThat(feedbackRepo.findById(java.util.UUID.fromString(codigo)))
                .isPresent()
                .get()
                .extracting(FeedbackRegistro::getTipo)
                .isEqualTo(FeedbackTipo.SUGESTAO);
    }
}
