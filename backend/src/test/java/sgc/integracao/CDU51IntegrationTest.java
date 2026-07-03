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
@DisplayName("CDU-51: Aceitar diagnósticos em bloco")
class CDU51IntegrationTest extends BaseIntegrationTest {
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
    private Unidade unidadeAnalise;
    private Unidade unidadeSuperior;

    @BeforeEach
    void setUp() {
        unidadeAnalise = unidadeRepo.findById(6L).orElseThrow();
        unidadeSuperior = unidadeRepo.findById(2L).orElseThrow();
        Unidade unidade1 = unidadeRepo.findById(9L).orElseThrow();
        Unidade unidade2 = unidadeRepo.findById(10L).orElseThrow();
        Usuario usuarioGestor = usuarioRepo.findById("666666666666").orElseThrow();

        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.DIAGNOSTICO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo diagnóstico CDU-51");
        processo.adicionarParticipantes(Set.of(unidade1, unidade2));
        processo = processoRepo.saveAndFlush(processo);

        subprocesso1 = criarSubprocessoConcluido(processo, unidade1);
        subprocesso2 = criarSubprocessoConcluido(processo, unidade2);

        movimentacaoRepo.saveAndFlush(Movimentacao.builder()
                .subprocesso(subprocesso1)
                .unidadeOrigem(unidade1)
                .unidadeDestino(unidadeAnalise)
                .usuario(usuarioGestor)
                .descricao("Conclusão de diagnóstico")
                .build());
        movimentacaoRepo.saveAndFlush(Movimentacao.builder()
                .subprocesso(subprocesso2)
                .unidadeOrigem(unidade2)
                .unidadeDestino(unidadeAnalise)
                .usuario(usuarioGestor)
                .descricao("Conclusão de diagnóstico")
                .build());

        entityManager.flush();
        entityManager.clear();
        recarregar();
    }

