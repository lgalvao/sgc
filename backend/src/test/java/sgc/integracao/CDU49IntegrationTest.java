package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import sgc.alerta.model.*;
import sgc.comum.*;
import sgc.diagnostico.model.*;
import sgc.diagnostico.service.*;
import sgc.integracao.mocks.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@DisplayName("CDU-49: Concluir diagnóstico da unidade")
class CDU49IntegrationTest extends DiagnosticoCduIntegrationTestBase {
    private static final String API_DIAGNOSTICO = "/api/subprocessos/{codSubprocesso}/diagnostico";

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    @Autowired
    private DiagnosticoFluxoService diagnosticoFluxoService;

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
        assertThat(alertaRepo.findByProcessoCodigo(processo.getCodigo()))
                .filteredOn(alerta -> alerta.getDescricao().equals("Diagnóstico da unidade SEDIA submetido para análise"))
                .hasSize(1);
        assertThat(notificacaoEmailRepo.findAll()).anySatisfy(notificacao -> {
            assertThat(notificacao.getAssunto()).contains("Diagnóstico da unidade SEDIA submetido para análise");
            assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo("COSIS");
        });
    }

    @Test
    @WithMockChefe("333333333333")
    @DisplayName("Deve notificar novamente ao concluir diagnóstico após devolução")
    void deveNotificarNovaConclusaoAposDevolucao() {
        diagnosticoFluxoService.concluirDiagnosticoUnidade(subprocesso.getCodigo());
        diagnosticoFluxoService.devolverDiagnostico(subprocesso.getCodigo(), "Ajustar diagnóstico");

        preencherConsenso("50003", 4, 4, 4, 4, 4, 4, 4, 4, SituacaoAvaliacaoServidor.CONSENSO_APROVADO);
        preencherSituacoesCapacitacao("50003", ValorSituacaoCapacitacao.EC, ValorSituacaoCapacitacao.C);
        diagnosticoFluxoService.concluirDiagnosticoUnidade(subprocesso.getCodigo());

        List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll().stream()
                .filter(notificacao -> notificacao.getTipoNotificacao() == TipoNotificacao.DIAGNOSTICO_CONCLUIDO)
                .toList();

        assertThat(notificacoes).hasSize(2);
        assertThat(notificacoes)
                .extracting(NotificacaoEmail::getChaveIdempotencia)
                .doesNotHaveDuplicates();
    }
}
