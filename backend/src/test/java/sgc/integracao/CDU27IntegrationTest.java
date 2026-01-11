package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;
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
import sgc.subprocesso.dto.AlterarDataLimiteRequest;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, sgc.integracao.mocks.TestThymeleafConfig.class})
@Transactional
@DisplayName("CDU-27: Alterar data limite de subprocesso")
class CDU27IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private EntityManager entityManager;

    private Unidade unidade;
    private Processo processo;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        // Obter Unidade
        unidade = unidadeRepo.findById(1L).orElseThrow();

        // Criar Processo
        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo CDU-27");
        processo = processoRepo.save(processo);

        // Criar Subprocesso
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso = subprocessoRepo.save(subprocesso);

        entityManager.flush();
        entityManager.clear();

        // Reload to attach
        subprocesso = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
    }

    @Test
    @DisplayName("Deve alterar a data limite e gerar alerta quando ADMIN")
    @WithMockAdmin
    void alterarDataLimite_comoAdmin_sucesso() throws Exception {
        // Given
        LocalDate novaData = LocalDate.now().plusDays(20);
        AlterarDataLimiteRequest request = new AlterarDataLimiteRequest();
        request.setNovaDataLimite(novaData);

        // When
        mockMvc.perform(
                post("/api/subprocessos/{codigo}/data-limite", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        entityManager.flush();
        entityManager.clear();

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(atualizado.getDataLimiteEtapa1().toLocalDate()).isEqualTo(novaData);

        // Verify Alerta
        boolean alertaExiste = alertaRepo.findAll().stream()
                .anyMatch(a -> a.getUnidadeDestino() != null &&
                        a.getUnidadeDestino().getCodigo().equals(unidade.getCodigo()) &&
                        a.getDescricao().contains("Data limite"));
        assertThat(alertaExiste).isTrue();
    }

    @Test
    @DisplayName("Não deve permitir alterar data limite se não for ADMIN")
    @org.springframework.security.test.context.support.WithMockUser(roles = "GESTOR")
    void alterarDataLimite_semPermissao_proibido() throws Exception {
        // Given
        LocalDate novaData = LocalDate.now().plusDays(20);
        AlterarDataLimiteRequest request = new AlterarDataLimiteRequest();
        request.setNovaDataLimite(novaData);

        // When/Then
        mockMvc.perform(
                post("/api/subprocessos/{codigo}/data-limite", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
