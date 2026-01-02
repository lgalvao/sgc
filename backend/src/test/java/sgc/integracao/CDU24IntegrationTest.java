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
import sgc.mapa.model.*;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.ProcessarEmBlocoRequest;
import sgc.subprocesso.model.*;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, sgc.integracao.mocks.TestThymeleafConfig.class})
@Transactional
@DisplayName("CDU-24: Disponibilizar mapas de competências em bloco")
class CDU24IntegrationTest extends BaseIntegrationTest {

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
    private AtividadeRepo atividadeRepo;

    @Autowired
    private CompetenciaRepo competenciaRepo;

    @Autowired
    private MapaRepo mapaRepo;

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
        Long idSuperior = 6000L;
        Long idUnidade1 = 6001L;
        Long idUnidade2 = 6002L;

        String sqlInsertUnidade = "INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlInsertUnidade, idSuperior, "Coordenação Mapa", "COORD-MAPA", "INTERMEDIARIA", "ATIVA", null, null);
        jdbcTemplate.update(sqlInsertUnidade, idUnidade1, "Unidade Mapa 1", "UNID-MAPA-1", "OPERACIONAL", "ATIVA", idSuperior, null);
        jdbcTemplate.update(sqlInsertUnidade, idUnidade2, "Unidade Mapa 2", "UNID-MAPA-2", "OPERACIONAL", "ATIVA", idSuperior, null);

        unidade1 = unidadeRepo.findById(idUnidade1).orElseThrow();
        unidade2 = unidadeRepo.findById(idUnidade2).orElseThrow();

        // Usuário Admin
        Usuario admin = UsuarioFixture.usuarioPadrao();
        admin.setTituloEleitoral("888888888888");
        usuarioRepo.save(admin);

        // Processo
        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo Mapa CDU-24");
        processo = processoRepo.save(processo);

        // Subprocessos (Status: MAPEAMENTO_MAPA_CRIADO)
        subprocesso1 = createSubprocessoComMapaCompleto(unidade1);
        subprocesso2 = createSubprocessoComMapaCompleto(unidade2);

        entityManager.flush();
        entityManager.clear();

        processo = processoRepo.findById(processo.getCodigo()).orElseThrow();
        subprocesso1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        subprocesso2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
    }

    private Subprocesso createSubprocessoComMapaCompleto(Unidade unidade) {
        // Criar Subprocesso
        Subprocesso sub = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        sub.setCodigo(null);
        sub.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        sub = subprocessoRepo.save(sub);

        // Criar Mapa
        Mapa mapa = new Mapa();
        mapa.setSubprocesso(sub);
        mapa = mapaRepo.save(mapa);

        // Associar mapa ao subprocesso
        sub.setMapa(mapa);
        sub = subprocessoRepo.save(sub);

        // Criar Atividade
        Atividade ativ = new Atividade(mapa, "Atividade Teste " + unidade.getSigla());
        ativ = atividadeRepo.save(ativ);

        // Criar Competência
        Competencia comp = new Competencia("Competência Teste " + unidade.getSigla(), mapa);
        comp = competenciaRepo.save(comp);

        // Associar (ManyToMany)
        ativ.getCompetencias().add(comp);
        ativ = atividadeRepo.save(ativ);

        return sub;
    }

    @Test
    @DisplayName("Deve disponibilizar mapas de competências em bloco (sucesso)")
    @WithMockAdmin
    void disponibilizarMapaEmBloco_deveDisponibilizarSucesso() throws Exception {
        // Given
        Long codigoContexto = subprocesso1.getCodigo();
        List<Long> unidadesSelecionadas = List.of(unidade1.getCodigo(), unidade2.getCodigo());
        LocalDate dataLimite = LocalDate.now().plusDays(15);

        ProcessarEmBlocoRequest request = new ProcessarEmBlocoRequest();
        request.setUnidadeCodigos(unidadesSelecionadas);
        request.setDataLimite(dataLimite);

        // When
        mockMvc.perform(
                post("/api/subprocessos/{id}/disponibilizar-mapa-bloco", codigoContexto)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        entityManager.flush();
        entityManager.clear();

        // Verificações para Subprocesso 1
        Subprocesso s1 = subprocessoRepo.findById(subprocesso1.getCodigo()).orElseThrow();
        assertThat(s1.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);

        List<Movimentacao> movs1 = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(s1.getCodigo());
        assertThat(movs1).isNotEmpty();
        assertThat(movs1.getFirst().getDescricao()).contains("Disponibilização do mapa");

        // Verificações para Subprocesso 2
        Subprocesso s2 = subprocessoRepo.findById(subprocesso2.getCodigo()).orElseThrow();
        assertThat(s2.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
    }
}
