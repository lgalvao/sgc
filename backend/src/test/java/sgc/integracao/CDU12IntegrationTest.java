package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.UnidadeMapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@Import({
        TestSecurityConfig.class,
        WithMockChefeSecurityContextFactory.class,
        WithMockAdminSecurityContextFactory.class,
        WithMockGestorSecurityContextFactory.class,
        UnidadeFixture.class,
        ProcessoFixture.class,
        SubprocessoFixture.class,
        MapaFixture.class,
        AtividadeFixture.class
})
@DisplayName("CDU-12: Verificar impactos no mapa de competências")
class CDU12IntegrationTest extends BaseIntegrationTest {

    private static final String API_SUBPROCESSOS_ID_IMPACTOS_MAPA =
            "/api/subprocessos/{codigo}/impactos-mapa";
    private static final String CHEFE_TITULO = "121212121212";
    private static final String TEM_IMPACTOS_JSON_PATH = "$.temImpactos";
    private static final String TOTAL_ATIVIDADES_INSERIDAS_JSON_PATH = "$.totalAtividadesInseridas";
    private static final String TOTAL_ATIVIDADES_REMOVIDAS_JSON_PATH = "$.totalAtividadesRemovidas";

    // Repositories
    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;
    @Autowired
    private CompetenciaRepo competenciaRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;


    private Atividade atividadeVigente1;
    private Atividade atividadeVigente2;
    private Subprocesso subprocessoRevisao;
    private Mapa mapaSubprocesso;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        // Reset sequence
        try {
            jdbcTemplate.execute("ALTER SEQUENCE SGC.VW_UNIDADE_SEQ RESTART WITH 1000");
        } catch (DataAccessException e) {
            try {
                jdbcTemplate.execute("ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN codigo RESTART WITH 1000");
            } catch (DataAccessException ex) {
                // Ignore
            }
        }

        unidade = UnidadeFixture.unidadeComSigla("IMPACT_TEST");
        unidade.setCodigo(null);
        unidade = unidadeRepo.save(unidade);

        Processo processoRevisao = ProcessoFixture.processoPadrao();
        processoRevisao.setCodigo(null);
        processoRevisao.setDescricao("Processo de Revisão 2024");
        processoRevisao.setTipo(TipoProcesso.REVISAO);
        processoRevisao.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoRevisao.setDataLimite(LocalDateTime.now().plusMonths(3));
        processoRevisao = processoRepo.save(processoRevisao);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setDataHoraHomologado(LocalDateTime.now().minusMonths(6));
        mapaVigente = mapaRepo.save(mapaVigente);

        unidadeMapaRepo.save(UnidadeMapa.builder().unidadeCodigo(unidade.getCodigo()).mapaVigente(mapaVigente).build());

        atividadeVigente1 = AtividadeFixture.atividadePadrao(mapaVigente);
        atividadeVigente1.setDescricao("Analisar e despachar processos.");
        atividadeVigente1 = atividadeRepo.save(atividadeVigente1);

        atividadeVigente2 = AtividadeFixture.atividadePadrao(mapaVigente);
        atividadeVigente2.setDescricao("Elaborar relatórios gerenciais.");
        atividadeVigente2 = atividadeRepo.save(atividadeVigente2);

        Competencia competenciaVigente1 = competenciaRepo.save(Competencia.builder()
                .descricao("Gerenciamento de Processos")
                .mapa(mapaVigente)
                .build());

        vincularAtividadeCompetencia(competenciaVigente1, atividadeVigente1);
        vincularAtividadeCompetencia(competenciaVigente1, atividadeVigente2);

        subprocessoRevisao = SubprocessoFixture.subprocessoPadrao(processoRevisao, unidade);
        subprocessoRevisao.setCodigo(null);
        subprocessoRevisao.setMapa(null); // Initially null, set later
        subprocessoRevisao.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        subprocessoRevisao.setDataLimiteEtapa1(processoRevisao.getDataLimite());
        subprocessoRevisao = subprocessoRepo.save(subprocessoRevisao);

