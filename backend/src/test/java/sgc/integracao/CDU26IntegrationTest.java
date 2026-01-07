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
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UsuarioFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.ProcessarEmBlocoRequest;
import sgc.subprocesso.model.*;
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
@DisplayName("CDU-26: Homologar validação de mapas em bloco")
class CDU26IntegrationTest extends BaseIntegrationTest {

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
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Unidade unidade1;
    private Unidade unidade2;
    private Processo processo;
    private Subprocesso subprocesso1;
    private Subprocesso subprocesso2;

    @BeforeEach
    void setUp() {
        Long idSuperior = 8000L;
        Long idUnidade1 = 8001L;
        Long idUnidade2 = 8002L;

        String sqlInsertUnidade = "INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlInsertUnidade, idSuperior, "Coordenação Homolog Valid", "COORD-HOM-VAL", "INTERMEDIARIA", "ATIVA", null, null);
        jdbcTemplate.update(sqlInsertUnidade, idUnidade1, "Unidade Hom Val 1", "UNID-HOM-VAL-1", "OPERACIONAL", "ATIVA", idSuperior, null);
        jdbcTemplate.update(sqlInsertUnidade, idUnidade2, "Unidade Hom Val 2", "UNID-HOM-VAL-2", "OPERACIONAL", "ATIVA", idSuperior, null);

        unidade1 = unidadeRepo.findById(idUnidade1).orElseThrow();
        unidade2 = unidadeRepo.findById(idUnidade2).orElseThrow();

        Usuario admin = UsuarioFixture.usuarioPadrao();
        admin.setTituloEleitoral("555555555555");
        usuarioRepo.save(admin);

        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo Homologação Validação CDU-26");
        processo = processoRepo.save(processo);

        // Subprocessos (Status deve ser Validado ou similar, pronto para homologação)
        // O CDU-26 geralmente segue o aceite da validação. Se o gestor aceitou, sobe para admin homologar.
        // O status "MAPEAMENTO_MAPA_VALIDADO" é um bom candidato se não houver um "VALIDACAO_ACEITA" intermediário.
        subprocesso1 = SubprocessoFixture.subprocessoPadrao(processo, unidade1);
        subprocesso1.setCodigo(null);
        subprocesso1.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocesso1 = subprocessoRepo.save(subprocesso1);

        subprocesso2 = SubprocessoFixture.subprocessoPadrao(processo, unidade2);
        subprocesso2.setCodigo(null);
        subprocesso2.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocesso2 = subprocessoRepo.save(subprocesso2);

        entityManager.flush();
        entityManager.clear();

        processo = processoRepo.findById(processo.getCodigo()).orElseThrow();
        subprocesso1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        subprocesso2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
    }

    @Test
    @DisplayName("Deve homologar validação de mapas em bloco")
    @WithMockAdmin
    void homologarValidacaoEmBloco_deveHomologarSucesso() throws Exception {
        // Given
        Long codigoContexto = subprocesso1.getCodigo();
        List<Long> unidadesSelecionadas = List.of(unidade1.getCodigo(), unidade2.getCodigo());

        ProcessarEmBlocoRequest request = new ProcessarEmBlocoRequest();
        request.setUnidadeCodigos(unidadesSelecionadas);

        // When
        mockMvc.perform(
                post("/api/subprocessos/{id}/homologar-validacao-bloco", codigoContexto)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        entityManager.flush();
        entityManager.clear();

        // Check Subprocesso 1
        Subprocesso s1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        assertThat(s1.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);

        List<Movimentacao> movs1 = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s1.getCodigo());
        assertThat(movs1).isNotEmpty();
        // A mensagem de homologação pode variar. "Mapa de competências homologado"
        assertThat(movs1.getFirst().getDescricao()).contains("homologado");

        // Check Subprocesso 2
        Subprocesso s2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
        assertThat(s2.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
    }
}
