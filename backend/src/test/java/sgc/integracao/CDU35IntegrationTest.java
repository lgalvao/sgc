package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.dto.UnidadeResponsavelDto;

@Tag("integration")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestThymeleafConfig.class})
@Transactional
@DisplayName("CDU-35: Gerar relatório de andamento")
class CDU35IntegrationTest extends BaseIntegrationTest {
    private static final String API_REL_ANDAMENTO = "/api/relatorios/andamento/{codProcesso}";

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private UnidadeFacade unidadeService;

    private Processo processo;

    @BeforeEach
    void setUp() {
        // Obter Unidade
        Unidade unidade = unidadeRepo.findById(1L).orElseThrow();

        when(unidadeService.buscarResponsavelUnidade(anyLong()))
                .thenReturn(UnidadeResponsavelDto.builder()
                        .titularNome("Responsável Teste")
                        .build());

        // Criar Processo
        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo CDU-35");
        processo = processoRepo.save(processo);

        // Criar Subprocesso
        Subprocesso subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocessoRepo.save(subprocesso);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Deve gerar relatório de andamento em PDF quando ADMIN")
    @WithMockAdmin
    void gerarRelatorioAndamento_comoAdmin_sucesso() throws Exception {
        // When/Then
        mockMvc.perform(
                        get(API_REL_ANDAMENTO, processo.getCodigo())
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @DisplayName("Não deve permitir gerar relatório de andamento sem ser ADMIN")
    @WithMockUser(roles = "GESTOR")
    void gerarRelatorioAndamento_semPermissao_proibido() throws Exception {
        // When/Then
        mockMvc.perform(
                        get(API_REL_ANDAMENTO, processo.getCodigo())
                                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar erro ao gerar relatório de processo inexistente")
    @WithMockAdmin
    void gerarRelatorioAndamento_processoInexistente_erro() throws Exception {
        // When/Then
        mockMvc.perform(get(API_REL_ANDAMENTO, 99999L).with(csrf()))
                .andExpect(status().is4xxClientError());
    }
}
