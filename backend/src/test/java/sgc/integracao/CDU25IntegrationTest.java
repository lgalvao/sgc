package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.analise.model.Analise;
import sgc.analise.model.AnaliseRepo;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UsuarioFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockGestor;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.ProcessarEmBlocoRequest;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, sgc.integracao.mocks.TestThymeleafConfig.class})
@Transactional
@DisplayName("CDU-25: Aceitar validação de mapas em bloco")
class CDU25IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private SubprocessoMovimentacaoRepo movimentacaoRepo;

    @Autowired
    private AnaliseRepo analiseRepo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Unidade unidadeSuperior;
    private Unidade unidade1;
    private Unidade unidade2;
    private Processo processo;
    private Subprocesso subprocesso1;
    private Subprocesso subprocesso2;

    @BeforeEach
    void setUp() {
        Long idSuperior = 7000L;
        Long idUnidade1 = 7001L;
        Long idUnidade2 = 7002L;

        // É crucial que a unidade superior tenha unidade superior ou seja nula corretamente para o fluxo de "Aceite".
        // O fluxo de Aceitar Validação em SubprocessoMapaWorkflowService diz:
        // Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        // Unidade proximaUnidade = unidadeSuperior != null ? unidadeSuperior.getUnidadeSuperior() : null;
        // Se proximaUnidade == null, ele HOMOLOGA.
        // Se proximaUnidade != null, ele registra analise/movimentacao para proxima unidade.

        // No teste, criamos idSuperior como 7000L e unidade_superior_codigo = NULL.
        // Entao unidade1.superior = unidadeSuperior.
        // unidadeSuperior.superior = NULL.
        // Logo, proximaUnidade = NULL.
        // Então o sistema executa o bloco "homologarImplícito" que cria análise de aceite mas NAO cria movimentação de transição?
        // Vamos verificar o código em SubprocessoMapaWorkflowService.aceitarValidacao:

        /*
        if (proximaUnidade == null) {
            // ...
            analiseService.criarAnalise(...);
            sp.setSituacao(...HOMOLOGADO);
            subprocessoRepo.save(sp);
            // Sem transição registrada no código original para este caso.
        }
        */

        // Se não tem transição, não tem movimentação! Por isso o teste falha "Expecting actual not to be empty".

        // Para testar o fluxo "padrão" de ACEITE onde ele sobe para a próxima instância, precisamos de mais um nível hierárquico.

        Long idSuperSuperior = 6999L;
        String sqlInsertUnidade = "INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular) VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sqlInsertUnidade, idSuperSuperior, "Diretoria", "DIRETORIA", "INTERMEDIARIA", "ATIVA", null, null);
        jdbcTemplate.update(sqlInsertUnidade, idSuperior, "Coordenação Validação", "COORD-VALID", "INTERMEDIARIA", "ATIVA", idSuperSuperior, null);
        jdbcTemplate.update(sqlInsertUnidade, idUnidade1, "Unidade Valid 1", "UNID-VALID-1", "OPERACIONAL", "ATIVA", idSuperior, null);
        jdbcTemplate.update(sqlInsertUnidade, idUnidade2, "Unidade Valid 2", "UNID-VALID-2", "OPERACIONAL", "ATIVA", idSuperior, null);

        unidadeSuperior = unidadeRepo.findById(idSuperior).orElseThrow();
        unidade1 = unidadeRepo.findById(idUnidade1).orElseThrow();
        unidade2 = unidadeRepo.findById(idUnidade2).orElseThrow();

        // Gestor da Unidade Superior (quem aceita)
        Usuario gestor = UsuarioFixture.usuarioPadrao();
        gestor.setTituloEleitoral("707070707070");
        gestor.setNome("Gestor Validação");
        gestor.setUnidadeLotacao(unidadeSuperior);
        usuarioRepo.save(gestor);

        // Processo
        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo Validação CDU-25");
        processo = processoRepo.save(processo);

        // Subprocessos
        subprocesso1 = SubprocessoFixture.subprocessoPadrao(processo, unidade1);
        subprocesso1.setCodigo(null);
        subprocesso1.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocesso1 = subprocessoRepo.save(subprocesso1);

        subprocesso2 = SubprocessoFixture.subprocessoPadrao(processo, unidade2);
        subprocesso2.setCodigo(null);
        subprocesso2.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocesso2 = subprocessoRepo.save(subprocesso2);

        entityManager.flush();
        entityManager.clear();

        processo = processoRepo.findById(processo.getCodigo()).orElseThrow();
        subprocesso1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        subprocesso2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
        unidadeSuperior = unidadeRepo.findById(idSuperior).orElseThrow();
    }

    @Test
    @DisplayName("Deve aceitar validação de mapas em bloco")
    @WithMockGestor("707070707070")
    void aceitarValidacaoEmBloco_deveAceitarSucesso() throws Exception {
        // Given
        Long codigoContexto = subprocesso1.getCodigo();
        List<Long> unidadesSelecionadas = List.of(unidade1.getCodigo(), unidade2.getCodigo());

        ProcessarEmBlocoRequest request = new ProcessarEmBlocoRequest();
        request.setUnidadeCodigos(unidadesSelecionadas);

        // When
        mockMvc.perform(
                post("/api/subprocessos/{id}/aceitar-validacao-bloco", codigoContexto)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        entityManager.flush();
        entityManager.clear();

        // Check Subprocesso 1
        Subprocesso s1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        List<Analise> analises1 = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s1.getCodigo());
        assertThat(analises1).isNotEmpty();
        assertThat(analises1.getFirst().getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE_MAPEAMENTO);

        List<Movimentacao> movs1 = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s1.getCodigo());
        assertThat(movs1).isNotEmpty();
        assertThat(movs1.getFirst().getDescricao()).contains("Validação do mapa aceita");

        // Check Subprocesso 2
        Subprocesso s2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
        assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s2.getCodigo())).isNotEmpty();
    }
}
