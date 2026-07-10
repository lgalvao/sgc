package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;
import sgc.comum.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-22: Aceitar cadastros em bloco")
class CDU22IntegrationTest extends BaseIntegrationTest {
    private static final Long codAdmin = 1L;
    private static final Long codSecretaria = 2L;
    private static final Long codCoordenadoria = 6L;
    private static final Long codSecaoDesenvolvimento = 8L;
    private static final Long codSecaoDados = 9L;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private AnaliseRepo analiseRepo;
    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;
    @Autowired
    private AlertaRepo alertaRepo;

    private Unidade unidadeSuperior;
    private Subprocesso subprocesso1;
    private Subprocesso subprocesso2;
    private Processo processo;

    @BeforeEach
    void setUp() {
        unidadeSuperior = unidadeRepo.findById(codCoordenadoria)
                .orElseThrow(() -> new RuntimeException("Coordenadoria não encontrada no data.sql"));
        Unidade unidade1 = unidadeRepo.findById(codSecaoDesenvolvimento)
                .orElseThrow(() -> new RuntimeException("Seção de desenvolvimento não encontrada no data.sql"));
        Unidade unidade2 = unidadeRepo.findById(codSecaoDados)
                .orElseThrow(() -> new RuntimeException("Seção de dados não encontrada no data.sql"));
        Usuario usuarioGestor = usuarioRepo.findById("666666666666").orElseThrow();

        // Create test process
        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo bloco CDU-22");
        processo = processoRepo.save(processo);

        // Create subprocesses for both units
        subprocesso1 = SubprocessoFixture.subprocessoPadrao(processo, unidade1);
        subprocesso1.setCodigo(null);
        subprocesso1.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocesso1.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso1 = subprocessoRepo.save(subprocesso1);

        subprocesso2 = SubprocessoFixture.subprocessoPadrao(processo, unidade2);
        subprocesso2.setCodigo(null);
        subprocesso2.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocesso2.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso2 = subprocessoRepo.save(subprocesso2);

        Movimentacao m1 = Movimentacao.builder()
                .subprocesso(subprocesso1)
                .unidadeOrigem(unidade1)
                .unidadeDestino(unidadeSuperior)
                .descricao(Mensagens.HIST_CADASTRO_DISPONIBILIZADO)
                .usuario(usuarioGestor)
                .build();
        movimentacaoRepo.save(m1);

        Movimentacao m2 = Movimentacao.builder()
                .subprocesso(subprocesso2)
                .unidadeOrigem(unidade2)
                .unidadeDestino(unidadeSuperior)
                .descricao(Mensagens.HIST_CADASTRO_DISPONIBILIZADO)
                .usuario(usuarioGestor)
                .build();
        movimentacaoRepo.save(m2);

        entityManager.flush();
        entityManager.clear();

        // Reload to attach
        processoRepo.findById(processo.getCodigo()).orElseThrow();
        subprocesso1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        subprocesso2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
        unidadeSuperior = unidadeRepo.findById(codCoordenadoria).orElseThrow();
    }

    @Test
    @DisplayName("Deve aceitar cadastro de múltiplas unidades em bloco")
    @WithMockGestor("666666666666")
        // GESTOR of unit 6 (parent of units 8 and 9)
    void aceitarCadastroEmBloco_deveAceitarTodasSelecionadas() throws Exception {

        List<Long> subprocessosSelecionados = List.of(subprocesso1.getCodigo(), subprocesso2.getCodigo());

        ProcessarEmBlocoRequest request = ProcessarEmBlocoRequest.builder()
                .subprocessos(subprocessosSelecionados)
                .build();

        mockMvc.perform(
                        post("/api/subprocessos/aceitar-cadastro-bloco")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        // Verify subprocesso 1
        Subprocesso s1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        List<Analise> analises1 = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s1.getCodigo());
        assertThat(analises1).isNotEmpty();
        assertThat(analises1.getFirst().getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE_MAPEAMENTO);
        assertThat(analises1.getFirst().getUnidadeCodigo()).isEqualTo(unidadeSuperior.getCodigo());

        List<Movimentacao> movs1 = movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(s1.getCodigo());
        assertThat(movs1).isNotEmpty();
        assertThat(movs1.getFirst().getDescricao()).contains("aceito");
        assertThat(movs1.getFirst().getUnidadeDestino().getCodigo()).isEqualTo(codSecretaria);

        // Verify subprocesso 2
        Subprocesso s2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
        List<Analise> analises2 = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s2.getCodigo());
        assertThat(analises2).isNotEmpty();
        assertThat(analises2.getFirst().getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE_MAPEAMENTO);

        List<Movimentacao> movs2 = movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(s2.getCodigo());
        assertThat(movs2).isNotEmpty();
        assertThat(movs2.getFirst().getUnidadeDestino().getCodigo()).isEqualTo(codSecretaria);

        List<Alerta> alertas = alertaRepo.findByProcessoCodigo(processo.getCodigo());
        assertThat(alertas).anySatisfy(alerta -> {
            assertThat(alerta.getDescricao()).isEqualTo(Mensagens.ALERTA_CADASTRO_ACEITO.formatted("SEDESENV"));
        });
        assertThat(alertas).anySatisfy(alerta -> {
            assertThat(alerta.getDescricao()).isEqualTo(Mensagens.ALERTA_CADASTRO_ACEITO.formatted("SEDIA"));
        });

