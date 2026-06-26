package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import sgc.alerta.model.*;
import sgc.comum.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@WithMockAdmin
@DisplayName("CDU-53: Finalizar processo de diagnóstico")
class CDU53IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;

    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    private Processo processo;
    private Unidade unidadeOperacional;
    private Unidade unidadeIntermediaria;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        unidadeIntermediaria = UnidadeFixture.unidadePadrao();
        unidadeIntermediaria.setCodigo(null);
        unidadeIntermediaria.setSigla("CDU53-SUP");
        unidadeIntermediaria.setNome("Unidade intermediária CDU-53");
        unidadeIntermediaria.setTipo(TipoUnidade.INTERMEDIARIA);
        unidadeIntermediaria = unidadeRepo.save(unidadeIntermediaria);

        unidadeOperacional = UnidadeFixture.unidadePadrao();
        unidadeOperacional.setCodigo(null);
        unidadeOperacional.setSigla("CDU53-UND");
        unidadeOperacional.setNome("Unidade CDU-53");
        unidadeOperacional.setTipo(TipoUnidade.OPERACIONAL);
        unidadeOperacional.setUnidadeSuperior(unidadeIntermediaria);
        unidadeOperacional = unidadeRepo.save(unidadeOperacional);

        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setDescricao("Processo CDU-53");
        processo.setTipo(TipoProcesso.DIAGNOSTICO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);

        processo.adicionarParticipantes(Set.of(unidadeOperacional));
        processoRepo.save(processo);

        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidadeOperacional);
        subprocesso.setCodigo(null);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(7));
        subprocesso = subprocessoRepo.save(subprocesso);
    }

    @Test
    @DisplayName("Não deve finalizar quando houver subprocesso não homologado")
    void naoDeveFinalizarQuandoDiagnosticosNaoHomologados() throws Exception {
        mockMvc.perform(post("/api/processos/{codigo}/finalizar", processo.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value(Mensagens.PROCESSO_FINALIZACAO_DIAGNOSTICO_PENDENTE));

        Processo atualizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
        assertThat(atualizado.getDataFinalizacao()).isNull();
    }

    @Test
    @DisplayName("Deve finalizar diagnóstico homologado sem tornar mapas vigentes e com alertas consolidados")
    void deveFinalizarProcessoDiagnostico() throws Exception {
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.DIAGNOSTICO_HOMOLOGADO);
        subprocessoRepo.save(subprocesso);

        mockMvc.perform(post("/api/processos/{codigo}/finalizar", processo.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Processo atualizado = processoRepo.findById(processo.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        assertThat(atualizado.getDataFinalizacao()).isNotNull();
        assertThat(unidadeMapaRepo.findById(unidadeOperacional.getCodigo())).isEmpty();

        List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll().stream()
                .filter(n -> n.getTipoNotificacao() == TipoNotificacao.PROCESSO_FINALIZADO)
                .toList();
        assertThat(notificacoes).hasSize(2);

        NotificacaoEmail notificacaoDireta = notificacoes.stream()
                .filter(notificacao -> "CDU53-UND".equals(notificacao.getUnidadeDestinoSigla()))
                .findFirst()
                .orElseThrow();
        assertThat(notificacaoDireta.getDestinatario()).isEqualTo("cdu53-und@tre-pe.jus.br");
        assertThat(notificacaoDireta.getAssunto()).isEqualTo("SGC: Finalização de processo de diagnóstico");
        assertThat(notificacaoDireta.getCorpoHtml())
                .contains("Comunicamos a finalização do processo <strong>Processo CDU-53</strong>.")
                .contains("Os resultados consolidados do diagnóstico já podem ser consultados");

        NotificacaoEmail notificacaoConsolidada = notificacoes.stream()
                .filter(notificacao -> "CDU53-SUP".equals(notificacao.getUnidadeDestinoSigla()))
                .findFirst()
                .orElseThrow();
        assertThat(notificacaoConsolidada.getDestinatario()).isEqualTo("cdu53-sup@tre-pe.jus.br");
        assertThat(notificacaoConsolidada.getAssunto()).isEqualTo("SGC: Finalização de processo de diagnóstico em unidades subordinadas");
        assertThat(notificacaoConsolidada.getCorpoHtml())
                .contains("CDU53-UND")
                .contains("Os resultados do diagnóstico destas unidades podem ser consultados");

        assertThat(alertaRepo.findByProcessoCodigo(processo.getCodigo()))
                .extracting(Alerta::getDescricao)
                .contains("Processo finalizado", "Processo finalizado em unidades subordinadas");
    }
}
