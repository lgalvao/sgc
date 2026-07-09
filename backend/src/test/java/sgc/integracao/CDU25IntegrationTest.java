package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.*;
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
@DisplayName("CDU-25: Aceitar validação de mapas em bloco")
class CDU25IntegrationTest extends BaseIntegrationTest {
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

    private Subprocesso subprocesso1;
    private Subprocesso subprocesso2;
    private Processo processo;

    @BeforeEach
    void setUp() {
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
        processo.setDescricao("Processo validação CDU-25");
        processo = processoRepo.save(processo);

        // Create subprocesses in MAPEAMENTO_MAPA_VALIDADO state
        subprocesso1 = SubprocessoFixture.subprocessoPadrao(processo, unidade1);
        subprocesso1.setCodigo(null);
        subprocesso1.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocesso1 = subprocessoRepo.save(subprocesso1);

        subprocesso2 = SubprocessoFixture.subprocessoPadrao(processo, unidade2);
        subprocesso2.setCodigo(null);
        subprocesso2.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocesso2 = subprocessoRepo.save(subprocesso2);

        Unidade unidadeSuperior = unidadeRepo.findById(codCoordenadoria).orElseThrow();
        Movimentacao m1 = Movimentacao.builder()
                .subprocesso(subprocesso1)
                .unidadeOrigem(unidade1)
                .unidadeDestino(unidadeSuperior)
                .descricao(Mensagens.HIST_MAPA_VALIDADO)
                .dataHora(LocalDateTime.now())
                .usuario(usuarioGestor)
                .build();
        movimentacaoRepo.save(m1);

        Movimentacao m2 = Movimentacao.builder()
                .subprocesso(subprocesso2)
                .unidadeOrigem(unidade2)
                .unidadeDestino(unidadeSuperior)
                .descricao(Mensagens.HIST_MAPA_VALIDADO)
                .dataHora(LocalDateTime.now())
                .usuario(usuarioGestor)
                .build();
        movimentacaoRepo.save(m2);

        entityManager.flush();
        entityManager.clear();

        processoRepo.findById(processo.getCodigo()).orElseThrow();
        subprocesso1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        subprocesso2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
    }

