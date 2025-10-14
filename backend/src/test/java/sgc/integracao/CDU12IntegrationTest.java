package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividade.Id;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.integracao.mocks.*;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapa;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.processo.SituacaoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.sgrh.Perfil;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import({TestSecurityConfig.class, WithMockChefeSecurityContextFactory.class, WithMockAdminSecurityContextFactory.class, WithMockGestorSecurityContextFactory.class})
@DisplayName("CDU-12: Verificar impactos no mapa de competências")
class CDU12IntegrationTest {

    private static final String API_SUBPROCESSOS_ID_IMPACTOS_MAPA = "/api/subprocessos/{id}/impactos-mapa";
    private static final String CHEFE_UT_USERNAME = "121212121212";
    private static final String TEM_IMPACTOS_JSON_PATH = "$.temImpactos";
    private static final String TOTAL_ATIVIDADES_INSERIDAS_JSON_PATH = "$.totalAtividadesInseridas";
    private static final String TOTAL_ATIVIDADES_REMOVIDAS_JSON_PATH = "$.totalAtividadesRemovidas";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Repositories
    @Autowired private ProcessoRepo processoRepo;
    @Autowired private UnidadeRepo unidadeRepo;
    @Autowired private SubprocessoRepo subprocessoRepo;
    @Autowired private MapaRepo mapaRepo;
    @Autowired private AtividadeRepo atividadeRepo;
    @Autowired private ConhecimentoRepo conhecimentoRepo;
    @Autowired private UsuarioRepo usuarioRepo;
    @Autowired private CompetenciaRepo competenciaRepo;
    @Autowired private CompetenciaAtividadeRepo competenciaAtividadeRepo;
    @Autowired private UnidadeMapaRepo unidadeMapaRepo;

    private Atividade atividadeVigente1;
    private Atividade atividadeVigente2;
    private Competencia competenciaVigente1;
    private Subprocesso subprocessoRevisao;
    private Mapa mapaSubprocesso;


    @BeforeEach
    void setUp() {
        // 1. Unidade e Chefe
        // Test data
        Unidade unidade = new Unidade("Unidade de Teste", "UT");
        Usuario chefe = new Usuario();
        chefe.setTituloEleitoral(Long.parseLong(CHEFE_UT_USERNAME));
        chefe.setPerfis(java.util.Set.of(Perfil.CHEFE));
        usuarioRepo.save(chefe);
        unidade.setTitular(chefe);
        unidadeRepo.save(unidade);

        Usuario gestor = new Usuario();
        gestor.setTituloEleitoral(222222222222L);
        gestor.setPerfis(java.util.Set.of(Perfil.GESTOR));
        usuarioRepo.save(gestor);

        Usuario admin = new Usuario();
        admin.setTituloEleitoral(111111111111L);
        admin.setPerfis(java.util.Set.of(Perfil.ADMIN));
        usuarioRepo.save(admin);

        // 2. Processo de Revisão
        Processo processoRevisao = new Processo(
                "Processo de Revisão 2024",
                TipoProcesso.REVISAO,
                SituacaoProcesso.EM_ANDAMENTO,
                LocalDate.now().plusMonths(3)
        );
        processoRepo.save(processoRevisao);

        // 3. Mapa Vigente da Unidade
        Mapa mapaVigente = new Mapa();
        mapaVigente.setDataHoraHomologado(LocalDateTime.now().minusMonths(6));
        mapaRepo.save(mapaVigente);
        UnidadeMapa unidadeMapa = new UnidadeMapa(unidade.getCodigo());
        unidadeMapa.setMapaVigente(mapaVigente);
        unidadeMapa.setMapaVigenteCodigo(mapaVigente.getCodigo());
        unidadeMapaRepo.save(unidadeMapa);


        // 4. Atividades e Competências no Mapa Vigente
        atividadeVigente1 = atividadeRepo.save(new Atividade(null, "Analisar e despachar processos."));
        atividadeVigente2 = atividadeRepo.save(new Atividade(null, "Elaborar relatórios gerenciais."));

        competenciaVigente1 = competenciaRepo.save(new Competencia(mapaVigente, "Gerenciamento de Processos"));

        vincularAtividadeCompetencia(competenciaVigente1, atividadeVigente1);
        vincularAtividadeCompetencia(competenciaVigente1, atividadeVigente2);

        // 5. Subprocesso de Revisão
        mapaSubprocesso = mapaRepo.save(new Mapa());
        subprocessoRevisao = new Subprocesso(
                processoRevisao,
                unidade,
            mapaSubprocesso,
            SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
            processoRevisao.getDataLimite()
        );
        subprocessoRepo.save(subprocessoRevisao);
    }

