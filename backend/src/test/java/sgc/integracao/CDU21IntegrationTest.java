package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import sgc.alerta.model.NotificacaoEmail;
import sgc.alerta.model.NotificacaoEmailRepo;
import sgc.alerta.model.SituacaoNotificacao;
import sgc.alerta.model.TipoNotificacao;
import sgc.comum.Mensagens;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.UnidadeMapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@WithMockAdmin
@DisplayName("CDU-21: Finalizar processo de mapeamento ou revisão")
class CDU21IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;

    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    private Processo processo;
    private Subprocesso subprocesso;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("CDU21-UND");
        unidade.setNome("Unidade CDU-21");
        unidade = unidadeRepo.save(unidade);

        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setDescricao("Processo CDU-21");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);

        processo.adicionarParticipantes(Set.of(unidade));
        processoRepo.save(processo);

        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(7));
        subprocesso = subprocessoRepo.save(subprocesso);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapa = mapaRepo.save(mapa);

        subprocesso.setMapa(mapa);
        subprocessoRepo.save(subprocesso);
    }

    @Test
    @DisplayName("Não deve finalizar quando houver subprocesso não homologado")
    void naoDeveFinalizarQuandoSubprocessosNaoHomologados() throws Exception {
        mockMvc.perform(post("/api/processos/{codigo}/finalizar", processo.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value(Mensagens.SUBPROCESSOS_NAO_HOMOLOGADOS));

        Processo atualizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
        assertThat(atualizado.getDataFinalizacao()).isNull();
        assertThat(unidadeMapaRepo.findById(unidade.getCodigo())).isEmpty();
    }

    @Test
    @DisplayName("Não deve finalizar revisão quando estiver somente com cadastro homologado")
    void naoDeveFinalizarRevisaoComCadastroHomologado() throws Exception {
        processo.setTipo(TipoProcesso.REVISAO);
        processoRepo.save(processo);

        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        subprocessoRepo.save(subprocesso);

        mockMvc.perform(post("/api/processos/{codigo}/finalizar", processo.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value(Mensagens.SUBPROCESSOS_NAO_HOMOLOGADOS));

        Processo atualizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
        assertThat(atualizado.getDataFinalizacao()).isNull();
    }

    @Test
    @DisplayName("Deve finalizar processo e definir mapa vigente da unidade")
    void deveFinalizarProcessoEAtualizarMapaVigente() throws Exception {
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        subprocessoRepo.save(subprocesso);

        mockMvc.perform(post("/api/processos/{codigo}/finalizar", processo.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Processo atualizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        assertThat(atualizado.getDataFinalizacao()).isNotNull();

        UnidadeMapa unidadeMapa = unidadeMapaRepo.findById(unidade.getCodigo()).orElseThrow();
        assertThat(unidadeMapa.getMapaVigente()).isNotNull();
        assertThat(unidadeMapa.getMapaVigente().getCodigo()).isEqualTo(subprocesso.getMapa().getCodigo());


        mockMvc.perform(get("/api/unidades/{codUnidade}/mapa-vigente/referencia", unidade.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codProcesso").value(processo.getCodigo()))
                .andExpect(jsonPath("$.codSubprocesso").value(subprocesso.getCodigo()));

        List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll().stream()
                .filter(n -> n.getTipoNotificacao() == TipoNotificacao.PROCESSO_FINALIZADO)
                .toList();
        assertThat(notificacoes).hasSize(1);

        NotificacaoEmail notificacao = notificacoes.getFirst();
        assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo("CDU21-UND");
        assertThat(notificacao.getDestinatario()).isEqualTo("cdu21-und@tre-pe.jus.br");
        assertThat(notificacao.getAssunto()).isEqualTo("SGC: Finalização do processo Processo CDU-21");
        assertThat(notificacao.getCorpoHtml())
                .contains("Comunicamos a finalização do processo <strong>Processo CDU-21</strong> para a sua unidade.")
                .contains("menu \"Minha unidade\"");
        assertThat(notificacao.getSituacao()).isEqualTo(SituacaoNotificacao.PENDENTE);

        aguardarEmail(1);
        assertThat(algumEmailPara("cdu21-und@tre-pe.jus.br")).isTrue();
        assertThat(algumEmailComAssunto("[SGC-TEST] Finalização do processo Processo CDU-21")).isTrue();
        assertThat(algumEmailContem("Comunicamos a finalização do processo")).isTrue();
        assertThat(algumEmailContem("Processo CDU-21")).isTrue();
        assertThat(algumEmailContem("menu \"Minha unidade\"")).isTrue();
    }

    @Test
    @DisplayName("Deve finalizar revisão com subprocesso em mapa homologado")
    void deveFinalizarRevisaoComMapaHomologado() throws Exception {
        processo.setTipo(TipoProcesso.REVISAO);
        processoRepo.save(processo);

        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO);
        subprocessoRepo.save(subprocesso);

        mockMvc.perform(post("/api/processos/{codigo}/finalizar", processo.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Processo atualizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        assertThat(atualizado.getDataFinalizacao()).isNotNull();
    }

    @Test
    @DisplayName("Deve finalizar processo DIAGNOSTICO sem tornar mapas vigentes")
    void deveFinalizarProcessoDiagnostico() throws Exception {
        processo.setTipo(TipoProcesso.DIAGNOSTICO);
        processoRepo.save(processo);

        subprocesso.setSituacaoForcada(SituacaoSubprocesso.DIAGNOSTICO_HOMOLOGADO);
        subprocessoRepo.save(subprocesso);

        mockMvc.perform(post("/api/processos/{codigo}/finalizar", processo.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Processo atualizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        assertThat(atualizado.getDataFinalizacao()).isNotNull();
        // DIAGNOSTICO não torna mapas vigentes
        assertThat(unidadeMapaRepo.findById(unidade.getCodigo())).isEmpty();
    }
}