        mapaSubprocesso = new Mapa();
        mapaSubprocesso.setSubprocesso(subprocessoRevisao);
        mapaSubprocesso = mapaRepo.save(mapaSubprocesso);

        subprocessoRevisao.setMapa(mapaSubprocesso);
        subprocessoRevisao = subprocessoRepo.save(subprocessoRevisao);

        // Setup user for security check if needed (Chefe with unit)
        // Note: WithMockChefe uses a default user or checks DB.
        // We might need to ensure user '121212121212' exists and is Chefe of 'unidade'.
        // For now, we rely on WithMockChefe mocking the Principal/Authorities,
        // but if the service checks DB (UsuarioFacade), we need data.
        // Assuming test mocks UsuarioFacade or we add data if it fails.
    }

    private void vincularAtividadeCompetencia(Competencia competencia, Atividade atividade) {
        competencia.getAtividades().add(atividade);
        atividade.getCompetencias().add(competencia);
        competenciaRepo.save(competencia);
        atividadeRepo.save(atividade);
    }

    // Helper to insert User/Profile data for security checks that hit the DB (or View)
    private void setupChefeForUnidade(String titulo, Unidade unidade) {
        jdbcTemplate.update("INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)",
                titulo, unidade.getCodigo(), sgc.organizacao.model.Perfil.CHEFE.name());
    }

    @Nested
    @DisplayName("Cenários de Sucesso")
    class Sucesso {

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("Não deve detectar impactos quando o cadastro de atividades é idêntico ao mapa vigente")
        void semImpactos_QuandoCadastroIdentico() throws Exception {
            // Need to setup the MockChefe properly linked to the Unidade for permission checks
            setupChefeForUnidade(CHEFE_TITULO, unidade);

            atividadeRepo.save(Atividade.builder().mapa(mapaSubprocesso).descricao(atividadeVigente1.getDescricao()).build());
            atividadeRepo.save(Atividade.builder().mapa(mapaSubprocesso).descricao(atividadeVigente2.getDescricao()).build());

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TEM_IMPACTOS_JSON_PATH, is(false)))
                    .andExpect(jsonPath(TOTAL_ATIVIDADES_INSERIDAS_JSON_PATH, is(0)));
        }

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("Deve detectar atividades inseridas")
        void deveDetectarAtividadesInseridas() throws Exception {
            setupChefeForUnidade(CHEFE_TITULO, unidade);

            atividadeRepo.save(Atividade.builder().mapa(mapaSubprocesso).descricao(atividadeVigente1.getDescricao()).build());
            atividadeRepo.save(Atividade.builder().mapa(mapaSubprocesso).descricao(atividadeVigente2.getDescricao()).build());
            atividadeRepo.save(Atividade.builder().mapa(mapaSubprocesso).descricao("Realizar auditorias internas.").build());

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TEM_IMPACTOS_JSON_PATH, is(true)))
                    .andExpect(jsonPath(TOTAL_ATIVIDADES_INSERIDAS_JSON_PATH, is(1)))
                    .andExpect(jsonPath("$.atividadesInseridas[0].descricao", is("Realizar auditorias internas.")));
        }

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("Deve detectar atividades removidas e as competências relacionadas")
        void deveDetectarAtividadesRemovidas() throws Exception {
            setupChefeForUnidade(CHEFE_TITULO, unidade);

            atividadeRepo.save(Atividade.builder().mapa(mapaSubprocesso).descricao(atividadeVigente1.getDescricao()).build());

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TEM_IMPACTOS_JSON_PATH, is(true)))
                    .andExpect(jsonPath(TOTAL_ATIVIDADES_REMOVIDAS_JSON_PATH, is(1)))
                    .andExpect(jsonPath("$.atividadesRemovidas[0].descricao", is(atividadeVigente2.getDescricao())));
        }

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("Deve detectar atividades alteradas como uma remoção e uma inserção")
        void deveDetectarAtividadesAlteradas() throws Exception {
            setupChefeForUnidade(CHEFE_TITULO, unidade);

            Atividade atividadeAlterada = Atividade.builder().mapa(mapaSubprocesso).descricao("Elaborar relatórios gerenciais e estratégicos.").build();
            atividadeRepo.save(atividadeAlterada);
            atividadeRepo.save(Atividade.builder().mapa(mapaSubprocesso).descricao(atividadeVigente1.getDescricao()).build());

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TEM_IMPACTOS_JSON_PATH, is(true)))
                    .andExpect(jsonPath(TOTAL_ATIVIDADES_INSERIDAS_JSON_PATH, is(1)))
                    .andExpect(jsonPath(TOTAL_ATIVIDADES_REMOVIDAS_JSON_PATH, is(1)));
        }

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("Deve identificar competências impactadas por remoções e alterações")
        void deveIdentificarCompetenciasImpactadas() throws Exception {
            setupChefeForUnidade(CHEFE_TITULO, unidade);

            Atividade atividadeNova = Atividade.builder().mapa(mapaSubprocesso).descricao("Elaborar relatórios gerenciais e estratégicos.").build();
            atividadeRepo.save(atividadeNova);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCompetenciasImpactadas", is(1)));
        }
    }

    @Nested
    @DisplayName("Cenários de Borda e Falhas")
    class BordaEfalhas {

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("Não deve detectar impactos se a unidade não possui mapa vigente")
        void semImpactos_QuandoNaoExisteMapaVigente() throws Exception {
            setupChefeForUnidade(CHEFE_TITULO, unidade);
            unidadeMapaRepo.deleteById(unidade.getCodigo());

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TEM_IMPACTOS_JSON_PATH, is(false)));
        }

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("Deve retornar 404 para subprocesso inexistente")
        void deveRetornar404_QuandoSubprocessoNaoExiste() throws Exception {
            setupChefeForUnidade(CHEFE_TITULO, unidade);
            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, 9999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testes de Controle de Acesso por Perfil e Situação")
    class Acesso {

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("CHEFE pode acessar se subprocesso está em 'Revisão do cadastro em andamento'")
        void chefePodeAcessar_EmRevisaoCadastro() throws Exception {
            setupChefeForUnidade(CHEFE_TITULO, unidade);
            subprocessoRevisao.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            subprocessoRepo.save(subprocessoRevisao);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockGestor
        @DisplayName("GESTOR pode acessar se subprocesso está em 'Revisão do cadastro disponibilizada'")
        void gestorPodeAcessar_EmRevisaoDisponibilizada() throws Exception {
            // Gestor usually has global access or needs setup too.
            // Assuming MockGestor works globally or we need to link.
            subprocessoRevisao.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
            subprocessoRepo.save(subprocessoRevisao);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockAdmin
        @DisplayName("ADMIN pode acessar se subprocesso está em 'Revisão do cadastro homologada'")
        void adminPodeAcessar_EmRevisaoHomologada() throws Exception {
            subprocessoRevisao.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            subprocessoRepo.save(subprocessoRevisao);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockAdmin
        @DisplayName("ADMIN pode acessar se subprocesso está em 'Mapa Ajustado'")
        void adminPodeAcessar_EmMapaAjustado() throws Exception {
            subprocessoRevisao.setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            subprocessoRepo.save(subprocessoRevisao);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("CHEFE NÃO pode acessar se subprocesso está em situação diferente de 'Revisão do cadastro em andamento'")
        void chefeNaoPodeAcessar_EmSituacaoIncorreta() throws Exception {
            setupChefeForUnidade(CHEFE_TITULO, unidade);
            subprocessoRevisao.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
            subprocessoRepo.save(subprocessoRevisao);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isForbidden());
        }
    }
}