    private void vincularAtividadeCompetencia(Competencia competencia, Atividade atividade) {
        Id id = new Id(atividade.getCodigo(), competencia.getCodigo());
        CompetenciaAtividade vinculo = new CompetenciaAtividade(id, atividade, competencia);
        competenciaAtividadeRepo.save(vinculo);
    }

    @Nested
    @DisplayName("Cenários de Sucesso")
    class Sucesso {

        @Test
        @WithMockChefe(CHEFE_UT_USERNAME)
        @DisplayName("Não deve detectar impactos quando o cadastro de atividades é idêntico ao mapa vigente")
        void semImpactos_QuandoCadastroIdentico() throws Exception {
            // Arrange: Copia as atividades do mapa vigente para o mapa do subprocesso
            atividadeRepo.save(new Atividade(mapaSubprocesso, atividadeVigente1.getDescricao()));
            atividadeRepo.save(new Atividade(mapaSubprocesso, atividadeVigente2.getDescricao()));

            // Act & Assert
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
        @WithMockChefe(CHEFE_UT_USERNAME)
        @DisplayName("Deve detectar atividades inseridas")
        void deveDetectarAtividadesInseridas() throws Exception {
            // Arrange: Mantém as atividades vigentes e adiciona uma nova
            atividadeRepo.save(new Atividade(mapaSubprocesso, atividadeVigente1.getDescricao()));
            atividadeRepo.save(new Atividade(mapaSubprocesso, atividadeVigente2.getDescricao()));
            atividadeRepo.save(new Atividade(mapaSubprocesso, "Realizar auditorias internas."));

            // Act & Assert
            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(TEM_IMPACTOS_JSON_PATH, is(true)))
                .andExpect(jsonPath(TOTAL_ATIVIDADES_INSERIDAS_JSON_PATH, is(1)))
                .andExpect(jsonPath("$.atividadesInseridas", hasSize(1)))
                .andExpect(jsonPath("$.atividadesInseridas[0].descricao", is("Realizar auditorias internas.")))
                .andExpect(jsonPath("$.atividadesInseridas[0].tipoImpacto", is("INSERIDA")));
        }

        @Test
        @WithMockChefe(CHEFE_UT_USERNAME)
        @DisplayName("Deve detectar atividades removidas e as competências relacionadas")
        void deveDetectarAtividadesRemovidas() throws Exception {
            // Arrange: Adiciona apenas uma das atividades vigentes, efetivamente removendo a outra.
            atividadeRepo.save(new Atividade(mapaSubprocesso, atividadeVigente1.getDescricao()));

            // Act & Assert
            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(TEM_IMPACTOS_JSON_PATH, is(true)))
                .andExpect(jsonPath(TOTAL_ATIVIDADES_REMOVIDAS_JSON_PATH, is(1)))
                .andExpect(jsonPath("$.atividadesRemovidas", hasSize(1)))
                .andExpect(jsonPath("$.atividadesRemovidas[0].descricao", is(atividadeVigente2.getDescricao())))
                .andExpect(jsonPath("$.atividadesRemovidas[0].tipoImpacto", is("REMOVIDA")))
                .andExpect(jsonPath("$.atividadesRemovidas[0].competenciasVinculadas", contains(competenciaVigente1.getDescricao())));
        }

