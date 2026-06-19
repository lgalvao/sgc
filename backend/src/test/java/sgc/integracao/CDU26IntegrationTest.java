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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@DisplayName("CDU-26: Homologar validação de mapas em bloco")
class CDU26IntegrationTest extends BaseIntegrationTest {
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
    private Usuario admin;

    @BeforeEach
    void setUp() {
        Long idSuperior = 8000L;
        Long idUnidade1 = 8001L;
        Long idUnidade2 = 8002L;

        String sqlInsertUnidade = "INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlInsertUnidade, idSuperior, "Coordenação homolog valid", "COORD-HOM-VAL", "INTERMEDIARIA", "ATIVA", null, null);
        jdbcTemplate.update(sqlInsertUnidade, idUnidade1, "Unidade hom val 1", "UNID-HOM-VAL-1", "OPERACIONAL", "ATIVA", idSuperior, null);
        jdbcTemplate.update(sqlInsertUnidade, idUnidade2, "Unidade hom val 2", "UNID-HOM-VAL-2", "OPERACIONAL", "ATIVA", idSuperior, null);

        unidade1 = unidadeRepo.findById(idUnidade1).orElseThrow();
        unidade2 = unidadeRepo.findById(idUnidade2).orElseThrow();

        admin = usuarioRepo.findById("111111111111").orElseThrow();

        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo homologação validação CDU-26");
        processo = processoRepo.save(processo);

        // Subprocessos (Status deve ser Validado ou similar, pronto para homologação)
        // O CDU-26 geralmente segue o aceite da validação. Se o gestor aceitou, sobe para admin homologar.
        // O status "MAPEAMENTO_MAPA_VALIDADO" é um bom candidato se não houver um "VALIDACAO_ACEITA" intermediário.
        subprocesso1 = SubprocessoFixture.subprocessoPadrao(processo, unidade1);
        subprocesso1.setCodigo(null);
        subprocesso1.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocesso1 = subprocessoRepo.save(subprocesso1);

        subprocesso2 = SubprocessoFixture.subprocessoPadrao(processo, unidade2);
        subprocesso2.setCodigo(null);
        subprocesso2.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocesso2 = subprocessoRepo.save(subprocesso2);

        entityManager.flush();
        entityManager.clear();

        processoRepo.findById(processo.getCodigo()).orElseThrow();
        subprocesso1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        subprocesso2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
    }

