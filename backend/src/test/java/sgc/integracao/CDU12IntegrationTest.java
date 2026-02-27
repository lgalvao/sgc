package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private static final String API_SUBPROCESSOS_ID_IMPACTOS_MAPA = "/api/subprocessos/{codigo}/impactos-mapa";

    private static final String CHEFE_TITULO = "121212121212";
    private static final String GESTOR_TITULO = "666666666666";

    private static final String TEM_IMPACTOS_JSON_PATH = "$.temImpactos";
    private static final String TOTAL_ATIVIDADES_INSERIDAS_JSON_PATH = "$.inseridas.length()";
    private static final String TOTAL_ATIVIDADES_REMOVIDAS_JSON_PATH = "$.removidas.length()";

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
    private Subprocesso sp;
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

        sp = SubprocessoFixture.subprocessoPadrao(processoRevisao, unidade);
        sp.setCodigo(null);
        sp.setMapa(null);
        sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        sp.setDataLimiteEtapa1(processoRevisao.getDataLimite());
        sp = subprocessoRepo.save(sp);

        mapaSubprocesso = new Mapa();
        mapaSubprocesso.setSubprocesso(sp);
        mapaSubprocesso = mapaRepo.save(mapaSubprocesso);

        sp.setMapa(mapaSubprocesso);
        sp = subprocessoRepo.save(sp);
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

    private void setupChefeForUnidade(String titulo, Unidade unidade) {
        jdbcTemplate.update(
                "INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)",
                titulo, unidade.getCodigo(), Perfil.CHEFE.name()
        );

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
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
            setupChefeForUnidade(CHEFE_TITULO, unidade);

            atividadeRepo.save(Atividade.builder()
                    .mapa(mapaSubprocesso)
                    .descricao(atividadeVigente1.getDescricao())
                    .build());

            atividadeRepo.save(Atividade.builder()
                    .mapa(mapaSubprocesso)
                    .descricao(atividadeVigente2.getDescricao())
                    .build());

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, sp.getCodigo()))
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

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, sp.getCodigo()))
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

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, sp.getCodigo()))
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

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, sp.getCodigo()))
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

            Atividade atividadeNova = Atividade.builder()
                    .mapa(mapaSubprocesso)
                    .descricao("Elaborar relatórios gerenciais e estratégicos.")
                    .build();

            atividadeRepo.save(atividadeNova);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, sp.getCodigo()))
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

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, sp.getCodigo()))
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
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            subprocessoRepo.save(sp);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, sp.getCodigo()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockGestor(GESTOR_TITULO)
        @DisplayName("GESTOR pode acessar se subprocesso está em 'Revisão do cadastro disponibilizada'")
        void gestorPodeAcessar_EmRevisaoDisponibilizada() throws Exception {
            setupGestorForUnidadeSuperior(GESTOR_TITULO, unidade);
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
            subprocessoRepo.save(sp);

            // Simular envio para unidade superior (onde está o gestor)
            Movimentacao movimentacao = Movimentacao.builder()
                    .subprocesso(sp)
                    .unidadeOrigem(unidade)
                    .unidadeDestino(unidade.getUnidadeSuperior())
                    .descricao("Enviado para Gestor")
                    .dataHora(LocalDateTime.now())
                    .build();
            movimentacaoRepo.save(movimentacao);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, sp.getCodigo()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockAdmin
        @DisplayName("ADMIN pode acessar se subprocesso está em 'Revisão do cadastro homologada'")
        void adminPodeAcessar_EmRevisaoHomologada() throws Exception {
            configurarUnidadeAdministrador(unidade.getCodigo());

            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            subprocessoRepo.save(sp);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, sp.getCodigo()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockAdmin
        @DisplayName("ADMIN pode acessar se subprocesso está em 'Mapa Ajustado'")
        void adminPodeAcessar_EmMapaAjustado() throws Exception {
            configurarUnidadeAdministrador(unidade.getCodigo());
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            subprocessoRepo.save(sp);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, sp.getCodigo()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("CHEFE recebe erro 422 se sp em situação diferente de 'Revisão do cadastro em andamento' mas com permissão de vis.")
        void chefeRecebeErroValidacao_EmSituacaoIncorreta() throws Exception {
            setupChefeForUnidade(CHEFE_TITULO, unidade);
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
            subprocessoRepo.save(sp);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, sp.getCodigo()))
                    .andExpect(status().isUnprocessableContent()); // Espera 422
        }
    }
}
