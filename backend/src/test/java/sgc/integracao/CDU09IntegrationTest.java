package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.AlertaRepo;
import sgc.analise.model.Analise;
import sgc.analise.model.AnaliseRepo;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.integracao.mocks.WithMockChefe;
import sgc.mapa.model.*;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@DisplayName("CDU-09: Fluxo Completo de Disponibilização")
class CDU09IntegrationTest extends BaseIntegrationTest {

    @Autowired private CompetenciaRepo competenciaRepo;
    @Autowired private MovimentacaoRepo movimentacaoRepo;
    @Autowired private ConhecimentoRepo conhecimentoRepo;
    @Autowired private AlertaRepo alertaRepo;
    @Autowired private AnaliseRepo analiseRepo;
    @Autowired private EntityManager entityManager;

    private final Long SP_CODIGO = 60000L; // SEDESENV (Unidade 8) no data.sql

    @Test
    @WithMockChefe("3") // Fernanda Oliveira - Chefe da Unidade 8 no data.sql
    @DisplayName("Fluxo Principal - Visualizar, Preparar e Disponibilizar Cadastro")
    void fluxoCompletoDisponibilizacao() throws Exception {
        Subprocesso sp = subprocessoRepo.findById(SP_CODIGO).orElseThrow();
        
        // --- ETAPA 1: Visualizar Detalhes e Histórico de Análise (Passos 1 a 5) ---
        
        // Simula uma análise anterior (que deve ser exibida no histórico)
        analiseRepo.saveAndFlush(Analise.builder()
                .subprocesso(sp)
                .unidadeCodigo(6L) // COSIS (Superior da 8)
                .usuarioTitulo("666666666666")
                .tipo(TipoAnalise.CADASTRO)
                .acao(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                .dataHora(LocalDateTime.now().minusDays(2))
                .observacoes("Favor ajustar atividades")
                .build());

        mockMvc.perform(get("/api/subprocessos/{id}", SP_CODIGO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subprocesso.codUnidade", is(8)))
                .andExpect(jsonPath("$.subprocesso.situacao", is(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO.name())));

        mockMvc.perform(get("/api/subprocessos/{id}/analises-cadastro", SP_CODIGO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].observacoes", is("Favor ajustar atividades")));

        // --- ETAPA 2: Validação de Pendências (Passo 7) ---
        
        // Limpa mapa para garantir que falhe por falta de atividades
        competenciaRepo.deleteByMapa_Codigo(sp.getMapa().getCodigo());
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/api/subprocessos/{id}/cadastro/disponibilizar", SP_CODIGO).with(csrf()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("O mapa de competências deve ter ao menos uma atividade cadastrada."));

        // --- ETAPA 3: Preparar Dados e Disponibilizar (Passos 9 a 16) ---
        
        var spEtapa3 = subprocessoRepo.findById(SP_CODIGO).orElseThrow();
        var competencia = competenciaRepo.save(Competencia.builder().descricao("Java").mapa(spEtapa3.getMapa()).build());
        var atividade = Atividade.builder().mapa(spEtapa3.getMapa()).descricao("Desenvolver APIs").build();
        atividade.getCompetencias().add(competencia);
        atividade = atividadeRepo.save(atividade);
        
        conhecimentoRepo.save(Conhecimento.builder()
                .descricao("Spring Boot")
                .atividade(atividade)
                .build());

        entityManager.flush();
        entityManager.clear();

        // Ação de Disponibilizar
        mockMvc.perform(post("/api/subprocessos/{id}/cadastro/disponibilizar", SP_CODIGO).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem", is("Cadastro de atividades disponibilizado")));

        // --- ETAPA 4: Verificações Pós-Ação ---

        Subprocesso atualizado = subprocessoRepo.findById(SP_CODIGO).orElseThrow();
        
        // 10. Alteração de Situação
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        
        // 11. Registro de Movimentação
        List<Movimentacao> movs = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(SP_CODIGO);
        assertThat(movs).isNotEmpty();
        assertThat(movs.getFirst().getDescricao()).isEqualTo("Disponibilização do cadastro de atividades");
        assertThat(movs.getFirst().getUnidadeOrigem().getSigla()).isEqualTo("SEDESENV");
        assertThat(movs.getFirst().getUnidadeDestino().getSigla()).isEqualTo("COSIS");

        // 12. Notificação por E-mail (Superior da 8 é COSIS)
        aguardarEmail(1);

        // 13. Criação de Alerta para Unidade Superior (Async)
        final Long processoCodigo = spEtapa3.getProcesso().getCodigo();
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var alertas = alertaRepo.findByProcessoCodigo(processoCodigo);
                    assertThat(alertas.stream().anyMatch(a -> a.getUnidadeDestino() != null && a.getUnidadeDestino().getCodigo() == 6L)).isTrue();
                });

        // 14. Data de Fim Etapa 1
        assertThat(atualizado.getDataFimEtapa1()).isNotNull();

        // 15. Exclusão do Histórico de Análise
        assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(SP_CODIGO)).isEmpty();
    }
}
