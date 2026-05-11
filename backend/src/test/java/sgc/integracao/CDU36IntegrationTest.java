package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-36: Gerar relatório de mapas")
class CDU36IntegrationTest extends BaseIntegrationTest {
    private static final String API_REL_MAPAS = "/api/relatorios/mapas/exportar";

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;

    private Unidade unidade;

    @BeforeEach
    void setUp() {
        // Obter unidade
        unidade = unidadeRepo.findById(1L).orElseThrow();

        // Criar subprocesso
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo CDU-36");
        processo = processoRepo.save(processo);

        Subprocesso subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso = subprocessoRepo.save(subprocesso);

        // Criar mapa (opcional, mas ajuda a tornar o teste mais realista)
        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapa = mapaRepo.save(mapa);
        unidadeMapaRepo.save(UnidadeMapa.builder()
                .unidadeCodigo(unidade.getCodigo())
                .mapaVigente(mapa)
                .build());

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Deve gerar relatório de mapas em PDF quando ADMIN")
    @WithMockAdmin
    void gerarRelatorioMapas_comoAdmin_sucesso() throws Exception {
        mockMvc.perform(get(API_REL_MAPAS)
                        .param("codUnidade", unidade.getCodigo().toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @DisplayName("Deve gerar relatório de mapas para múltiplas unidades quando ADMIN")
    @WithMockAdmin
    void gerarRelatorioMapas_multiplasUnidades_sucesso() throws Exception {
        mockMvc.perform(get(API_REL_MAPAS)
                        .param("codUnidade", unidade.getCodigo().toString())
                        .param("codUnidade", "999999")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @DisplayName("Deve permitir gerar relatório de mapas para a própria hierarquia quando GESTOR")
    @WithMockGestor
    void gerarRelatorioMapas_comoGestorNaHierarquia_sucesso() throws Exception {
        mockMvc.perform(get(API_REL_MAPAS)
                        .param("codUnidade", "101")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @DisplayName("Deve negar relatório de mapas fora da subárvore do GESTOR apenas como defesa de servidor")
    @WithMockGestor
    void gerarRelatorioMapas_foraDaHierarquiaDoGestor_proibido() throws Exception {
        mockMvc.perform(get(API_REL_MAPAS)
                        .param("codUnidade", unidade.getCodigo().toString())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar erro quando não houver parâmetro de unidade")
    @WithMockAdmin
    void gerarRelatorioMapas_semUnidade_erro() throws Exception {
        mockMvc.perform(get(API_REL_MAPAS).with(csrf()))
                .andExpect(status().is4xxClientError());
    }
}
