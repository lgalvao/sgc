package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.*;
import sgc.comum.Mensagens;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.ProcessarEmBlocoRequest;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        assertThat(alertas).noneMatch(alerta ->
                alerta.getUnidadeDestino() != null
                        && (Objects.equals(alerta.getUnidadeDestino().getCodigo(), unidade1.getCodigo())
                        || Objects.equals(alerta.getUnidadeDestino().getCodigo(), unidade2.getCodigo()))
                        && Mensagens.HIST_CADASTRO_HOMOLOGADO.equals(alerta.getDescricao())
        );

        List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll().stream()
                .filter(n -> n.getTipoNotificacao() == TipoNotificacao.CADASTRO_HOMOLOGADO)
                .toList();
        assertThat(notificacoes).isEmpty();
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
