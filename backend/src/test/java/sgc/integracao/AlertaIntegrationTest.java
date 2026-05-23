package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;
import sgc.integracao.mocks.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para os endpoints e fluxos de alertas do painel.
 */
@Tag("integration")
@Transactional
@DisplayName("Alertas — integração")
class AlertaIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AlertaRepo customAlertaRepo;

    private Unidade unidade102;
    private Unidade unidadeOrigem;
    private Processo processo;
    private Alerta alerta1;
    private Alerta alerta2;

    @BeforeEach
    void setUp() {
        // Carrega unidades e processo existentes no data.sql
        unidade102 = unidadeRepo.findById(102L).orElseThrow();
        unidadeOrigem = unidadeRepo.findById(1L).orElseThrow(); // Admin

        processo = Processo.builder()
                .descricao("Processo Teste Alertas")
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataCriacao(LocalDateTime.now())
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processo = processoRepo.save(processo);

        // Cria alertas destinados à unidade 102 (Unidade do chefe mockado)
        alerta1 = Alerta.builder()
                .processo(processo)
                .dataHora(LocalDateTime.now().minusHours(2))
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(unidade102)
                .descricao("Alerta transição A")
                .build();
        alerta1 = customAlertaRepo.save(alerta1);

        alerta2 = Alerta.builder()
                .processo(processo)
                .dataHora(LocalDateTime.now().minusHours(1))
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(unidade102)
                .descricao("Alerta transição B")
                .build();
        alerta2 = customAlertaRepo.save(alerta2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Deve listar todos os alertas do chefe autenticado")
    @WithMockChefe
    void listarAlertas_sucesso() throws Exception {
        mockMvc.perform(get("/api/alertas")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    assertThat(body).contains("Alerta transição A");
                    assertThat(body).contains("Alerta transição B");
                });
    }

    @Test
    @DisplayName("Deve listar alertas não lidos do chefe autenticado")
    @WithMockChefe
    void listarNaoLidos_sucesso() throws Exception {
        mockMvc.perform(get("/api/alertas/nao-lidos")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    assertThat(body).contains("Alerta transição A");
                    assertThat(body).contains("Alerta transição B");
                });
    }

    @Test
    @DisplayName("Deve listar alertas paginados no painel do chefe")
    @WithMockChefe
    void listarAlertasPainel_sucesso() throws Exception {
        mockMvc.perform(get("/api/painel/alertas")
                        .with(csrf())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].mensagem").value("Alerta transição B")); // Ordenado por dataHora DESC
    }

    @Test
    @DisplayName("Deve marcar alertas como lidos")
    @WithMockChefe
    void marcarAlertasLidos_sucesso() throws Exception {
        List<Long> codigos = List.of(alerta1.getCodigo(), alerta2.getCodigo());

        mockMvc.perform(post("/api/painel/alertas/marcar-lidos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(codigos)))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        // Após marcar como lido, a listagem de não lidos deve vir vazia
        mockMvc.perform(get("/api/alertas/nao-lidos")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