        List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll().stream()
                .filter(n -> n.getTipoNotificacao() == TipoNotificacao.CADASTRO_ACEITO)
                .filter(n -> n.getUsuarioDestinoTitulo() == null)
                .toList();
        assertThat(notificacoes).hasSize(1);
        assertThat(notificacoes).anySatisfy(notificacao -> {
            assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo("STIC");
            assertThat(notificacao.getUnidadeOrigemSigla()).isEqualTo("COSIS");
            assertThat(notificacao.getDestinatario()).isEqualTo("stic@tre-pe.jus.br");
            assertThat(notificacao.getAssunto()).isEqualTo("SGC: Cadastros de atividades e conhecimentos submetidos para análise");
            assertThat(notificacao.getCorpoHtml()).contains("SEDESENV, SEDIA");
            assertThat(notificacao.getSituacao()).isIn(SituacaoNotificacao.PENDENTE, SituacaoNotificacao.ENVIADO);
        });
        assertThat(notificacoes)
                .extracting(NotificacaoEmail::getUnidadeDestinoSigla)
                .doesNotContain("SEDESENV", "SEDIA", "COSIS");

        aguardarEmail(1);
        assertThat(algumEmailPara("stic@tre-pe.jus.br")).isTrue();
        assertThat(algumEmailComAssunto("[SGC-TEST] Cadastros de atividades e conhecimentos submetidos para análise")).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar cadastro em bloco no último nível e notificar a ADMIN")
    void aceitarCadastroEmBlocoNoUltimoNivel_deveNotificarAdmin() throws Exception {
        Unidade secretaria = unidadeRepo.findById(codSecretaria)
                .orElseThrow(() -> new RuntimeException("Secretaria não encontrada no data.sql"));
        Unidade coordenadoria = unidadeRepo.findById(codCoordenadoria)
                .orElseThrow(() -> new RuntimeException("Coordenadoria não encontrada no data.sql"));
        Unidade secao = unidadeRepo.findById(codSecaoDesenvolvimento)
                .orElseThrow(() -> new RuntimeException("Seção não encontrada no data.sql"));

        Processo processoFinal = ProcessoFixture.processoPadrao();
        processoFinal.setCodigo(null);
        processoFinal.setTipo(TipoProcesso.MAPEAMENTO);
        processoFinal.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoFinal.setDescricao("Processo bloco final CDU-22");
        processoFinal = processoRepo.save(processoFinal);

        Subprocesso subprocessoFinal = SubprocessoFixture.subprocessoPadrao(processoFinal, secao);
        subprocessoFinal.setCodigo(null);
        subprocessoFinal.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocessoFinal.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocessoFinal = subprocessoRepo.save(subprocessoFinal);

        Usuario usuarioGestorNivelFinal = usuarioRepo.findById("666666666666").orElseThrow();
        usuarioGestorNivelFinal.setPerfilAtivo(Perfil.GESTOR);
        usuarioGestorNivelFinal.setUnidadeAtivaCodigo(secretaria.getCodigo());
        usuarioGestorNivelFinal.setAuthorities(Set.of(Perfil.GESTOR.toGrantedAuthority()));
        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocessoFinal)
                .unidadeOrigem(secao)
                .unidadeDestino(coordenadoria)
                .descricao(Mensagens.HIST_CADASTRO_DISPONIBILIZADO)
                .usuario(usuarioGestorNivelFinal)
                .build());
        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocessoFinal)
                .unidadeOrigem(coordenadoria)
                .unidadeDestino(secretaria)
                .descricao(Mensagens.HIST_CADASTRO_DISPONIBILIZADO)
                .usuario(usuarioGestorNivelFinal)
                .build());

        entityManager.flush();
        entityManager.clear();

        ProcessarEmBlocoRequest request = ProcessarEmBlocoRequest.builder()
                .subprocessos(List.of(subprocessoFinal.getCodigo()))
                .build();

        mockMvc.perform(
                        post("/api/subprocessos/aceitar-cadastro-bloco")
                                .with(user(usuarioGestorNivelFinal))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        List<Movimentacao> movimentacoes = movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(subprocessoFinal.getCodigo());
        assertThat(movimentacoes).isNotEmpty();
        assertThat(movimentacoes.getFirst().getUnidadeOrigem().getCodigo()).isEqualTo(secretaria.getCodigo());
        assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla()).isEqualTo("ADMIN");

        assertThat(alertaRepo.findByProcessoCodigo(processoFinal.getCodigo()))
                .anySatisfy(alerta -> {
                    assertThat(alerta.getDescricao()).isEqualTo(Mensagens.ALERTA_CADASTRO_ACEITO.formatted("SEDESENV"));
                    assertThat(alerta.getUnidadeDestino().getSigla()).isEqualTo("ADMIN");
                });

        assertThat(notificacaoEmailRepo.findAll().stream()
                .filter(n -> n.getTipoNotificacao() == TipoNotificacao.CADASTRO_ACEITO)
                .toList())
                .anySatisfy(notificacao -> {
                    assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo("ADMIN");
                    assertThat(notificacao.getUnidadeOrigemSigla()).isEqualTo("STIC");
                    assertThat(notificacao.getDestinatario()).isEqualTo("admin@tre-pe.jus.br");
                    assertThat(notificacao.getAssunto()).isEqualTo("SGC: Cadastros de atividades e conhecimentos submetidos para análise");
                    assertThat(notificacao.getCorpoHtml()).contains("SEDESENV");
                });
        assertThat(notificacaoEmailRepo.findAll().stream()
                .filter(n -> n.getTipoNotificacao() == TipoNotificacao.CADASTRO_ACEITO)
                .map(NotificacaoEmail::getUnidadeDestinoSigla)
                .toList())
                .doesNotContain("STIC");
    }
}
