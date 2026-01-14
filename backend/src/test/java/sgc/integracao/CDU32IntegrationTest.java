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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.alerta.model.AlertaRepo;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.ReabrirProcessoRequest;
import sgc.subprocesso.model.*;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, sgc.integracao.mocks.TestThymeleafConfig.class})
@Transactional
@DisplayName("CDU-32: Reabrir cadastro")
class CDU32IntegrationTest extends BaseIntegrationTest {
    private static final String API_REABRIR_CADASTRO = "/api/subprocessos/{codigo}/reabrir-cadastro";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private SubprocessoMovimentacaoRepo movimentacaoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private EntityManager entityManager;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        // Obter Unidade
        Unidade unidade = unidadeRepo.findById(1L).orElseThrow();

        // Criar Processo
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo CDU-32");
        processo = processoRepo.save(processo);

        // Criar Subprocesso em estado que permite reabertura (MAPEAMENTO_CADASTRO_HOMOLOGADO)
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso = subprocessoRepo.save(subprocesso);

        entityManager.flush();
        entityManager.clear();

        // Reload to attach
        subprocesso = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
    }

    @Test
    @DisplayName("Deve reabrir cadastro com justificativa válida quando ADMIN")
    @WithMockAdmin
    void reabrirCadastro_comoAdmin_sucesso() throws Exception {
        // Given
        ReabrirProcessoRequest request = new ReabrirProcessoRequest("Necessário ajustar informações do cadastro");

        // When
        mockMvc.perform(
                post(API_REABRIR_CADASTRO, subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        entityManager.flush();
        entityManager.clear();

        Subprocesso reaberto = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(reaberto.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        // Verificar se foi criada uma movimentação
        List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
        assertThat(movimentacoes).isNotEmpty();
        boolean movimentacaoExiste = movimentacoes.stream()
                .anyMatch(m -> m.getDescricao() != null && 
                        m.getDescricao().contains("Reabertura de cadastro"));
        assertThat(movimentacaoExiste).isTrue();

        // Verificar se foi criado um alerta
        boolean alertaExiste = alertaRepo.findAll().stream()
                .anyMatch(a -> a.getUnidadeDestino() != null &&
                        a.getUnidadeDestino().getCodigo().equals(reaberto.getUnidade().getCodigo()) &&
                        a.getDescricao().contains("reaberto"));
        assertThat(alertaExiste).isTrue();
    }

    @Test
    @DisplayName("Não deve permitir reabrir cadastro sem ser ADMIN")
    @org.springframework.security.test.context.support.WithMockUser(roles = "GESTOR")
    void reabrirCadastro_semPermissao_proibido() throws Exception {
        // Given
        ReabrirProcessoRequest request = new ReabrirProcessoRequest("Tentativa sem permissão");

        // When/Then
        mockMvc.perform(
                post(API_REABRIR_CADASTRO, subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Não deve permitir reabrir cadastro sem justificativa")
    @WithMockAdmin
    void reabrirCadastro_semJustificativa_erro() throws Exception {
        // Given
        ReabrirProcessoRequest request = new ReabrirProcessoRequest("");

        // When/Then
        mockMvc.perform(
                post(API_REABRIR_CADASTRO, subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
