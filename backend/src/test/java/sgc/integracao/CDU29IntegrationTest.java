package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockChefe;
import sgc.integracao.mocks.WithMockGestor;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@DisplayName("CDU-29: Consultar histórico de processos")
class CDU29IntegrationTest extends BaseIntegrationTest {
    private Processo processoFinalizado1;

    @BeforeEach
    void setUp() {
        // Unidades do data.sql: 1 (admin), 101 (gestor), 102 (chefe). 1 -> 101 -> 102
        Unidade u101 = unidadeRepo.findById(101L).orElseThrow();
        Unidade u102 = unidadeRepo.findById(102L).orElseThrow();
        Unidade u10 = unidadeRepo.findById(10L).orElseThrow();

        LocalDateTime agora = LocalDateTime.now();

        // Processo 1: Finalizado, participa unidade 101 (Gestor)
        processoFinalizado1 = Processo.builder()
                .descricao("Processo Finalizado 1")
                .situacao(SituacaoProcesso.FINALIZADO)
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataCriacao(agora.minusDays(10))
                .dataLimite(agora.plusMonths(1))
                .dataFinalizacao(agora.minusDays(2))
                .build();
        processoFinalizado1.adicionarParticipantes(Set.of(u101));
        processoFinalizado1 = processoRepo.save(processoFinalizado1);
        processoRepo.flush();

        // Processo 2: Finalizado, participa unidade 102 (Chefe)
        Processo processoFinalizado2 = Processo.builder()
                .descricao("Processo Finalizado 2")
                .situacao(SituacaoProcesso.FINALIZADO)
                .tipo(TipoProcesso.REVISAO)
                .dataCriacao(agora.minusDays(10))
                .dataLimite(agora.plusMonths(1))
                .dataFinalizacao(agora.minusDays(1))
                .build();
        processoFinalizado2.adicionarParticipantes(Set.of(u102));
        processoRepo.save(processoFinalizado2);
        processoRepo.flush();

        // Processo 3: Finalizado, participa unidade 10 (Fora da hierarquia)
        Processo processoFinalizado3 = Processo.builder()
                .descricao("Processo Finalizado 3")
                .situacao(SituacaoProcesso.FINALIZADO)
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataCriacao(agora.minusDays(10))
                .dataLimite(agora.plusMonths(1))
                .dataFinalizacao(agora.minusDays(3))
                .build();
        processoFinalizado3.adicionarParticipantes(Set.of(u10));
        processoRepo.save(processoFinalizado3);
        processoRepo.flush();

        // Criar processo em andamento (não deve aparecer no histórico)
        Processo processoEmAndamento = Processo.builder()
                .descricao("Processo Em Andamento")
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataCriacao(agora.minusDays(10))
                .dataLimite(agora.plusMonths(1))
                .build();
        processoEmAndamento.adicionarParticipantes(Set.of(u101));
        processoRepo.save(processoEmAndamento);
        processoRepo.flush();
    }

    @Test
    @DisplayName("ADMIN deve listar todos os processos finalizados (global)")
    @WithMockAdmin
    void listarFinalizados_admin_sucesso() throws Exception {
        mockMvc.perform(get("/api/processos/finalizados").with(csrf()))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertThat(content).contains("Processo Finalizado 1");
                    assertThat(content).contains("Processo Finalizado 2");
                    assertThat(content).contains("Processo Finalizado 3");
                    assertThat(content).doesNotContain("Processo Em Andamento");
                });
    }

    @Test
    @DisplayName("GESTOR deve listar apenas processos de sua unidade e subunidades")
    @WithMockGestor("222222222222") // Unidade 101, subunidade 102
    void listarFinalizados_gestor_sucesso() throws Exception {
        mockMvc.perform(get("/api/processos/finalizados").with(csrf()))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertThat(content).contains("Processo Finalizado 1"); // Unidade 101
                    assertThat(content).contains("Processo Finalizado 2"); // Unidade 102 (descendente)
                    assertThat(content).doesNotContain("Processo Finalizado 3"); // Unidade 10 (fora)
                    assertThat(content).doesNotContain("Processo Em Andamento");
                });
    }

    @Test
    @DisplayName("CHEFE deve listar apenas processos de sua unidade específica")
    @WithMockChefe("111111111111") // Unidade 102
    void listarFinalizados_chefe_sucesso() throws Exception {
        mockMvc.perform(get("/api/processos/finalizados").with(csrf()))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertThat(content).doesNotContain("Processo Finalizado 1"); // Unidade 101 (superior)
                    assertThat(content).contains("Processo Finalizado 2"); // Unidade 102
                    assertThat(content).doesNotContain("Processo Finalizado 3"); // Unidade 10 (fora)
                });
    }

    @Test
    @DisplayName("Deve retornar detalhes de um processo finalizado")
    @WithMockAdmin
    void obterDetalhes_finalizado_sucesso() throws Exception {
        mockMvc.perform(get("/api/processos/{id}", processoFinalizado1.getCodigo()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(processoFinalizado1.getCodigo()))
                .andExpect(jsonPath("$.situacao").value("FINALIZADO"));
    }
}
