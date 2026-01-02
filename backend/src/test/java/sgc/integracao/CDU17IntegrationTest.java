package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.analise.model.Analise;
import sgc.analise.model.AnaliseRepo;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.fixture.MapaFixture;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockGestor;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.DisponibilizarMapaReq;
import sgc.subprocesso.model.*;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-17: Disponibilizar Mapa de Competências")
@Import({TestSecurityConfig.class, sgc.integracao.mocks.TestThymeleafConfig.class})
class CDU17IntegrationTest extends BaseIntegrationTest {
    private static final String API_URL = "/api/subprocessos/{codigo}/disponibilizar-mapa";
    private static final String OBS_LITERAL = "Obs";
    private static final String SEDOC_LITERAL = "SEDOC";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private MapaRepo mapaRepo;
    @Autowired
    private AtividadeRepo atividadeRepo;
    @Autowired
    private CompetenciaRepo competenciaRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private AnaliseRepo analiseRepo;
    @Autowired
    private ProcessoRepo processoRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Unidade unidade;
    private Subprocesso subprocesso;
    private Mapa mapa;

    @BeforeEach
    void setUp() {
        // Use existing SEDOC from data.sql (ID 15) instead of creating a duplicate with ID 100.
        // Creating a duplicate 'SEDOC' causes NonUniqueResultException in services looking up by sigla.

        // Ensure Admin user has profile in SEDOC (ID 15)
        jdbcTemplate.update("MERGE INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) KEY(usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)",
                "111111111111", 15, "ADMIN");

        // Criar Unidade via Fixture
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setNome("Unidade CDU-17");
        unidade.setSigla("U17");

        // H2 Sequence Reset workaround for Unidade because it is usually inserted via SQL with IDs
        // Resetting sequence to avoid collision with existing IDs (1..30, 100, 200...)
        jdbcTemplate.execute("ALTER TABLE sgc.vw_unidade ALTER COLUMN codigo RESTART WITH 1000");

        unidade = unidadeRepo.save(unidade);

        // Criar Processo via Fixture
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.REVISAO);
        processo = processoRepo.save(processo);

        // Criar Mapa
        mapa = MapaFixture.mapaPadrao(null);
        mapa.setCodigo(null);
        mapa = mapaRepo.save(mapa);

        // Criar Subprocesso via Fixture
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setMapa(mapa);
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        subprocesso.setDataLimiteEtapa2(null);
        subprocesso.setDataFimEtapa2(null);
        subprocesso = subprocessoRepo.save(subprocesso);

        // Link mapa back to subprocesso if needed or just ensured via subprocesso.setMapa
        // MapaFixture.mapaPadrao(null) leaves it null, but subprocesso.setMapa sets it on subprocesso.

        // Setup inicial de Atividade e Competência válidas
        Atividade atividade = new Atividade(mapa, "Atividade Valida");
        atividade = atividadeRepo.save(atividade);

        Competencia competencia = new Competencia("Competencia Valida", mapa);
        competencia = competenciaRepo.save(competencia);

        // Associar (ManyToMany manually if needed or via helper methods)
        atividade.getCompetencias().add(competencia);
        atividadeRepo.save(atividade);