    @Test
    @DisplayName("Deve homologar validação de mapas em bloco")
    @WithMockAdmin
    void homologarValidacaoEmBloco_deveHomologarSucesso() throws Exception {

        List<Long> subprocessosSelecionados = List.of(subprocesso1.getCodigo(), subprocesso2.getCodigo());

        // Garante que os subprocessos estejam na unidade do ADMIN (1)
        Unidade adminUnit = unidadeRepo.findById(1L).orElseThrow();
        Movimentacao movAdmin1 = Movimentacao.builder()
                .subprocesso(subprocesso1)
                .unidadeOrigem(unidade1)
                .unidadeDestino(adminUnit)
                .descricao("Enviado para Admin")
                .dataHora(LocalDateTime.now())
                .usuario(admin)
                .build();
        movimentacaoRepo.save(movAdmin1);

        Movimentacao movAdmin2 = Movimentacao.builder()
                .subprocesso(subprocesso2)
                .unidadeOrigem(unidade2)
                .unidadeDestino(adminUnit)
                .descricao("Enviado para Admin")
                .dataHora(LocalDateTime.now())
                .usuario(admin)
                .build();
        movimentacaoRepo.save(movAdmin2);

        entityManager.flush();
        entityManager.clear();

        ProcessarEmBlocoRequest request = ProcessarEmBlocoRequest.builder()
                .subprocessos(subprocessosSelecionados)
                .build();

        mockMvc.perform(
                        post("/api/subprocessos/homologar-validacao-bloco")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        // Check subprocesso 1
        Subprocesso s1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        assertThat(s1.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);

        List<Movimentacao> movs1 = movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(s1.getCodigo());
        assertThat(movs1).isNotEmpty();
        assertThat(movs1.getFirst().getDescricao()).isEqualTo(Mensagens.HIST_MAPA_HOMOLOGADO);
        assertThat(movs1.getFirst().getUnidadeOrigem().getSigla()).isEqualTo("ADMIN");
        assertThat(movs1.getFirst().getUnidadeDestino().getSigla()).isEqualTo("ADMIN");

        // Check subprocesso 2
        Subprocesso s2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
        assertThat(s2.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);

        List<Movimentacao> movs2 = movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(s2.getCodigo());
        assertThat(movs2).isNotEmpty();
        assertThat(movs2.getFirst().getDescricao()).isEqualTo(Mensagens.HIST_MAPA_HOMOLOGADO);
        assertThat(movs2.getFirst().getUnidadeOrigem().getSigla()).isEqualTo("ADMIN");
        assertThat(movs2.getFirst().getUnidadeDestino().getSigla()).isEqualTo("ADMIN");

        List<Alerta> alertas = alertaRepo.findByProcessoCodigo(s1.getProcesso().getCodigo());
        assertThat(alertas).anyMatch(alerta ->
                alerta.getUnidadeDestino() != null
                        && Objects.equals(alerta.getUnidadeDestino().getCodigo(), unidade1.getCodigo())
                        && Mensagens.ALERTA_MAPA_HOMOLOGADO.formatted(unidade1.getSigla()).equals(alerta.getDescricao())
                        && "ADMIN".equals(alerta.getUnidadeOrigem().getSigla())
        );
        assertThat(alertas).anyMatch(alerta ->
                alerta.getUnidadeDestino() != null
                        && Objects.equals(alerta.getUnidadeDestino().getCodigo(), unidade2.getCodigo())
                        && Mensagens.ALERTA_MAPA_HOMOLOGADO.formatted(unidade2.getSigla()).equals(alerta.getDescricao())
                        && "ADMIN".equals(alerta.getUnidadeOrigem().getSigla())
        );

        List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll().stream()
                .filter(n -> n.getTipoNotificacao() == TipoNotificacao.MAPA_HOMOLOGADO)
                .toList();
        assertThat(notificacoes).hasSize(2);
        assertThat(notificacoes).anySatisfy(notificacao -> {
            assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo(unidade1.getSigla());
            assertThat(notificacao.getDestinatario()).isEqualTo("unid-hom-val-1@tre-pe.jus.br");
            assertThat(notificacao.getAssunto()).isEqualTo("SGC: Mapa de competências homologado");
            assertThat(notificacao.getCorpoHtml())
                    .contains("Prezado(a) responsável pela <strong>%s</strong>".formatted(unidade1.getSigla()))
                    .contains("O mapa de competências da sua unidade foi homologado no processo")
                    .contains("Processo homologação validação CDU-26")
                    .contains("Acompanhe o processo no Sistema de Gestão de Competências");
            assertThat(notificacao.getSituacao()).isIn(SituacaoNotificacao.PENDENTE, SituacaoNotificacao.ENVIADO);
        });
        assertThat(notificacoes).anySatisfy(notificacao -> {
            assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo(unidade2.getSigla());
            assertThat(notificacao.getDestinatario()).isEqualTo("unid-hom-val-2@tre-pe.jus.br");
            assertThat(notificacao.getAssunto()).isEqualTo("SGC: Mapa de competências homologado");
            assertThat(notificacao.getCorpoHtml())
                    .contains("Prezado(a) responsável pela <strong>%s</strong>".formatted(unidade2.getSigla()))
                    .contains("Processo homologação validação CDU-26");
            assertThat(notificacao.getSituacao()).isIn(SituacaoNotificacao.PENDENTE, SituacaoNotificacao.ENVIADO);
        });

        aguardarEmail(2);
        assertThat(algumEmailPara("unid-hom-val-1@tre-pe.jus.br")).isTrue();
        assertThat(algumEmailPara("unid-hom-val-2@tre-pe.jus.br")).isTrue();
        assertThat(algumEmailComAssunto("[SGC-TEST] Mapa de competências homologado")).isTrue();
        assertThat(algumEmailContem("O mapa de competências da sua unidade foi homologado no processo")).isTrue();
    }
}
