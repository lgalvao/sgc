package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.ReabrirProcessoRequest;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestThymeleafConfig.class})
@Transactional
@DisplayName("CDU-33: Reabrir revisão de cadastro")
class CDU33IntegrationTest extends BaseIntegrationTest {
    private static final String API_REABRIR_REVISAO = "/api/subprocessos/{codigo}/reabrir-revisao-cadastro";

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private EntityManager entityManager;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        // Garantir que SEDOC existe
        if (unidadeRepo.findBySigla("SEDOC").isEmpty()) {
            Unidade sedoc = new Unidade();
            sedoc.setSigla("SEDOC");
            sedoc.setNome("Secretaria de Documentação");
            sedoc.setSituacao(SituacaoUnidade.ATIVA);
            sedoc.setTipo(TipoUnidade.OPERACIONAL);
            unidadeRepo.save(sedoc);
        }

        // Obter Unidade
        Unidade unidade = unidadeRepo.findById(1L).orElseGet(() -> {
            Unidade u = new Unidade();
            u.setCodigo(1L);
            u.setSigla("TESTE");
            u.setNome("Unidade Teste");
            u.setSituacao(SituacaoUnidade.ATIVA);
            u.setTipo(TipoUnidade.OPERACIONAL);
            return unidadeRepo.save(u);
        });

        // Criar Processo de REVISAO
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.REVISAO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo CDU-33");
        processo = processoRepo.save(processo);

        // Criar Subprocesso em estado que permite reabertura de revisão
        // (REVISAO_CADASTRO_HOMOLOGADA)
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso = subprocessoRepo.save(subprocesso);

        entityManager.flush();
        entityManager.clear();

        // Reload to attach
        subprocesso = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
    }

    @Test
    @DisplayName("Deve reabrir revisão de cadastro com justificativa válida quando ADMIN")
    @WithMockAdmin
    void reabrirRevisaoCadastro_comoAdmin_sucesso() throws Exception {
        // Given
        ReabrirProcessoRequest request = new ReabrirProcessoRequest(
                "Necessário corrigir erros identificados na revisão");

        // When
        mockMvc.perform(
                        post(API_REABRIR_REVISAO, subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        entityManager.flush();
        entityManager.clear();

        Subprocesso reaberto = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(reaberto.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

        // Verificar se foi criada uma movimentação
        List<Movimentacao> movimentacoes = movimentacaoRepo
                .findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
        assertThat(movimentacoes).isNotEmpty();
        boolean movimentacaoExiste = movimentacoes.stream()
                .anyMatch(m -> m.getDescricao() != null &&
                        m.getDescricao().contains("Reabertura de revisão de cadastro"));
        assertThat(movimentacaoExiste).isTrue();

        // Verificar se foi criado um alerta
        List<Alerta> alerts = alertaRepo.findAll();
        assertThat(alerts).isNotEmpty();
        boolean alertaExiste = alerts.stream()
                .anyMatch(a -> a.getUnidadeDestino() != null &&
                        a.getUnidadeDestino().getCodigo()
                                .equals(reaberto.getUnidade().getCodigo())
                        &&
                        a.getDescricao().contains("reaberta"));
        assertThat(alertaExiste).isTrue();
    }

    @Test
    @DisplayName("Não deve permitir reabrir revisão de cadastro sem ser ADMIN")
    @WithMockUser(roles = "GESTOR")
    void reabrirRevisaoCadastro_semPermissao_proibido() throws Exception {
        // Given
        ReabrirProcessoRequest request = new ReabrirProcessoRequest("Tentativa sem permissão");

        // When/Then
        mockMvc.perform(
                        post(API_REABRIR_REVISAO, subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Não deve permitir reabrir revisão sem justificativa")
    @WithMockAdmin
    void reabrirRevisaoCadastro_semJustificativa_erro() throws Exception {
        // Given
        ReabrirProcessoRequest request = new ReabrirProcessoRequest("");

        // When/Then
        mockMvc.perform(
                        post(API_REABRIR_REVISAO, subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
