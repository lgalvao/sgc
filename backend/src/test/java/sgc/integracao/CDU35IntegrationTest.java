package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockGestor;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.dto.UnidadeResponsavelDto;
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

@Tag("integration")
@Transactional
@DisplayName("CDU-35: Gerar relatório de andamento")
class CDU35IntegrationTest extends BaseIntegrationTest {
    private static final String API_REL_ANDAMENTO = "/api/relatorios/andamento/{codProcesso}";

    @MockitoBean
    private OrganizacaoFacade facade;

    private Processo processo;

    @BeforeEach
    void setUp() {
        Unidade unidadeRaiz = unidadeRepo.findById(1L).orElseThrow();

        when(facade.buscarResponsavelUnidade(anyLong())).thenReturn(UnidadeResponsavelDto.builder()
                .titularNome("Responsável Teste")
                .build());

        processo = ProcessoFixture.novoProcesso()
                .setTipo(TipoProcesso.MAPEAMENTO)
                .setSituacao(SituacaoProcesso.EM_ANDAMENTO)
                .setDescricao("Processo CDU-35");

        processo = processoRepo.save(processo);

        Subprocesso sp = SubprocessoFixture.novoSubprocesso(processo, unidadeRaiz)
                .setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));

        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.saveAndFlush(sp);
    }

    @Test
    @DisplayName("Deve gerar relatório de andamento em PDF quando ADMIN")
    @WithMockAdmin
    void gerarRelatorioAndamento_comoAdmin_sucesso() throws Exception {
        mockMvc.perform(get(API_REL_ANDAMENTO, processo.getCodigo()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @DisplayName("Não deve permitir gerar relatório de andamento sem ser ADMIN")
    @WithMockGestor
    void gerarRelatorioAndamento_semPermissao_proibido() throws Exception {
        mockMvc.perform(get(API_REL_ANDAMENTO, processo.getCodigo()).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar erro ao gerar relatório de processo inexistente")
    @WithMockAdmin
    void gerarRelatorioAndamento_processoInexistente_erro() throws Exception {
        mockMvc.perform(get(API_REL_ANDAMENTO, 99999L).with(csrf()))
                .andExpect(status().is4xxClientError());
    }
}
