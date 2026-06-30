package sgc.integracao;

import jakarta.persistence.*;
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
@DisplayName("CDU-52: Homologar diagnósticos em bloco")
class CDU52IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private AnaliseRepo analiseRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    @Autowired
    private EntityManager entityManager;

    private Processo processo;
    private Subprocesso subprocesso1;
    private Subprocesso subprocesso2;
    private Unidade unidadeAdmin;

    @BeforeEach
    void setUp() {
        unidadeAdmin = unidadeRepo.findById(1L).orElseThrow();
        Unidade unidadeAnalise = unidadeRepo.findById(6L).orElseThrow();
        Unidade unidade1 = unidadeRepo.findById(9L).orElseThrow();
        Unidade unidade2 = unidadeRepo.findById(10L).orElseThrow();
        Usuario usuarioGestor = usuarioRepo.findById("666666666666").orElseThrow();

        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.DIAGNOSTICO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo diagnóstico CDU-52");
        processo.adicionarParticipantes(Set.of(unidade1, unidade2));
        processo = processoRepo.saveAndFlush(processo);

        subprocesso1 = criarSubprocessoConcluido(processo, unidade1);
        subprocesso2 = criarSubprocessoConcluido(processo, unidade2);

        registrarAceitePrevio(subprocesso1, unidade1, unidadeAnalise, usuarioGestor);
        registrarAceitePrevio(subprocesso2, unidade2, unidadeAnalise, usuarioGestor);

        entityManager.flush();
        entityManager.clear();
        recarregar();
    }

    @Test
    @WithMockAdmin
    @DisplayName("ADMIN deve homologar diagnósticos em bloco sem gerar alertas ou e-mails")
    void adminDeveHomologarDiagnosticosEmBloco() throws Exception {
        mockMvc.perform(post("/api/processos/{codigo}/acao-em-bloco", processo.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "unidadeCodigos": [9, 10],
                                  "acao": "HOMOLOGAR"
                                }
                                """))
                .andExpect(status().isOk());

        recarregar();

        assertThat(subprocesso1.getSituacao()).isEqualTo(SituacaoSubprocesso.DIAGNOSTICO_HOMOLOGADO);
        assertThat(subprocesso2.getSituacao()).isEqualTo(SituacaoSubprocesso.DIAGNOSTICO_HOMOLOGADO);

        assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso1.getCodigo()))
                .anySatisfy(analise -> assertThat(analise.getAcao()).isEqualTo(TipoAcaoAnalise.HOMOLOGACAO_DIAGNOSTICO));
        assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso2.getCodigo()))
                .anySatisfy(analise -> assertThat(analise.getAcao()).isEqualTo(TipoAcaoAnalise.HOMOLOGACAO_DIAGNOSTICO));

        assertThat(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(subprocesso1.getCodigo()))
                .anySatisfy(movimentacao -> {
                    assertThat(movimentacao.getDescricao()).isEqualTo(Mensagens.HIST_DIAGNOSTICO_HOMOLOGADO);
                    assertThat(movimentacao.getUnidadeOrigem().getCodigo()).isEqualTo(unidadeAdmin.getCodigo());
                    assertThat(movimentacao.getUnidadeDestino().getCodigo()).isEqualTo(unidadeAdmin.getCodigo());
                });
        assertThat(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(subprocesso2.getCodigo()))
                .anySatisfy(movimentacao -> {
                    assertThat(movimentacao.getDescricao()).isEqualTo(Mensagens.HIST_DIAGNOSTICO_HOMOLOGADO);
                    assertThat(movimentacao.getUnidadeOrigem().getCodigo()).isEqualTo(unidadeAdmin.getCodigo());
                    assertThat(movimentacao.getUnidadeDestino().getCodigo()).isEqualTo(unidadeAdmin.getCodigo());
                });

        assertThat(alertaRepo.findByProcessoCodigo(processo.getCodigo())).isEmpty();
        assertThat(notificacaoEmailRepo.findAll().stream()
                .filter(notificacao -> notificacao.getTipoNotificacao() == TipoNotificacao.DIAGNOSTICO_HOMOLOGADO))
                .isEmpty();
    }

    private Subprocesso criarSubprocessoConcluido(Processo processo, Unidade unidade) {
        Subprocesso subprocesso = SubprocessoFixture.novoSubprocesso(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        return subprocessoRepo.saveAndFlush(subprocesso);
    }

    private void registrarAceitePrevio(Subprocesso subprocesso, Unidade unidadeOrigem, Unidade unidadeAnalise, Usuario usuarioGestor) {
        analiseRepo.saveAndFlush(Analise.builder()
                .tipo(TipoAnalise.DIAGNOSTICO)
                .subprocesso(subprocesso)
                .acao(TipoAcaoAnalise.ACEITE_DIAGNOSTICO)
                .dataHora(LocalDateTime.now())
                .unidadeCodigo(unidadeAnalise.getCodigo())
                .usuarioTitulo(usuarioGestor.getTituloEleitoral())
                .build());
        movimentacaoRepo.saveAndFlush(Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(unidadeAnalise)
                .usuario(usuarioGestor)
                .descricao("Conclusão de diagnóstico")
                .build());
        movimentacaoRepo.saveAndFlush(Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidadeAnalise)
                .unidadeDestino(unidadeAdmin)
                .usuario(usuarioGestor)
                .descricao(Mensagens.HIST_DIAGNOSTICO_ACEITO)
                .build());
    }

    private void recarregar() {
        entityManager.flush();
        entityManager.clear();
        processo = processoRepo.findById(processo.getCodigo()).orElseThrow();
        subprocesso1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        subprocesso2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
    }
}
