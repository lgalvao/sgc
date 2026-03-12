package sgc.e2e;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.*;
import org.springframework.web.context.*;
import sgc.processo.model.*;
import tools.jackson.databind.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
                        "Processo Fixture Teste Mapeamento", "ASSESSORIA_11", false, 30);

        // Executar e Validar
        mockMvc.perform(
                        post("/e2e/fixtures/processo-mapeamento")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo Fixture Teste Mapeamento"))
                .andExpect(jsonPath("$.tipo").value("MAPEAMENTO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.CRIADO.name()));
    }

    @Test
    @DisplayName("Deve permitir criar e iniciar processo de mapeamento via fixture")
    void devePermitirCriarEIniciarProcessoMapeamentoViaFixture() throws Exception {
        // Preparar requisição
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        "Processo Fixture Teste Mapeamento Iniciado",
                        "ADMIN", // Unidade Raiz
                        true, // iniciar = true
                        30);

        // Executar e Validar
        mockMvc.perform(post("/e2e/fixtures/processo-mapeamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.descricao").value("Processo Fixture Teste Mapeamento Iniciado"))
                .andExpect(jsonPath("$.tipo").value("MAPEAMENTO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.EM_ANDAMENTO.name()));
    }

    @Test
    @DisplayName("Deve retornar erro quando unidade não existe")
    void deveRetornarErroQuandoUnidadeNaoExiste() throws Exception {
        // Preparar requisição com unidade inexistente
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        "Processo Fixture Teste", "UNIDADE_INEXISTENTE", false, 30);

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
                .andExpect(jsonPath("$.descricao", containsString("Processo Fixture E2E")))
                .andExpect(jsonPath("$.descricao", containsString("MAPEAMENTO")));
    }

    @Test
    @DisplayName("Deve permitir criar processo de mapeamento com cadastro homologado via fixture")
    void devePermitirCriarProcessoMapeamentoComCadastroHomologadoViaFixture() throws Exception {
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        "Processo Fixture Cadastro Homologado", "ASSESSORIA_12", true, 30);

        mockMvc.perform(post("/e2e/fixtures/processo-mapeamento-com-cadastro-homologado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo Fixture Cadastro Homologado"))
                .andExpect(jsonPath("$.tipo").value("MAPEAMENTO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.EM_ANDAMENTO.name()));
    }

    @Test
    @DisplayName("Deve permitir criar processo de mapeamento com mapa disponibilizado via fixture")
    void devePermitirCriarProcessoMapeamentoComMapaDisponibilizadoViaFixture() throws Exception {
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        "Processo Fixture Mapa Disponibilizado", "ASSESSORIA_12", true, 30);

        mockMvc.perform(post("/e2e/fixtures/processo-mapeamento-com-mapa-disponibilizado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo Fixture Mapa Disponibilizado"))
                .andExpect(jsonPath("$.tipo").value("MAPEAMENTO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.EM_ANDAMENTO.name()));
    }

    @Test
    @DisplayName("Deve permitir criar processo de mapeamento com mapa validado via fixture")
    void devePermitirCriarProcessoMapeamentoComMapaValidadoViaFixture() throws Exception {
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        "Processo Fixture Mapa Validado", "ASSESSORIA_12", true, 30);

        mockMvc.perform(post("/e2e/fixtures/processo-mapeamento-com-mapa-validado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo Fixture Mapa Validado"))
                .andExpect(jsonPath("$.tipo").value("MAPEAMENTO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.EM_ANDAMENTO.name()));
    }

    @Test
    @DisplayName("Deve permitir criar processo de mapeamento com mapa homologado via fixture")
    void devePermitirCriarProcessoMapeamentoComMapaHomologadoViaFixture() throws Exception {
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        "Processo Fixture Mapa Homologado", "ASSESSORIA_12", true, 30);

        mockMvc.perform(post("/e2e/fixtures/processo-mapeamento-com-mapa-homologado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo Fixture Mapa Homologado"))
                .andExpect(jsonPath("$.tipo").value("MAPEAMENTO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.EM_ANDAMENTO.name()));
    }

    @Test
    @DisplayName("Deve permitir criar processo de revisão com cadastro homologado via fixture")
    void devePermitirCriarProcessoRevisaoComCadastroHomologadoViaFixture() throws Exception {
        E2eController.ProcessoFixtureRequest request =
                new E2eController.ProcessoFixtureRequest(
                        "Processo Fixture Revisão Cadastro Homologado", "ASSESSORIA_12", true, 30);

        mockMvc.perform(post("/e2e/fixtures/processo-revisao-com-cadastro-homologado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo Fixture Revisão Cadastro Homologado"))
                .andExpect(jsonPath("$.tipo").value("REVISAO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.EM_ANDAMENTO.name()));
    }

    @Test
    @DisplayName("Deve permitir criar processo de revisão com mapa homologado via fixture")
    void devePermitirCriarProcessoRevisaoComMapaHomologadoViaFixture() throws Exception {
        E2eController.ProcessoFixtureRequest processoMapeamento =
                new E2eController.ProcessoFixtureRequest(
                        "Processo Base Mapa Vigente", "ASSESSORIA_12", true, 30);
        E2eController.ProcessoFixtureRequest processoRevisao =
                new E2eController.ProcessoFixtureRequest(
                        "Processo Fixture Revisão Homologada", "ASSESSORIA_12", true, 30);

        mockMvc.perform(post("/e2e/fixtures/processo-finalizado-com-atividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processoMapeamento)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/e2e/fixtures/processo-revisao-com-mapa-homologado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processoRevisao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Processo Fixture Revisão Homologada"))
                .andExpect(jsonPath("$.tipo").value("REVISAO"))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.EM_ANDAMENTO.name()));
    }
}
