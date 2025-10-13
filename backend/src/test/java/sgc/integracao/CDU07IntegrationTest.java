package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.comum.modelo.SituacaoProcesso;
import sgc.comum.modelo.SituacaoSubprocesso;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.processo.modelo.TipoProcesso;
import sgc.processo.modelo.Processo;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.processo.modelo.ProcessoRepo;
import sgc.subprocesso.SubprocessoService;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin", roles = {"ADMIN"})
@Import(TestSecurityConfig.class)
@Transactional
public class CDU07IntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private SubprocessoService subprocessoService;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        Unidade unidade = new Unidade();
        unidade.setNome("Unidade de Teste");
        unidade.setSigla("UT");
        unidadeRepo.save(unidade);

        Processo processo = new Processo();
        processo.setDescricao("Processo de Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataLimite(LocalDate.now().plusDays(10));
        processoRepo.save(processo);

        subprocesso = new Subprocesso(processo, unidade, null, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, processo.getDataLimite());
        subprocessoRepo.save(subprocesso);

        Movimentacao movimentacao = new Movimentacao(subprocesso, null, unidade, "Subprocesso iniciado");
        movimentacaoRepo.save(movimentacao);
    }

    @Test
    void testDetalharSubprocesso_sucesso() throws Exception {
        mockMvc.perform(get("/api/subprocessos/{id}?perfil=ADMIN", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidade.nome").value("Unidade de Teste"))
                .andExpect(jsonPath("$.situacao").value(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO.name()))
                .andExpect(jsonPath("$.localizacaoAtual").value("UT"))
                .andExpect(jsonPath("$.movimentacoes[0].descricao").value("Subprocesso iniciado"));
    }

    @Test
    void testDetalharSubprocesso_naoEncontrado_falha() {
        assertThrows(ErroDominioNaoEncontrado.class, () -> subprocessoService.obterDetalhes(999L, "ADMIN", null));
    }
}