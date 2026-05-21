package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.*;
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
@DisplayName("CDU-23: Homologar cadastros em bloco")
class CDU23IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    private Unidade unidade1;
    private Unidade unidade2;
    private Subprocesso subprocesso1;
    private Subprocesso subprocesso2;
    private Processo processo;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        // Criar unidades
        Long idSuperior = 5000L;
        Long idUnidade1 = 5001L;
        Long idUnidade2 = 5002L;

        String sqlInsertUnidade = "INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular) VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sqlInsertUnidade, idSuperior, "Coordenação homologação", "COORD-HOMOLOG", "INTERMEDIARIA", "ATIVA", null, null);
        jdbcTemplate.update(sqlInsertUnidade, idUnidade1, "Unidade homolog 1", "UNID-HOMOLOG-1", "OPERACIONAL", "ATIVA", idSuperior, null);
        jdbcTemplate.update(sqlInsertUnidade, idUnidade2, "Unidade homolog 2", "UNID-HOMOLOG-2", "OPERACIONAL", "ATIVA", idSuperior, null);

        unidade1 = unidadeRepo.findById(idUnidade1).orElseThrow();
        unidade2 = unidadeRepo.findById(idUnidade2).orElseThrow();

        admin = usuarioRepo.findById("111111111111").orElseThrow();

        // Criar processo
        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo homologação CDU-23");
        processo = processoRepo.save(processo);

        // Criar subprocessos
        subprocesso1 = SubprocessoFixture.subprocessoPadrao(processo, unidade1);
        subprocesso1.setCodigo(null);
        subprocesso1.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocesso1.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso1 = subprocessoRepo.save(subprocesso1);
        registrarMovimentacaoInicial(subprocesso1);

        subprocesso2 = SubprocessoFixture.subprocessoPadrao(processo, unidade2);
        subprocesso2.setCodigo(null);
        subprocesso2.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocesso2.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso2 = subprocessoRepo.save(subprocesso2);
        registrarMovimentacaoInicial(subprocesso2);

        entityManager.flush();
        entityManager.clear();

        processoRepo.findById(processo.getCodigo()).orElseThrow();
        subprocesso1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        subprocesso2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
    }

    @Test
    @DisplayName("Deve homologar cadastro de múltiplas unidades em bloco")
    @WithMockAdmin
    void homologarCadastroEmBloco_deveHomologarTodasSelecionadas() throws Exception {

        List<Long> subprocessosSelecionados = List.of(subprocesso1.getCodigo(), subprocesso2.getCodigo());

        // Para homologar, os subprocessos devem estar na unidade do Admin (1)
        Unidade adminUnit = unidadeRepo.findById(1L).orElseThrow();

        Movimentacao m1 = Movimentacao.builder()
                .subprocesso(subprocesso1)
                .unidadeOrigem(unidade1)
                .unidadeDestino(adminUnit)
                .descricao("Enviado para Admin")
                .dataHora(LocalDateTime.now())
                .usuario(admin)
                .build();
        movimentacaoRepo.save(m1);

        Movimentacao m2 = Movimentacao.builder()
                .subprocesso(subprocesso2)
                .unidadeOrigem(unidade2)
                .unidadeDestino(adminUnit)
                .descricao("Enviado para Admin")
                .dataHora(LocalDateTime.now())
                .usuario(admin)
                .build();
        movimentacaoRepo.save(m2);

        ProcessarEmBlocoRequest request = ProcessarEmBlocoRequest.builder()
                .subprocessos(subprocessosSelecionados)
                .build();

        mockMvc.perform(
                        post("/api/subprocessos/homologar-cadastro-bloco")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        // Verify subprocesso 1
        Subprocesso s1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        assertThat(s1.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

        List<Movimentacao> movs1 = movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(s1.getCodigo());
        assertThat(movs1).isNotEmpty();
        assertThat(movs1.getFirst().getDescricao()).isEqualTo(Mensagens.HIST_CADASTRO_HOMOLOGADO);
        assertThat(movs1.getFirst().getUnidadeOrigem().getSigla()).isEqualTo("ADMIN");
        assertThat(movs1.getFirst().getUnidadeDestino().getSigla()).isEqualTo("ADMIN");

        // Verify subprocesso 2
        Subprocesso s2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
        assertThat(s2.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

        List<Movimentacao> movs2 = movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(s2.getCodigo());
        assertThat(movs2).isNotEmpty();
        assertThat(movs2.getFirst().getDescricao()).isEqualTo(Mensagens.HIST_CADASTRO_HOMOLOGADO);
        assertThat(movs2.getFirst().getUnidadeOrigem().getSigla()).isEqualTo("ADMIN");
        assertThat(movs2.getFirst().getUnidadeDestino().getSigla()).isEqualTo("ADMIN");

        List<Alerta> alertas = alertaRepo.findByProcessoCodigo(processo.getCodigo());
        assertThat(alertas).anyMatch(alerta ->
                alerta.getUnidadeDestino() != null
                        && Objects.equals(alerta.getUnidadeDestino().getCodigo(), unidade1.getCodigo())
                        && Mensagens.ALERTA_CADASTRO_HOMOLOGADO.formatted(unidade1.getSigla()).equals(alerta.getDescricao())
                        && "ADMIN".equals(alerta.getUnidadeOrigem().getSigla())
        );
        assertThat(alertas).anyMatch(alerta ->
                alerta.getUnidadeDestino() != null
                        && Objects.equals(alerta.getUnidadeDestino().getCodigo(), unidade2.getCodigo())
                        && Mensagens.ALERTA_CADASTRO_HOMOLOGADO.formatted(unidade2.getSigla()).equals(alerta.getDescricao())
                        && "ADMIN".equals(alerta.getUnidadeOrigem().getSigla())
        );

        List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll().stream()
                .filter(n -> n.getTipoNotificacao() == TipoNotificacao.CADASTRO_HOMOLOGADO)
                .toList();
        assertThat(notificacoes).hasSize(2);
        assertThat(notificacoes).anySatisfy(notificacao -> {
            assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo(unidade1.getSigla());
            assertThat(notificacao.getDestinatario()).isEqualTo("unid-homolog-1@tre-pe.jus.br");
            assertThat(notificacao.getAssunto()).isEqualTo("SGC: Cadastro de atividades homologado");
            assertThat(notificacao.getCorpoHtml())
                    .contains("Prezado(a) responsável pela <strong>%s</strong>".formatted(unidade1.getSigla()))
                    .contains("O cadastro de atividades e conhecimentos da sua unidade foi homologado no processo")
                    .contains(processo.getDescricao())
                    .contains("Acompanhe o processo no Sistema de Gestão de Competências");
            assertThat(notificacao.getSituacao()).isIn(SituacaoNotificacao.PENDENTE, SituacaoNotificacao.ENVIADO);
        });
        assertThat(notificacoes).anySatisfy(notificacao -> {
            assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo(unidade2.getSigla());
            assertThat(notificacao.getDestinatario()).isEqualTo("unid-homolog-2@tre-pe.jus.br");
            assertThat(notificacao.getAssunto()).isEqualTo("SGC: Cadastro de atividades homologado");
            assertThat(notificacao.getCorpoHtml())
                    .contains("Prezado(a) responsável pela <strong>%s</strong>".formatted(unidade2.getSigla()))
                    .contains(processo.getDescricao());
            assertThat(notificacao.getSituacao()).isIn(SituacaoNotificacao.PENDENTE, SituacaoNotificacao.ENVIADO);
        });

        aguardarEmail(2);
        assertThat(algumEmailPara("unid-homolog-1@tre-pe.jus.br")).isTrue();
        assertThat(algumEmailPara("unid-homolog-2@tre-pe.jus.br")).isTrue();
        assertThat(algumEmailComAssunto("[SGC-TEST] Cadastro de atividades homologado")).isTrue();
        assertThat(algumEmailContem("foi homologado no processo")).isTrue();
    }

    @Test
    @DisplayName("ADMIN não deve ver subprocessos devolvidos como elegíveis para homologação em bloco")
    @WithMockAdmin
    void contextoCompleto_naoDeveListarSubprocessosDevolvidosComoElegiveis() throws Exception {
        mockMvc.perform(get("/api/processos/{codigo}/contexto-completo", processo.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elegiveis").isEmpty());
    }
}
