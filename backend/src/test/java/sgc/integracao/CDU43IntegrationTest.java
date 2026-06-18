package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockGestor;
import sgc.mapa.model.Mapa;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@DisplayName("CDU-43: Visualizar detalhes de subprocesso de diagnóstico para GESTOR e ADMIN")
class CDU43IntegrationTest extends BaseIntegrationTest {

    @Test
    @WithMockGestor("222222222222")
    @DisplayName("GESTOR deve ver apenas a própria hierarquia no detalhamento do processo")
    void gestorDeveVerSomenteSuaHierarquia() throws Exception {
        Processo processo = criarProcessoDiagnosticoHierarquico();

        String resposta = mockMvc.perform(get("/api/processos/{codigo}/detalhes", processo.getCodigo()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(resposta).contains("GESTOR-UNIT");
        assertThat(resposta).contains("SUB-UNIT");
        assertThat(resposta).doesNotContain("SEDOC");
    }

    @Test
    @WithMockAdmin
    @DisplayName("ADMIN deve ver todas as unidades participantes no detalhamento do processo")
    void adminDeveVerTodasAsUnidadesParticipantes() throws Exception {
        Processo processo = criarProcessoDiagnosticoHierarquico();

        String resposta = mockMvc.perform(get("/api/processos/{codigo}/detalhes", processo.getCodigo()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(resposta).contains("GESTOR-UNIT");
        assertThat(resposta).contains("SUB-UNIT");
        assertThat(resposta).contains("SEDOC");
    }

    private Processo criarProcessoDiagnosticoHierarquico() {
        Processo processo = ProcessoFixture.novoProcesso();
        processo.setDescricao("Diagnóstico CDU-43 " + System.nanoTime());
        processo.setTipo(TipoProcesso.DIAGNOSTICO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataCriacao(LocalDateTime.now());
        processo.setDataLimite(LocalDateTime.now().plusDays(20));
        processo.adicionarParticipantes(Set.of(
                unidadeRepo.findById(101L).orElseThrow(),
                unidadeRepo.findById(102L).orElseThrow(),
                unidadeRepo.findById(15L).orElseThrow()
        ));
        processo = processoRepo.saveAndFlush(processo);

        criarSubprocessoDiagnostico(processo, 101L);
        criarSubprocessoDiagnostico(processo, 102L);
        criarSubprocessoDiagnostico(processo, 15L);
        return processo;
    }

    private void criarSubprocessoDiagnostico(Processo processo, Long codigoUnidade) {
        Subprocesso subprocesso = SubprocessoFixture.novoSubprocesso(
                processo,
                unidadeRepo.findById(codigoUnidade).orElseThrow()
        );
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso = subprocessoRepo.saveAndFlush(subprocesso);

        Mapa mapa = mapaRepo.saveAndFlush(Mapa.builder().subprocesso(subprocesso).build());
        subprocesso.setMapa(mapa);
        subprocessoRepo.saveAndFlush(subprocesso);
        registrarMovimentacaoInicial(subprocesso);
    }
}
