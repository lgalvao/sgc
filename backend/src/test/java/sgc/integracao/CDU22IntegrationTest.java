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
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.ProcessarEmBlocoRequest;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, sgc.integracao.mocks.TestThymeleafConfig.class})
@Transactional
@DisplayName("CDU-22: Aceitar cadastros em bloco")
class CDU22IntegrationTest extends BaseIntegrationTest {

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
        // Criar Unidades
        Long idSuperior = 4000L;
        Long idUnidade1 = 4001L;
        Long idUnidade2 = 4002L;

        String sqlInsertUnidade = "INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular) VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sqlInsertUnidade, idSuperior, "Coordenação de Bloco", "COORD-BLOCO", "INTERMEDIARIA", "ATIVA", null, null);
        jdbcTemplate.update(sqlInsertUnidade, idUnidade1, "Unidade Bloco 1", "UNID-BLOCO-1", "OPERACIONAL", "ATIVA", idSuperior, null);
        jdbcTemplate.update(sqlInsertUnidade, idUnidade2, "Unidade Bloco 2", "UNID-BLOCO-2", "OPERACIONAL", "ATIVA", idSuperior, null);

        unidadeSuperior = unidadeRepo.findById(idSuperior).orElseThrow();
        unidade1 = unidadeRepo.findById(idUnidade1).orElseThrow();
        unidade2 = unidadeRepo.findById(idUnidade2).orElseThrow();

        // Criar Usuário Gestor
        Usuario gestorUser = UsuarioFixture.usuarioPadrao();
        gestorUser.setTituloEleitoral("303030303030");
        gestorUser.setNome("Gestor Bloco");
        gestorUser.setUnidadeLotacao(unidadeSuperior);
        usuarioRepo.save(gestorUser);

        // Criar Processo
        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo Bloco CDU-22");
        processo = processoRepo.save(processo);

        // Criar Subprocessos
        subprocesso1 = SubprocessoFixture.subprocessoPadrao(processo, unidade1);
        subprocesso1.setCodigo(null);
        subprocesso1.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocesso1.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso1 = subprocessoRepo.save(subprocesso1);

        subprocesso2 = SubprocessoFixture.subprocessoPadrao(processo, unidade2);
        subprocesso2.setCodigo(null);
        subprocesso2.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocesso2.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso2 = subprocessoRepo.save(subprocesso2);

        entityManager.flush();
        entityManager.clear();

        // Reload to attach
        processo = processoRepo.findById(processo.getCodigo()).orElseThrow();
        subprocesso1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        subprocesso2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
        unidadeSuperior = unidadeRepo.findById(idSuperior).orElseThrow();
    }

    @Test
    @DisplayName("Deve aceitar cadastro de múltiplas unidades em bloco")
    @WithMockGestor("303030303030")
    void aceitarCadastroEmBloco_deveAceitarTodasSelecionadas() throws Exception {
        // Given
        Long codigoContexto = subprocesso1.getCodigo();
        List<Long> unidadesSelecionadas = List.of(unidade1.getCodigo(), unidade2.getCodigo());

        ProcessarEmBlocoRequest request = new ProcessarEmBlocoRequest();
        request.setUnidadeCodigos(unidadesSelecionadas);

        // When
        mockMvc.perform(
                post("/api/subprocessos/{id}/aceitar-cadastro-bloco", codigoContexto)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        entityManager.flush();
        entityManager.clear();

        // Verify Subprocesso 1
        Subprocesso s1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        List<Analise> analises1 = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s1.getCodigo());
        assertThat(analises1).isNotEmpty();
        assertThat(analises1.getFirst().getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE_MAPEAMENTO);
        assertThat(analises1.getFirst().getUnidadeCodigo()).isEqualTo(unidadeSuperior.getCodigo());

        List<Movimentacao> movs1 = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s1.getCodigo());
        assertThat(movs1).isNotEmpty();
        assertThat(movs1.getFirst().getDescricao()).contains("aceito");
        assertThat(movs1.getFirst().getUnidadeDestino().getCodigo()).isEqualTo(unidadeSuperior.getCodigo());

        // Verify Subprocesso 2
        Subprocesso s2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
        List<Analise> analises2 = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s2.getCodigo());
        assertThat(analises2).isNotEmpty();
        assertThat(analises2.getFirst().getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE_MAPEAMENTO);

        List<Movimentacao> movs2 = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s2.getCodigo());
        assertThat(movs2).isNotEmpty();
        assertThat(movs2.getFirst().getUnidadeDestino().getCodigo()).isEqualTo(unidadeSuperior.getCodigo());
    }
}
