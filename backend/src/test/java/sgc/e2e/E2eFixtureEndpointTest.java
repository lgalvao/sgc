package sgc.e2e;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import sgc.processo.model.SituacaoProcesso;
import tools.jackson.databind.ObjectMapper;

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
    @SuppressWarnings("unused")
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
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
                                .with(csrf())
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
                        "ASSESSORIA_12",
                        true, // iniciar = true
                        30);

        // Executar e Validar
        mockMvc.perform(
                        post("/e2e/fixtures/processo-mapeamento")
                                .with(csrf())
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
        mockMvc.perform(
                        post("/e2e/fixtures/processo-mapeamento")
                                .with(csrf())
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
        mockMvc.perform(
                        post("/e2e/fixtures/processo-mapeamento")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao", containsString("Processo Fixture E2E")))
                .andExpect(jsonPath("$.descricao", containsString("MAPEAMENTO")));
    }
}
