package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.integracao.mocks.*;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeMapa;
import sgc.unidade.model.UnidadeMapaRepo;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@Import({
        TestSecurityConfig.class,
        WithMockChefeSecurityContextFactory.class,
        WithMockAdminSecurityContextFactory.class,
        WithMockGestorSecurityContextFactory.class
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
    private ProcessoRepo processoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private MapaRepo mapaRepo;
    @Autowired
    private AtividadeRepo atividadeRepo;
    @Autowired
    private CompetenciaRepo competenciaRepo;

    private Atividade atividadeVigente1;
    private Atividade atividadeVigente2;
    private Competencia competenciaVigente1;
    private Subprocesso subprocessoRevisao;
    private Mapa mapaSubprocesso;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        unidade = unidadeRepo.findById(12L).orElseThrow();

        Processo processoRevisao =
                new Processo(
                        "Processo de Revisão 2024",
                        TipoProcesso.REVISAO,
                        SituacaoProcesso.EM_ANDAMENTO,
                        LocalDateTime.now().plusMonths(3));
        processoRepo.save(processoRevisao);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setDataHoraHomologado(LocalDateTime.now().minusMonths(6));
        mapaVigente = mapaRepo.save(mapaVigente);
        unidadeRepo.save(unidade);

        unidadeMapaRepo.save(new UnidadeMapa(unidade.getCodigo(), mapaVigente));

        atividadeVigente1 =
                atividadeRepo.save(new Atividade(mapaVigente, "Analisar e despachar processos."));
        atividadeVigente2 =
                atividadeRepo.save(new Atividade(mapaVigente, "Elaborar relatórios gerenciais."));

        competenciaVigente1 =
                competenciaRepo.save(new Competencia("Gerenciamento de Processos", mapaVigente));

        vincularAtividadeCompetencia(competenciaVigente1, atividadeVigente1);
        vincularAtividadeCompetencia(competenciaVigente1, atividadeVigente2);

        subprocessoRevisao =
                new Subprocesso(
                        processoRevisao,
                        unidade,
                        null,
                        SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
                        processoRevisao.getDataLimite());
        subprocessoRepo.save(subprocessoRevisao);

        mapaSubprocesso = new Mapa();
        mapaSubprocesso.setSubprocesso(subprocessoRevisao);
        mapaRepo.save(mapaSubprocesso);

        subprocessoRevisao.setMapa(mapaSubprocesso);
        subprocessoRepo.save(subprocessoRevisao);
    }

    private void vincularAtividadeCompetencia(Competencia competencia, Atividade atividade) {
        competencia.getAtividades().add(atividade);
        atividade.getCompetencias().add(competencia);
        competenciaRepo.save(competencia);
        atividadeRepo.save(atividade);
    }

    @Nested
    @DisplayName("Cenários de Sucesso")
    class Sucesso {

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName(
                "Não deve detectar impactos quando o cadastro de atividades é idêntico ao mapa"
                        + " vigente")
        void semImpactos_QuandoCadastroIdentico() throws Exception {
            atividadeRepo.save(new Atividade(mapaSubprocesso, atividadeVigente1.getDescricao()));
            atividadeRepo.save(new Atividade(mapaSubprocesso, atividadeVigente2.getDescricao()));

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TEM_IMPACTOS_JSON_PATH, is(false)))
                    .andExpect(jsonPath(TOTAL_ATIVIDADES_INSERIDAS_JSON_PATH, is(0)))
                    .andExpect(jsonPath(TOTAL_ATIVIDADES_REMOVIDAS_JSON_PATH, is(0)))
                    .andExpect(jsonPath("$.totalAtividadesAlteradas", is(0)))
                    .andExpect(jsonPath("$.totalCompetenciasImpactadas", is(0)))
                    .andExpect(jsonPath("$.atividadesInseridas", empty()))
                    .andExpect(jsonPath("$.atividadesRemovidas", empty()))
                    .andExpect(jsonPath("$.atividadesAlteradas", empty()))
                    .andExpect(jsonPath("$.competenciasImpactadas", empty()));
        }

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("Deve detectar atividades inseridas")
        void deveDetectarAtividadesInseridas() throws Exception {
            atividadeRepo.save(new Atividade(mapaSubprocesso, atividadeVigente1.getDescricao()));
            atividadeRepo.save(new Atividade(mapaSubprocesso, atividadeVigente2.getDescricao()));
            atividadeRepo.save(new Atividade(mapaSubprocesso, "Realizar auditorias internas."));

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TEM_IMPACTOS_JSON_PATH, is(true)))
                    .andExpect(jsonPath(TOTAL_ATIVIDADES_INSERIDAS_JSON_PATH, is(1)))
                    .andExpect(jsonPath("$.atividadesInseridas", hasSize(1)))
                    .andExpect(
                            jsonPath(
                                    "$.atividadesInseridas[0].descricao",
                                    is("Realizar auditorias internas.")))
                    .andExpect(jsonPath("$.atividadesInseridas[0].tipoImpacto", is("INSERIDA")));
        }

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("Deve detectar atividades removidas e as competências relacionadas")
        void deveDetectarAtividadesRemovidas() throws Exception {
            atividadeRepo.save(new Atividade(mapaSubprocesso, atividadeVigente1.getDescricao()));

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TEM_IMPACTOS_JSON_PATH, is(true)))
                    .andExpect(jsonPath(TOTAL_ATIVIDADES_REMOVIDAS_JSON_PATH, is(1)))
                    .andExpect(jsonPath("$.atividadesRemovidas", hasSize(1)))
                    .andExpect(
                            jsonPath(
                                    "$.atividadesRemovidas[0].descricao",
                                    is(atividadeVigente2.getDescricao())))
                    .andExpect(jsonPath("$.atividadesRemovidas[0].tipoImpacto", is("REMOVIDA")))
                    .andExpect(
                            jsonPath(
                                    "$.atividadesRemovidas[0].competenciasVinculadas",
                                    contains(competenciaVigente1.getDescricao())));
        }

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("Deve detectar atividades alteradas como uma remoção e uma inserção")
        void deveDetectarAtividadesAlteradas() throws Exception {
            Atividade atividadeAlterada =
                    new Atividade(
                            mapaSubprocesso, "Elaborar relatórios gerenciais e estratégicos.");
            atividadeRepo.save(atividadeAlterada);
            atividadeRepo.save(new Atividade(mapaSubprocesso, atividadeVigente1.getDescricao()));

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TEM_IMPACTOS_JSON_PATH, is(true)))
                    .andExpect(jsonPath(TOTAL_ATIVIDADES_INSERIDAS_JSON_PATH, is(1)))
                    .andExpect(jsonPath(TOTAL_ATIVIDADES_REMOVIDAS_JSON_PATH, is(1)))
                    .andExpect(jsonPath("$.totalAtividadesAlteradas", is(0)))
                    .andExpect(
                            jsonPath(
                                    "$.atividadesInseridas[0].descricao",
                                    is("Elaborar relatórios gerenciais e estratégicos.")))
                    .andExpect(
                            jsonPath(
                                    "$.atividadesRemovidas[0].descricao",
                                    is(atividadeVigente2.getDescricao())));
        }

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("Deve identificar competências impactadas por remoções e alterações")
        void deveIdentificarCompetenciasImpactadas() throws Exception {
            Atividade atividadeNova =
                    new Atividade(
                            mapaSubprocesso, "Elaborar relatórios gerenciais e estratégicos.");
            atividadeRepo.save(atividadeNova);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TEM_IMPACTOS_JSON_PATH, is(true)))
                    .andExpect(jsonPath(TOTAL_ATIVIDADES_REMOVIDAS_JSON_PATH, is(2)))
                    .andExpect(jsonPath(TOTAL_ATIVIDADES_INSERIDAS_JSON_PATH, is(1)))
                    .andExpect(jsonPath("$.totalCompetenciasImpactadas", is(1)))
                    .andExpect(jsonPath("$.competenciasImpactadas", hasSize(1)))
                    .andExpect(
                            jsonPath(
                                    "$.competenciasImpactadas[0].descricao",
                                    is(competenciaVigente1.getDescricao())))
                    .andExpect(
                            jsonPath("$.competenciasImpactadas[0].atividadesAfetadas", hasSize(2)))
                    .andExpect(
                            jsonPath(
                                    "$.competenciasImpactadas[0].atividadesAfetadas",
                                    containsInAnyOrder(
                                            "Atividade removida: "
                                                    + atividadeVigente1.getDescricao(),
                                            "Atividade removida: "
                                                    + atividadeVigente2.getDescricao())));
        }
    }

    @Nested
    @DisplayName("Cenários de Borda e Falhas")
    class BordaEfalhas {

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("Não deve detectar impactos se a unidade não possui mapa vigente")
        void semImpactos_QuandoNaoExisteMapaVigente() throws Exception {
            unidadeMapaRepo.deleteById(unidade.getCodigo());

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TEM_IMPACTOS_JSON_PATH, is(false)))
                    .andExpect(jsonPath(TOTAL_ATIVIDADES_INSERIDAS_JSON_PATH, is(0)))
                    .andExpect(jsonPath(TOTAL_ATIVIDADES_REMOVIDAS_JSON_PATH, is(0)))
                    .andExpect(jsonPath("$.totalAtividadesAlteradas", is(0)))
                    .andExpect(jsonPath("$.totalCompetenciasImpactadas", is(0)));
        }

        @Test
        @WithMockChefe(CHEFE_TITULO)
        @DisplayName("Deve retornar 404 para subprocesso inexistente")
        void deveRetornar404_QuandoSubprocessoNaoExiste() throws Exception {
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
            subprocessoRevisao.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            subprocessoRepo.save(subprocessoRevisao);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockGestor
        @DisplayName(
                "GESTOR pode acessar se subprocesso está em 'Revisão do cadastro disponibilizada'")
        void gestorPodeAcessar_EmRevisaoDisponibilizada() throws Exception {
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
        @DisplayName(
                "CHEFE NÃO pode acessar se subprocesso está em situação diferente de 'Revisão do"
                        + " cadastro em andamento'")
        void chefeNaoPodeAcessar_EmSituacaoIncorreta() throws Exception {
            subprocessoRevisao.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
            subprocessoRepo.save(subprocessoRevisao);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                    .andExpect(status().isForbidden());
        }
    }
}