    @Test
    @DisplayName("Deve aceitar validação de mapas em bloco")
    @WithMockGestor("666666666666")
        // GESTOR of unit 6 (parent of units 8 and 9)
    void aceitarValidacaoEmBloco_deveAceitarSucesso() throws Exception {

        List<Long> subprocessosSelecionados = List.of(subprocesso1.getCodigo(), subprocesso2.getCodigo());

        ProcessarEmBlocoRequest request = ProcessarEmBlocoRequest.builder()
                .subprocessos(subprocessosSelecionados)
                .build();

        mockMvc.perform(
                        post("/api/subprocessos/aceitar-validacao-bloco")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        // Check subprocesso 1
        Subprocesso s1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        List<Analise> analises1 = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s1.getCodigo());
        assertThat(analises1).isNotEmpty();
        assertThat(analises1.getFirst().getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE_MAPEAMENTO);

        List<Movimentacao> movs1 = movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(s1.getCodigo());
        assertThat(movs1).isNotEmpty();
        assertThat(movs1.getFirst().getDescricao()).contains(Mensagens.HIST_MAPA_VALIDACAO_ACEITA);

        // Check subprocesso 2
        Subprocesso s2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
        assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s2.getCodigo())).isNotEmpty();

        List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll().stream()
                .filter(n -> n.getTipoNotificacao() == TipoNotificacao.MAPA_VALIDACAO_ACEITA)
                .filter(n -> n.getUsuarioDestinoTitulo() == null)
                .toList();
        assertThat(notificacoes).hasSize(3);
        assertThat(notificacoes).anySatisfy(notificacao -> {
            assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo("SEDESENV");
            assertThat(notificacao.getAssunto()).isEqualTo("SGC: Validação do mapa de competências da SEDESENV submetida para análise");
            assertThat(notificacao.getCorpoHtml()).containsIgnoringWhitespaces("foi aceita e submetida para análise pela unidade superior");
        });
        assertThat(notificacoes).anySatisfy(notificacao -> {
            assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo("SEDIA");
            assertThat(notificacao.getAssunto()).isEqualTo("SGC: Validação do mapa de competências da SEDIA submetida para análise");
        });
        assertThat(notificacoes).anySatisfy(notificacao -> {
            assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo("COSIS");
            assertThat(notificacao.getAssunto()).isEqualTo("SGC: Validação de mapas de competências submetida para análise");
            assertThat(notificacao.getCorpoHtml()).contains("SEDESENV, SEDIA");
        });

        aguardarEmail(3);
        assertThat(algumEmailPara("sedesenv@tre-pe.jus.br")).isTrue();
        assertThat(algumEmailPara("sedia@tre-pe.jus.br")).isTrue();
        assertThat(algumEmailPara("cosis@tre-pe.jus.br")).isTrue();
    }

    @Test
    @DisplayName("Gestor da secretaria superior deve visualizar alerta no painel após aceite de validação")
    void gestorSecretariaSuperiorDeveVisualizarAlertaNoPainel() throws Exception {
        Unidade secaoDesenvolvimento = unidadeRepo.findById(codSecaoDesenvolvimento).orElseThrow();
        Unidade coordenadoriaSistemas = unidadeRepo.findById(codCoordenadoria).orElseThrow();
        Unidade secretariaInformatica = unidadeRepo.findById(codSecretaria).orElseThrow();

        Processo processoPainel = ProcessoFixture.processoPadrao();
        processoPainel.setCodigo(null);
        processoPainel.setTipo(TipoProcesso.MAPEAMENTO);
        processoPainel.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoPainel.setDescricao("Processo painel CDU-25");
        processoPainel = processoRepo.saveAndFlush(processoPainel);

        Subprocesso subprocessoPainel = SubprocessoFixture.subprocessoPadrao(processoPainel, secaoDesenvolvimento);
        subprocessoPainel.setCodigo(null);
        subprocessoPainel.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocessoPainel = subprocessoRepo.saveAndFlush(subprocessoPainel);

        Usuario usuarioCoordenadoria = usuarioRepo.findById("666666666666").orElseThrow();
        usuarioCoordenadoria.setPerfilAtivo(Perfil.GESTOR);
        usuarioCoordenadoria.setUnidadeAtivaCodigo(coordenadoriaSistemas.getCodigo());
        usuarioCoordenadoria.setAuthorities(Set.of(new SimpleGrantedAuthority("ROLE_GESTOR")));

        Movimentacao movimentacaoInicial = Movimentacao.builder()
                .subprocesso(subprocessoPainel)
                .unidadeOrigem(secaoDesenvolvimento)
                .unidadeDestino(coordenadoriaSistemas)
                .descricao(Mensagens.HIST_MAPA_VALIDADO)
                .dataHora(LocalDateTime.now())
                .usuario(usuarioCoordenadoria)
                .build();
        movimentacaoRepo.saveAndFlush(movimentacaoInicial);

        mockMvc.perform(post("/api/subprocessos/{codigo}/aceitar-validacao", subprocessoPainel.getCodigo())
                        .with(user(usuarioCoordenadoria))
                        .with(csrf()))
                .andExpect(status().isOk());

        Usuario usuarioSecretaria = usuarioRepo.findById("999999999999").orElseThrow();
        usuarioSecretaria.setPerfilAtivo(Perfil.GESTOR);
        usuarioSecretaria.setUnidadeAtivaCodigo(secretariaInformatica.getCodigo());
        usuarioSecretaria.setAuthorities(Set.of(new SimpleGrantedAuthority("ROLE_GESTOR")));

        mockMvc.perform(get("/api/painel/alertas")
                        .with(user(usuarioSecretaria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.descricao =~ /.*SEDESENV.*/)]").exists());
    }

    @Test
    @DisplayName("Deve aceitar validação em bloco no último nível e notificar a ADMIN")
    void aceitarValidacaoEmBlocoNoUltimoNivel_deveNotificarAdmin() throws Exception {
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
        processoFinal.setDescricao("Processo validação final CDU-25");
        processoFinal = processoRepo.save(processoFinal);

        Subprocesso subprocessoFinal = SubprocessoFixture.subprocessoPadrao(processoFinal, secao);
        subprocessoFinal.setCodigo(null);
        subprocessoFinal.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocessoFinal = subprocessoRepo.save(subprocessoFinal);

        Usuario usuarioGestorNivelFinal = usuarioRepo.findById("666666666666").orElseThrow();
        usuarioGestorNivelFinal.setPerfilAtivo(Perfil.GESTOR);
        usuarioGestorNivelFinal.setUnidadeAtivaCodigo(secretaria.getCodigo());
        usuarioGestorNivelFinal.setAuthorities(Set.of(Perfil.GESTOR.toGrantedAuthority()));
        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocessoFinal)
                .unidadeOrigem(secao)
                .unidadeDestino(coordenadoria)
                .descricao(Mensagens.HIST_MAPA_VALIDADO)
                .dataHora(LocalDateTime.now())
                .usuario(usuarioGestorNivelFinal)
                .build());
        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocessoFinal)
                .unidadeOrigem(coordenadoria)
                .unidadeDestino(secretaria)
                .descricao(Mensagens.HIST_MAPA_VALIDADO)
                .dataHora(LocalDateTime.now())
                .usuario(usuarioGestorNivelFinal)
                .build());

        entityManager.flush();
        entityManager.clear();

        ProcessarEmBlocoRequest request = ProcessarEmBlocoRequest.builder()
                .subprocessos(List.of(subprocessoFinal.getCodigo()))
                .build();

        mockMvc.perform(
                        post("/api/subprocessos/aceitar-validacao-bloco")
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

        assertThat(notificacaoEmailRepo.findAll().stream()
                .filter(n -> n.getTipoNotificacao() == TipoNotificacao.MAPA_VALIDACAO_ACEITA)
                .toList())
                .anySatisfy(notificacao -> {
                    assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo("ADMIN");
                    assertThat(notificacao.getDestinatario()).isEqualTo("admin@tre-pe.jus.br");
                    assertThat(notificacao.getAssunto()).isEqualTo("SGC: Validação de mapas de competências submetida para análise");
                    assertThat(notificacao.getCorpoHtml()).contains("SEDESENV");
                });
        assertThat(notificacaoEmailRepo.findAll().stream()
                .filter(n -> n.getTipoNotificacao() == TipoNotificacao.MAPA_VALIDACAO_ACEITA)
                .map(NotificacaoEmail::getUnidadeDestinoSigla)
                .toList())
                .doesNotContain("STIC");
    }
}