        competencia.getAtividades().add(atividade);
        competenciaRepo.save(competencia);
    }

    @Nested
    @DisplayName("Testes de Sucesso")
    class Sucesso {

        @Test
        @DisplayName("Deve disponibilizar mapa quando todos os dados estão corretos")
        @WithMockAdmin
        void disponibilizarMapa_comDadosValidos_retornaOk() throws Exception {
            Analise analiseAntiga = new Analise();
            analiseAntiga.setSubprocesso(subprocesso);
            analiseAntiga.setObservacoes("Análise antiga que deve ser removida.");
            analiseRepo.save(analiseAntiga);

            LocalDate dataLimite = LocalDate.now().plusDays(10);
            String observacoes = "Observações de teste para o mapa.";
            DisponibilizarMapaReq request = new DisponibilizarMapaReq(dataLimite, observacoes);

            // Print error if 500
            mockMvc.perform(
                            post(API_URL, subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.message").value("Mapa de competências disponibilizado."));

            Subprocesso spAtualizado =
                    subprocessoRepo
                            .findById(subprocesso.getCodigo())
                            .orElseThrow();
            // Subprocesso starts as REVISAO... so it should transition to REVISAO_MAPA_DISPONIBILIZADO
            assertThat(spAtualizado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);
            assertThat(spAtualizado.getDataLimiteEtapa2()).isEqualTo(dataLimite.atStartOfDay());

            Mapa mapaAtualizado =
                    mapaRepo.findById(mapa.getCodigo())
                            .orElseThrow();
            assertThat(mapaAtualizado.getSugestoes()).isEqualTo(observacoes);

            List<Movimentacao> movimentacoes =
                    movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                            subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            Movimentacao mov = movimentacoes.getFirst();
            assertThat(mov.getUnidadeOrigem().getSigla()).isEqualTo(SEDOC_LITERAL);
            assertThat(mov.getUnidadeDestino().getSigla()).isEqualTo(unidade.getSigla());
            assertThat(mov.getDescricao())
                    .isEqualTo("Disponibilização do mapa de competências para validação");

            List<Alerta> alertas =
                    alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo());
            assertThat(alertas).hasSize(1);

            List<Analise> analisesRestantes =
                    analiseRepo.findBySubprocessoCodigo(subprocesso.getCodigo());
            assertThat(analisesRestantes).isEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Falha")
    class Falha {
        @Test
        @DisplayName("Não deve disponibilizar mapa com usuário sem permissão (não ADMIN)")
        @WithMockGestor
        void disponibilizarMapa_semPermissao_retornaForbidden() throws Exception {
            DisponibilizarMapaReq request =
                    new DisponibilizarMapaReq(LocalDate.now().plusDays(10), OBS_LITERAL);

            mockMvc.perform(
                            post(API_URL, subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Não deve disponibilizar mapa se subprocesso não está no estado correto")
        @WithMockAdmin
        void disponibilizarMapa_comEstadoInvalido_retornaConflict() throws Exception {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            subprocessoRepo.save(subprocesso);

            DisponibilizarMapaReq request =
                    new DisponibilizarMapaReq(LocalDate.now().plusDays(10), OBS_LITERAL);

            mockMvc.perform(
                            post(API_URL, subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableContent());
        }

        @Test
        @DisplayName("Não deve disponibilizar mapa se houver atividade sem competência associada")
        @WithMockAdmin
        void disponibilizarMapa_comAtividadeNaoAssociada_retornaBadRequest() throws Exception {
            // Cria uma nova atividade sem competências para criar o cenário de erro
            Atividade atividadeSolta = new Atividade(mapa, "Atividade Solta");
            atividadeRepo.save(atividadeSolta);

            DisponibilizarMapaReq request =
                    new DisponibilizarMapaReq(LocalDate.now().plusDays(10), OBS_LITERAL);

            mockMvc.perform(
                            post(API_URL, subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableContent())
                    // Adjusted expectation based on actual error message
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Todas as atividades devem estar associadas a pelo menos uma competência.")));
        }

        @Test
        @DisplayName("Não deve disponibilizar mapa se houver competência sem atividade associada")
        @WithMockAdmin
        void disponibilizarMapa_comCompetenciaNaoAssociada_retornaBadRequest() throws Exception {
            competenciaRepo.save(new Competencia("Competência Solta", mapa));

            DisponibilizarMapaReq request =
                    new DisponibilizarMapaReq(LocalDate.now().plusDays(10), OBS_LITERAL);

            mockMvc.perform(
                            post(API_URL, subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.message").value("Todas as competências devem estar associadas a pelo menos uma atividade."));
        }
    }
}