        @Test
        @WithMockChefe(CHEFE_UT_USERNAME)
        @DisplayName("Deve detectar atividades alteradas como uma remoção e uma inserção")
        void deveDetectarAtividadesAlteradas() throws Exception {
            // Arrange: Cria uma atividade com descrição diferente.
            Atividade atividadeAlterada = new Atividade(mapaSubprocesso, "Elaborar relatórios gerenciais e estratégicos.");
            atividadeRepo.save(atividadeAlterada);
            // Mantém a outra atividade inalterada
            atividadeRepo.save(new Atividade(mapaSubprocesso, atividadeVigente1.getDescricao()));


            // Act & Assert
            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(TEM_IMPACTOS_JSON_PATH, is(true)))
                .andExpect(jsonPath(TOTAL_ATIVIDADES_INSERIDAS_JSON_PATH, is(1)))
                .andExpect(jsonPath(TOTAL_ATIVIDADES_REMOVIDAS_JSON_PATH, is(1)))
                .andExpect(jsonPath("$.totalAtividadesAlteradas", is(0)))
                .andExpect(jsonPath("$.atividadesInseridas[0].descricao", is("Elaborar relatórios gerenciais e estratégicos.")))
                .andExpect(jsonPath("$.atividadesRemovidas[0].descricao", is(atividadeVigente2.getDescricao())));
        }

        @Test
        @WithMockChefe(CHEFE_UT_USERNAME)
        @DisplayName("Deve identificar competências impactadas por remoções e alterações")
        void deveIdentificarCompetenciasImpactadas() throws Exception {
            // Arrange:
            // 1. Remove a "atividadeVigente1"
            // 2. Altera a "atividadeVigente2" (removendo a antiga e inserindo a nova)
            Atividade atividadeNova = new Atividade(mapaSubprocesso, "Elaborar relatórios gerenciais e estratégicos.");
            atividadeRepo.save(atividadeNova);

            // Act & Assert
            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(TEM_IMPACTOS_JSON_PATH, is(true)))
                .andExpect(jsonPath(TOTAL_ATIVIDADES_REMOVIDAS_JSON_PATH, is(2))) // atividadeVigente1 e atividadeVigente2
                .andExpect(jsonPath(TOTAL_ATIVIDADES_INSERIDAS_JSON_PATH, is(1)))
                .andExpect(jsonPath("$.totalCompetenciasImpactadas", is(1)))
                .andExpect(jsonPath("$.competenciasImpactadas", hasSize(1)))
                .andExpect(jsonPath("$.competenciasImpactadas[0].descricao", is(competenciaVigente1.getDescricao())))
                .andExpect(jsonPath("$.competenciasImpactadas[0].atividadesAfetadas", hasSize(2)))
                .andExpect(jsonPath("$.competenciasImpactadas[0].atividadesAfetadas",
                    containsInAnyOrder(
                        "Atividade removida: " + atividadeVigente1.getDescricao(),
                        "Atividade removida: " + atividadeVigente2.getDescricao()
                    )
                ));
        }
    }

    @Nested
    @DisplayName("Cenários de Borda e Falhas")
    class BordaEfalhas {

        @Test
        @WithMockChefe(CHEFE_UT_USERNAME)
        @DisplayName("Não deve detectar impactos se a unidade não possui mapa vigente")
        void semImpactos_QuandoNaoExisteMapaVigente() throws Exception {
            // Arrange
            unidadeMapaRepo.deleteAll();

            // Act & Assert
            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(TEM_IMPACTOS_JSON_PATH, is(false)))
                .andExpect(jsonPath(TOTAL_ATIVIDADES_INSERIDAS_JSON_PATH, is(0)))
                .andExpect(jsonPath(TOTAL_ATIVIDADES_REMOVIDAS_JSON_PATH, is(0)))
                .andExpect(jsonPath("$.totalAtividadesAlteradas", is(0)))
                .andExpect(jsonPath("$.totalCompetenciasImpactadas", is(0)));
        }

        @Test
        @WithMockChefe(CHEFE_UT_USERNAME)
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
        @WithMockChefe(CHEFE_UT_USERNAME)
        @DisplayName("CHEFE pode acessar se subprocesso está em 'Revisão do cadastro em andamento'")
        void chefePodeAcessar_EmRevisaoCadastro() throws Exception {
            subprocessoRevisao.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            subprocessoRepo.save(subprocessoRevisao);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockGestor
        @DisplayName("GESTOR pode acessar se subprocesso está em 'Revisão do cadastro disponibilizada'")
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
            subprocessoRevisao.setSituacao(SituacaoSubprocesso.MAPA_AJUSTADO);
            subprocessoRepo.save(subprocessoRevisao);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockChefe(CHEFE_UT_USERNAME)
        @DisplayName("CHEFE NÃO pode acessar se subprocesso está em situação diferente de 'Revisão do cadastro em andamento'")
        void chefeNaoPodeAcessar_EmSituacaoIncorreta() throws Exception {
            subprocessoRevisao.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
            subprocessoRepo.save(subprocessoRevisao);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_IMPACTOS_MAPA, subprocessoRevisao.getCodigo()))
                .andExpect(status().isForbidden());
        }
    }
}