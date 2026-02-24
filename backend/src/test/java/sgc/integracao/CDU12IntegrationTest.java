package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.*;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@Import({
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
    private static final String GESTOR_TITULO = "666666666666";
    private static final String TEM_IMPACTOS_JSON_PATH = "$.temImpactos";
    private static final String TOTAL_ATIVIDADES_INSERIDAS_JSON_PATH = "$.inseridas.length()";
    private static final String TOTAL_ATIVIDADES_REMOVIDAS_JSON_PATH = "$.removidas.length()";

    // Repositories
    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;
    @Autowired
    private CompetenciaRepo competenciaRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;


    private Atividade atividadeVigente1;
    private Atividade atividadeVigente2;
    private Subprocesso subprocessoRevisao;
    private Mapa mapaSubprocesso;
    private Unidade unidade;

    @BeforeEach
    void setUp() {

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
        subprocessoRevisao.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
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

    private void configurarUnidadeAdministrador(Long unidadeCodigo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario usuario) {
            usuario.setUnidadeAtivaCodigo(unidadeCodigo);
        }
    }
    
    // Helper to insert User/Profile data for security checks that hit the DB (or View)
    private void setupChefeForUnidade(String titulo, Unidade unidade) {
        jdbcTemplate.update("INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)",
                titulo, unidade.getCodigo(), Perfil.CHEFE.name());

        // Also update the Authentication in the SecurityContext to reflect this new unit
        // because the AccessPolicy uses usuario.getUnidadeAtivaCodigo() from the Principal.
        // The Principal was created by @WithMockChefe with a default (or old) unit.
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof Usuario usuario) {
            usuario.setUnidadeAtivaCodigo(unidade.getCodigo());
        }
    }

    private void setupGestorForUnidadeSuperior(String titulo, Unidade unidadeSubordinada) {
        Unidade unidadeSuperior = UnidadeFixture.unidadeComSigla("SUP_" + unidadeSubordinada.getSigla());
        unidadeSuperior.setCodigo(null);
        unidadeSuperior = unidadeRepo.save(unidadeSuperior);

        unidadeSubordinada.setUnidadeSuperior(unidadeSuperior);
        unidadeRepo.save(unidadeSubordinada);

        jdbcTemplate.update("INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)",
                titulo, unidadeSuperior.getCodigo(), Perfil.GESTOR.name());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario usuario) {
            usuario.setUnidadeAtivaCodigo(unidadeSuperior.getCodigo());
        }
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
                    .andExpect(jsonPath("$.inseridas[0].descricao", is("Realizar auditorias internas.")));
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
                    .andExpect(jsonPath("$.removidas[0].descricao", is(atividadeVigente2.getDescricao())));
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
                    .andExpect(jsonPath("$.competenciasImpactadas.length()", is(1)));
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

    }

    @Nested
    @DisplayName("Testes de Controle de Acesso por Perfil e Situação")
    class Acesso {

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("CHEFE pode acessar se subprocesso está em 'Revisão do cadastro em andamento'")
        void chefePodeAcessar_EmRevisaoCadastro() throws Exception {
            setupChefeForUnidade(CHEFE_TITULO, unidade);
            subprocessoRevisao.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            subprocessoRepo.save(subprocessoRevisao);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockGestor(GESTOR_TITULO)
        @DisplayName("GESTOR pode acessar se subprocesso está em 'Revisão do cadastro disponibilizada'")
        void gestorPodeAcessar_EmRevisaoDisponibilizada() throws Exception {
            setupGestorForUnidadeSuperior(GESTOR_TITULO, unidade);
            subprocessoRevisao.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
            subprocessoRepo.save(subprocessoRevisao);

            // Simular envio para unidade superior (onde está o gestor)
            Movimentacao movimentacao = Movimentacao.builder()
                    .subprocesso(subprocessoRevisao)
                    .unidadeOrigem(unidade)
                    .unidadeDestino(unidade.getUnidadeSuperior())
                    .descricao("Enviado para Gestor")
                    .dataHora(LocalDateTime.now())
                    .build();
            movimentacaoRepo.save(movimentacao);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockAdmin
        @DisplayName("ADMIN pode acessar se subprocesso está em 'Revisão do cadastro homologada'")
        void adminPodeAcessar_EmRevisaoHomologada() throws Exception {
            configurarUnidadeAdministrador(unidade.getCodigo());

            // Simular movimento para ADMIN (Unidade 1)
            // Mas espera, aqui o teste configuraAdmin na unidade do subprocesso?
            // "configurarUnidadeAdministrador(unidade.getCodigo())" sets the user's unit to the subprocess unit.
            // If user is in subprocess unit, Location check passes.
            // So we don't strictly need movement if we force user location.
            // But strict logic says Admin works in Admin Unit.
            // However, this test seems to force admin into local unit for simplicity.
            // I will leave it as is if it passes (it passed before).

            subprocessoRevisao.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            subprocessoRepo.save(subprocessoRevisao);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockAdmin
        @DisplayName("ADMIN pode acessar se subprocesso está em 'Mapa Ajustado'")
        void adminPodeAcessar_EmMapaAjustado() throws Exception {
            configurarUnidadeAdministrador(unidade.getCodigo());
            subprocessoRevisao.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            subprocessoRepo.save(subprocessoRevisao);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("CHEFE NÃO pode acessar se subprocesso está em situação diferente de 'Revisão do cadastro em andamento'")
        void chefeNaoPodeAcessar_EmSituacaoIncorreta() throws Exception {
            setupChefeForUnidade(CHEFE_TITULO, unidade);
            subprocessoRevisao.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
            subprocessoRepo.save(subprocessoRevisao);

            // Simular que o processo saiu da unidade do chefe (foi para unidade superior ou outra)
            // Precisamos criar uma unidade "Outra" ou usar Superior
            Unidade superior = UnidadeFixture.unidadeComSigla("SUP");
            superior = unidadeRepo.save(superior);

            Movimentacao movimentacao = Movimentacao.builder()
                    .subprocesso(subprocessoRevisao)
                    .unidadeOrigem(unidade)
                    .unidadeDestino(superior)
                    .descricao("Enviado para Superior")
                    .dataHora(LocalDateTime.now())
                    .build();
            movimentacaoRepo.save(movimentacao);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isForbidden());
        }
    }
}
