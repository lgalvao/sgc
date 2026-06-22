package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.NotificacaoEmailRepo;
import sgc.comum.Mensagens;
import sgc.diagnostico.model.SituacaoAvaliacaoServidor;
import sgc.diagnostico.model.ValorSituacaoCapacitacao;
import sgc.integracao.mocks.WithMockChefe;
import sgc.subprocesso.model.SituacaoSubprocesso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@DisplayName("CDU-49: Concluir diagnóstico da unidade")
class CDU49IntegrationTest extends DiagnosticoCduIntegrationTestBase {
    private static final String API_DIAGNOSTICO = "/api/subprocessos/{codSubprocesso}/diagnostico";

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    @BeforeEach
    void setUp() {
        criarCenarioDiagnosticoBase(9L, "50003", "50004");
        preencherConsenso("50003", 4, 4, 4, 4, 4, 4, 4, 4, SituacaoAvaliacaoServidor.CONSENSO_APROVADO);
        preencherAutoavaliacao("50004", 3, 3, 2, 2, SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);
        preencherSituacoesCapacitacao("50003", ValorSituacaoCapacitacao.EC, ValorSituacaoCapacitacao.C);
    }

    @Test
    @WithMockChefe("333333333333")
    @DisplayName("Deve barrar a conclusão quando ainda houver pendências")
    void deveBarrarConclusaoQuandoHouverPendencias() throws Exception {
        preencherSituacoesCapacitacao("50003", ValorSituacaoCapacitacao.EC, null);

        mockMvc.perform(get(API_DIAGNOSTICO + "/concluir/validacao", subprocesso.getCodigo()))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithMockChefe("333333333333")
    @DisplayName("Deve barrar a conclusão quando todos os servidores estiverem impossibilitados")
    void deveBarrarConclusaoQuandoTodosServidoresImpossibilitados() throws Exception {
        preencherAutoavaliacao("50003", 4, 4, 4, 4, SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);

        mockMvc.perform(get(API_DIAGNOSTICO + "/concluir/validacao", subprocesso.getCodigo()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value(Mensagens.DIAGNOSTICO_TODOS_SERVIDORES_IMPOSSIBILITADOS));
    }

    @Test
    @WithMockChefe("333333333333")
    @DisplayName("Deve concluir o diagnóstico, registrar movimentação e notificar a unidade superior")
    void deveConcluirDiagnosticoDaUnidade() throws Exception {
        mockMvc.perform(post(API_DIAGNOSTICO + "/concluir", subprocesso.getCodigo()).with(csrf()))
                .andExpect(status().isOk());

        recarregarContexto();

        assertThat(subprocesso.getSituacao()).isEqualTo(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        assertThat(subprocesso.getDataFimEtapa1()).isNotNull();
        assertThat(diagnostico.getDataConclusao()).isNotNull();
        assertThat(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(subprocesso.getCodigo()))
                .anySatisfy(movimentacao -> {
                    assertThat(movimentacao.getDescricao()).isEqualTo("Conclusão de diagnóstico");
                    assertThat(movimentacao.getUnidadeOrigem().getSigla()).isEqualTo("SEDIA");
                    assertThat(movimentacao.getUnidadeDestino().getSigla()).isEqualTo("COSIS");
                });
        assertThat(alertaRepo.findAll()).anySatisfy(alerta ->
                assertThat(alerta.getDescricao()).isEqualTo("Diagnóstico da unidade SEDIA submetido para análise"));
        assertThat(notificacaoEmailRepo.findAll()).anySatisfy(notificacao -> {
            assertThat(notificacao.getAssunto()).contains("Diagnóstico da unidade SEDIA submetido para análise");
            assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo("COSIS");
        });
    }
}
