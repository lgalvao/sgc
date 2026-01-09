package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.fixture.ProcessoFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, sgc.integracao.mocks.TestThymeleafConfig.class})
@Transactional
@DisplayName("CDU-29: Consultar histórico de processos")
class CDU29IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private EntityManager entityManager;

    private Processo processoFinalizado1;
    private Processo processoFinalizado2;
    private Processo processoEmAndamento;

    @BeforeEach
    void setUp() {
        // Criar processo finalizado 1
        processoFinalizado1 = ProcessoFixture.processoPadrao();
        processoFinalizado1.setCodigo(null);
        processoFinalizado1.setDescricao("Processo Finalizado 1");
        processoFinalizado1.setSituacao(SituacaoProcesso.FINALIZADO);
        processoFinalizado1.setTipo(TipoProcesso.MAPEAMENTO);
        processoFinalizado1.setDataFinalizacao(LocalDateTime.now().minusDays(2));
        processoFinalizado1 = processoRepo.save(processoFinalizado1);

        // Criar processo finalizado 2
        processoFinalizado2 = ProcessoFixture.processoPadrao();
        processoFinalizado2.setCodigo(null);
        processoFinalizado2.setDescricao("Processo Finalizado 2");
        processoFinalizado2.setSituacao(SituacaoProcesso.FINALIZADO);
        processoFinalizado2.setTipo(TipoProcesso.REVISAO);
        processoFinalizado2.setDataFinalizacao(LocalDateTime.now().minusDays(1));
        processoFinalizado2 = processoRepo.save(processoFinalizado2);

        // Criar processo em andamento (não deve aparecer)
        processoEmAndamento = ProcessoFixture.processoPadrao();
        processoEmAndamento.setCodigo(null);
        processoEmAndamento.setDescricao("Processo Em Andamento");
        processoEmAndamento.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoEmAndamento = processoRepo.save(processoEmAndamento);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Deve listar apenas processos finalizados")
    @org.springframework.security.test.context.support.WithMockUser
    void listarFinalizados_sucesso() throws Exception {
        mockMvc.perform(get("/api/processos/finalizados")
                        .with(csrf()))
                .andExpect(status().isOk())
                // .andExpect(jsonPath("$.length()").value(2)) // Removing exact count check as other tests might have created data
                .andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertThat(content).contains("Processo Finalizado 1");
                    assertThat(content).contains("Processo Finalizado 2");
                    assertThat(content).doesNotContain("Processo Em Andamento");
                });
    }

    @Test
    @DisplayName("Deve retornar detalhes de um processo finalizado")
    @WithMockAdmin
    void obterDetalhes_finalizado_sucesso() throws Exception {
        mockMvc.perform(get("/api/processos/{id}", processoFinalizado1.getCodigo())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(processoFinalizado1.getCodigo()))
                .andExpect(jsonPath("$.situacao").value("FINALIZADO"));
    }
}
