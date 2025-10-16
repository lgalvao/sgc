package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.processo.SituacaoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockAdmin
@Transactional
@DisplayName("CDU-06: Detalhar processo")
public class CDU06IntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    private Processo processo;

    @BeforeEach
    void setUp() {
        Unidade unidade = new Unidade();
        unidade.setNome("Unidade de Teste");
        unidade.setSigla("UT");
        unidadeRepo.save(unidade);

        processo = new Processo();
        processo.setDescricao("Processo de Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataLimite(LocalDateTime.now().plusDays(10));
        processoRepo.save(processo);

        Subprocesso subprocesso = new Subprocesso(processo, unidade, null, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, processo.getDataLimite());
        subprocessoRepo.save(subprocesso);
    }

    @Test
    void testDetalharProcesso_sucesso() throws Exception {
        mockMvc.perform(get("/api/processos/{id}/detalhes?perfil=ADMIN", processo.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(processo.getCodigo()))
                .andExpect(jsonPath("$.descricao").value("Processo de Teste"))
                .andExpect(jsonPath("$.tipo").value(TipoProcesso.MAPEAMENTO.name()))
                .andExpect(jsonPath("$.situacao").value(SituacaoProcesso.EM_ANDAMENTO.name()))
                .andExpect(jsonPath("$.unidades[0].nome").value("Unidade de Teste"))
                .andExpect(jsonPath("$.unidades[0].situacaoSubprocesso").value(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO.name()));
    }

    @Test
    void testDetalharProcesso_naoEncontrado_falha() throws Exception {
        mockMvc.perform(get("/api/processos/{id}/detalhes?perfil=ADMIN", 999L)) // ID que não existe
                .andExpect(status().isNotFound());
    }
}