    @Test
    @WithMockGestor("666666666666")
    @DisplayName("GESTOR deve aceitar diagnósticos em bloco com notificação e alerta consolidados")
    void gestorDeveAceitarDiagnosticosEmBloco() throws Exception {
        mockMvc.perform(post("/api/processos/{codigo}/acao-em-bloco", processo.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "unidadeCodigos": [9, 10],
                                  "acao": "ACEITAR"
                                }
                                """))
                .andExpect(status().isOk());

        recarregar();

        assertThat(subprocesso1.getSituacao()).isEqualTo(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        assertThat(subprocesso2.getSituacao()).isEqualTo(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso1.getCodigo()))
                .anySatisfy(analise -> assertThat(analise.getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE_DIAGNOSTICO));
        assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso2.getCodigo()))
                .anySatisfy(analise -> assertThat(analise.getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE_DIAGNOSTICO));

        assertThat(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(subprocesso1.getCodigo()))
                .anySatisfy(movimentacao -> {
                    assertThat(movimentacao.getDescricao()).isEqualTo(Mensagens.HIST_DIAGNOSTICO_ACEITO);
                    assertThat(movimentacao.getUnidadeOrigem().getCodigo()).isEqualTo(unidadeAnalise.getCodigo());
                    assertThat(movimentacao.getUnidadeDestino().getCodigo()).isEqualTo(unidadeSuperior.getCodigo());
                });
        assertThat(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(subprocesso2.getCodigo()))
                .anySatisfy(movimentacao -> {
                    assertThat(movimentacao.getDescricao()).isEqualTo(Mensagens.HIST_DIAGNOSTICO_ACEITO);
                    assertThat(movimentacao.getUnidadeOrigem().getCodigo()).isEqualTo(unidadeAnalise.getCodigo());
                    assertThat(movimentacao.getUnidadeDestino().getCodigo()).isEqualTo(unidadeSuperior.getCodigo());
                });

        List<Alerta> alertas = alertaRepo.findByProcessoCodigo(processo.getCodigo());
        assertThat(alertas).anySatisfy(alerta -> {
            assertThat(alerta.getDescricao()).isEqualTo(Mensagens.ALERTA_DIAGNOSTICO_ACEITO_BLOCO);
            assertThat(alerta.getUnidadeOrigem().getCodigo()).isEqualTo(unidadeAnalise.getCodigo());
            assertThat(alerta.getUnidadeDestino().getCodigo()).isEqualTo(unidadeSuperior.getCodigo());
        });
        assertThat(alertas)
                .noneSatisfy(alerta -> assertThat(alerta.getDescricao()).isEqualTo("Diagnóstico da unidade SEDIA aceito"))
                .noneSatisfy(alerta -> assertThat(alerta.getDescricao()).isEqualTo("Diagnóstico da unidade SESEL aceito"));

        List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll().stream()
                .filter(notificacao -> notificacao.getTipoNotificacao() == TipoNotificacao.DIAGNOSTICO_ACEITO)
                .toList();
        assertThat(notificacoes).singleElement().satisfies(notificacao -> {
            assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo("STIC");
            assertThat(notificacao.getDestinatario()).isEqualTo("stic@tre-pe.jus.br");
            assertThat(notificacao.getAssunto()).isEqualTo("SGC: Diagnósticos submetidos para análise");
            assertThat(notificacao.getCorpoHtml()).contains("SEDIA", "SESEL");
        });

        aguardarEmail(1);
        assertThat(algumEmailPara("stic@tre-pe.jus.br")).isTrue();
        assertThat(algumEmailComAssunto("[SGC-TEST] Diagnósticos submetidos para análise")).isTrue();
    }

    @Test
    @DisplayName("GESTOR deve aceitar diagnósticos em bloco no último nível e notificar a ADMIN")
    void gestorDeveAceitarDiagnosticosEmBlocoNoUltimoNivel() throws Exception {
        Unidade unidadeAnaliseFinal = unidadeRepo.findById(2L).orElseThrow();
        Unidade unidadeOrigem = unidadeRepo.findById(6L).orElseThrow();
        Usuario usuarioGestor = usuarioRepo.findById("666666666666").orElseThrow();
        usuarioGestor.setPerfilAtivo(Perfil.GESTOR);
        usuarioGestor.setUnidadeAtivaCodigo(unidadeAnaliseFinal.getCodigo());
        usuarioGestor.setAuthorities(Set.of(Perfil.GESTOR.toGrantedAuthority()));

        Processo processoFinal = ProcessoFixture.processoPadrao();
        processoFinal.setCodigo(null);
        processoFinal.setTipo(TipoProcesso.DIAGNOSTICO);
        processoFinal.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoFinal.setDescricao("Processo diagnóstico final CDU-51");
        processoFinal.adicionarParticipantes(Set.of(unidadeOrigem));
        processoFinal = processoRepo.saveAndFlush(processoFinal);

        Subprocesso subprocessoFinal = criarSubprocessoConcluido(processoFinal, unidadeOrigem);
        movimentacaoRepo.saveAndFlush(Movimentacao.builder()
                .subprocesso(subprocessoFinal)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(unidadeAnaliseFinal)
                .usuario(usuarioGestor)
                .descricao("Conclusão de diagnóstico")
                .build());

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/api/processos/{codigo}/acao-em-bloco", processoFinal.getCodigo())
                        .with(user(usuarioGestor))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "unidadeCodigos": [6],
                                  "acao": "ACEITAR"
                                }
                                """))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Subprocesso subprocessoRecarregado = subprocessoRepo.findById(subprocessoFinal.getCodigo()).orElseThrow();
        assertThat(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(subprocessoRecarregado.getCodigo()))
                .anySatisfy(movimentacao -> {
                    assertThat(movimentacao.getDescricao()).isEqualTo(Mensagens.HIST_DIAGNOSTICO_ACEITO);
                    assertThat(movimentacao.getUnidadeOrigem().getCodigo()).isEqualTo(unidadeAnaliseFinal.getCodigo());
                    assertThat(movimentacao.getUnidadeDestino().getSigla()).isEqualTo("ADMIN");
                });

        assertThat(alertaRepo.findByProcessoCodigo(processoFinal.getCodigo()))
                .anySatisfy(alerta -> {
                    assertThat(alerta.getDescricao()).isEqualTo(Mensagens.ALERTA_DIAGNOSTICO_ACEITO_BLOCO);
                    assertThat(alerta.getUnidadeDestino().getSigla()).isEqualTo("ADMIN");
                });

        assertThat(notificacaoEmailRepo.findAll().stream()
                .filter(notificacao -> notificacao.getSubprocesso().getCodigo().equals(subprocessoRecarregado.getCodigo()))
                .filter(notificacao -> notificacao.getTipoNotificacao() == TipoNotificacao.DIAGNOSTICO_ACEITO))
                .singleElement()
                .satisfies(notificacao -> {
                    assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo("ADMIN");
                    assertThat(notificacao.getDestinatario()).isEqualTo("admin@tre-pe.jus.br");
                    assertThat(notificacao.getAssunto()).isEqualTo("SGC: Diagnósticos submetidos para análise");
                });
    }

    private Subprocesso criarSubprocessoConcluido(Processo processo, Unidade unidade) {
        Subprocesso subprocesso = SubprocessoFixture.novoSubprocesso(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        return subprocessoRepo.saveAndFlush(subprocesso);
    }

    private void recarregar() {
        entityManager.flush();
        entityManager.clear();
        processo = processoRepo.findById(processo.getCodigo()).orElseThrow();
        subprocesso1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        subprocesso2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
    }
}
