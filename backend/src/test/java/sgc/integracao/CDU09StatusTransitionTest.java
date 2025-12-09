package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import sgc.atividade.AtividadeService;
import sgc.atividade.dto.AtividadeDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoService;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test to isolate and verify the status transition bug:
 * When an activity is added to a subprocess in NAO_INICIADO state,
 * it should automatically transition to MAPEAMENTO_CADASTRO_EM_ANDAMENTO.
 * 
 * This reproduces the issue from E2E test cdu-09.spec.ts.
 */
@DisplayName("CDU-09 Status Transition Bug Test")
public class CDU09StatusTransitionTest extends BaseIntegrationTest {

    @Autowired
    private ProcessoService processoService;
    
    @Autowired
    private AtividadeService atividadeService;

    @Test
    @DisplayName("Adding activity should transition subprocess from NAO_INICIADO to MAPEAMENTO_CADASTRO_EM_ANDAMENTO")
    void shouldTransitionStatusWhenActivityIsAdded() throws Exception {
        // GIVEN: An OPERACIONAL unit with a CHEFE as titular
        // Using SESEL (unit 10) which has titular 333333333333 from data.sql
        Long unitCodigo = 10L;
        String titularTitulo = "333333333333";
        
        Unidade unidade = unidadeRepo.findById(unitCodigo)
            .orElseThrow(() -> new AssertionError("Unit not found: " + unitCodigo));
        
        assertThat(unidade.getTitular()).isNotNull();
        assertThat(unidade.getTitular().getTituloEleitoral()).isEqualTo(titularTitulo);
        
        // GIVEN: A MAPEAMENTO process is created and started for this unit
        Processo processo = new Processo()
            .setDescricao("Test Process for Status Transition")
            .setTipo(TipoProcesso.MAPEAMENTO)
            .setSituacao(SituacaoProcesso.CRIADO)
            .setDataCriacao(LocalDateTime.now())
            .setDataLimite(LocalDateTime.now().plusDays(30))
            .setParticipantes(Set.of(unidade));
        
        Processo savedProcesso = processoRepo.saveAndFlush(processo);
        
        // Start the process - this creates subprocesses
        processoService.iniciarProcessoMapeamento(savedProcesso.getCodigo(), List.of(unitCodigo));
        
        // VERIFY: Subprocess exists with NAO_INICIADO status
        Subprocesso subprocesso = subprocessoRepo.findByProcessoCodigoWithUnidade(savedProcesso.getCodigo())
            .stream()
            .filter(sp -> sp.getUnidade().getCodigo().equals(unitCodigo))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Subprocess not found for unit: " + unitCodigo));
        
        assertThat(subprocesso.getSituacao())
            .as("Initial subprocess status should be NAO_INICIADO")
            .isEqualTo(SituacaoSubprocesso.NAO_INICIADO);
        
        assertThat(subprocesso.getMapa())
            .as("Subprocess should have a mapa associated")
            .isNotNull();
        
        Long mapaCodigo = subprocesso.getMapa().getCodigo();
        System.out.println("=== TEST INFO ===");
        System.out.println("Subprocess ID: " + subprocesso.getCodigo());
        System.out.println("Mapa ID: " + mapaCodigo);
        System.out.println("Initial Status: " + subprocesso.getSituacao());
        System.out.println("Titular: " + titularTitulo);
        System.out.println("=================");
        
        // WHEN: The titular creates an activity
        AtividadeDto novaAtividade = new AtividadeDto();
        novaAtividade.setDescricao("Test Activity for Status Transition");
        novaAtividade.setMapaCodigo(mapaCodigo);
        
        AtividadeDto atividadeCriada = atividadeService.criar(novaAtividade, titularTitulo);
        
        assertThat(atividadeCriada).isNotNull();
        assertThat(atividadeCriada.getCodigo()).isNotNull();
        System.out.println("Created Activity ID: " + atividadeCriada.getCodigo());
        
        // THEN: Subprocess status should have changed to MAPEAMENTO_CADASTRO_EM_ANDAMENTO
        // Re-fetch the subprocess to get updated status
        Subprocesso updatedSubprocesso = subprocessoRepo.findById(subprocesso.getCodigo())
            .orElseThrow(() -> new AssertionError("Subprocess not found after activity creation"));
        
        System.out.println("Updated Status: " + updatedSubprocesso.getSituacao());
        
        assertThat(updatedSubprocesso.getSituacao())
            .as("Status should transition to MAPEAMENTO_CADASTRO_EM_ANDAMENTO after activity is added")
            .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
    }
    
    @Test
    @DisplayName("Adding activity via API endpoint should also transition status")
    void shouldTransitionStatusWhenActivityIsAddedViaApi() throws Exception {
        // GIVEN: Same setup as above
        Long unitCodigo = 10L;

        
        Unidade unidade = unidadeRepo.findById(unitCodigo)
            .orElseThrow(() -> new AssertionError("Unit not found: " + unitCodigo));
        
        // Create and start process
        Processo processo = new Processo()
            .setDescricao("Test API Process for Status Transition")
            .setTipo(TipoProcesso.MAPEAMENTO)
            .setSituacao(SituacaoProcesso.CRIADO)
            .setDataCriacao(LocalDateTime.now())
            .setDataLimite(LocalDateTime.now().plusDays(30))
            .setParticipantes(Set.of(unidade));
        
        Processo savedProcesso = processoRepo.saveAndFlush(processo);
        processoService.iniciarProcessoMapeamento(savedProcesso.getCodigo(), List.of(unitCodigo));
        
        Subprocesso subprocesso = subprocessoRepo.findByProcessoCodigoWithUnidade(savedProcesso.getCodigo())
            .stream()
            .filter(sp -> sp.getUnidade().getCodigo().equals(unitCodigo))
            .findFirst()
            .orElseThrow();
        
        Long mapaCodigo = subprocesso.getMapa().getCodigo();
        
        // WHEN: Activity is created via API
        String requestBody = """
            {
                "descricao": "API Test Activity",
                "mapaCodigo": %d
            }
            """.formatted(mapaCodigo);
        
        Usuario titular = unidade.getTitular();
        
        mockMvc.perform(post("/api/atividades")
                .with(user(titular))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated());
        
        // THEN: Status should have transitioned
        Subprocesso updatedSubprocesso = subprocessoRepo.findById(subprocesso.getCodigo())
            .orElseThrow();
        
        assertThat(updatedSubprocesso.getSituacao())
            .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
    }
}
