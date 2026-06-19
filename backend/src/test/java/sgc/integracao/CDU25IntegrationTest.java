package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.NotificacaoEmail;
import sgc.alerta.model.NotificacaoEmailRepo;
import sgc.alerta.model.TipoNotificacao;
import sgc.comum.Mensagens;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.integracao.mocks.WithMockGestor;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.ProcessarEmBlocoRequest;
import sgc.subprocesso.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@DisplayName("CDU-25: Aceitar validação de mapas em bloco")
class CDU25IntegrationTest extends BaseIntegrationTest {
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
        // Use existing 3-level hierarchy from data.sql:
        // Unit 2 (STIC - INTEROPERACIONAL) - top level
        // Unit 6 (COSIS - INTERMEDIARIA) - subordinate to 2, user '666666666666' is GESTOR
        // Unit 8 (SEDESENV - OPERACIONAL) - subordinate to 6
        // Unit 9 (SEDIA - OPERACIONAL) - subordinate to 6

        Unidade unidade1 = unidadeRepo.findById(8L)
                .orElseThrow(() -> new RuntimeException("Unit 8 not found in data.sql"));
        Unidade unidade2 = unidadeRepo.findById(9L)
                .orElseThrow(() -> new RuntimeException("Unit 9 not found in data.sql"));
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

        // Subprocessos devem estar na unidade superior (6) para o Gestor aceitar
        Unidade unidadeSuperior = unidadeRepo.findById(6L).orElseThrow();
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
            assertThat(notificacao.getCorpoHtml()).contains("foi aceita e submetida para análise pela unidade superior imediata");
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
        Unidade secaoDesenvolvimento = unidadeRepo.findById(8L).orElseThrow();
        Unidade coordenadoriaSistemas = unidadeRepo.findById(6L).orElseThrow();
        Unidade secretariaInformatica = unidadeRepo.findById(2L).orElseThrow();

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
}
