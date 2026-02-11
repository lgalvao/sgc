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
import sgc.integracao.mocks.TestLoginHelper;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.ConhecimentoRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.*;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-11: Visualizar cadastro de atividades e conhecimentos")
@Import({
        TestSecurityConfig.class,
        TestLoginHelper.class,
        UnidadeFixture.class,
        ProcessoFixture.class,
        SubprocessoFixture.class,
        MapaFixture.class,
        AtividadeFixture.class
})
class CDU11IntegrationTest extends BaseIntegrationTest {
    private static final String API_SUBPROCESSOS_ID_CADASTRO = "/api/subprocessos/{codigo}/cadastro";
    private static final String UNIDADE_SIGLA_JSON_PATH = "$.unidadeSigla";

    @Autowired
    private ConhecimentoRepo conhecimentoRepo;
    @Autowired
    private TestLoginHelper loginHelper;

    private Unidade unidade;
    private Processo processo;
    private Subprocesso subprocesso;
    private String tokenChefe;
    private String tokenGestor;
    private String tokenAdmin;
    private String tokenServidor;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
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

        // Unidade
        unidade = UnidadeFixture.unidadeComSigla("SENIC");
        unidade.setCodigo(null);
        unidade = unidadeRepo.save(unidade);

        // Assign users to this new unit so login works
        String sql = "INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)";
        // Chefe (User 3)
        jdbcTemplate.update(sql, "3", unidade.getCodigo(), Perfil.CHEFE.name());
        // Gestor (User 8 - Gestor)
        jdbcTemplate.update(sql, "8", unidade.getCodigo(), Perfil.GESTOR.name());
        // Servidor (User 1)
        jdbcTemplate.update(sql, "1", unidade.getCodigo(), Perfil.SERVIDOR.name());

        // Login real obtendo tokens JWT
        tokenChefe = loginHelper.loginChefe(mockMvc, "3", unidade.getCodigo());
        tokenGestor = loginHelper.loginGestor(mockMvc, "8", unidade.getCodigo());
        tokenAdmin = loginHelper.loginAdmin(mockMvc, "6");
        tokenServidor = loginHelper.loginServidor(mockMvc, "1", unidade.getCodigo());

        // Processo
        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setDescricao("Processo de Mapeamento");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);

        // Mapa e Subprocesso
        var mapa = mapaRepo.save(new Mapa());

        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setMapa(mapa);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocesso.setDataLimiteEtapa1(processo.getDataLimite());
        subprocesso = subprocessoRepo.save(subprocesso);

        // Atividades e Conhecimentos
        Atividade atividade1 = AtividadeFixture.atividadePadrao(mapa);
        atividade1.setDescricao("Analisar documentação");
        atividade1 = atividadeRepo.save(atividade1);

        Conhecimento conhecimento1 = Conhecimento.builder().atividade(atividade1)
                .descricao("Interpretação de textos técnicos").build();
        // Verificando uso de builder se construtor foi removido:
        // Conhecimento.builder().atividade(atividade1).descricao("...").build()
        conhecimentoRepo.save(conhecimento1);

        Atividade atividade2 = AtividadeFixture.atividadePadrao(mapa);
        atividade2.setDescricao("Elaborar relatórios");
        atividade2 = atividadeRepo.save(atividade2);

        Conhecimento conhecimento2a = Conhecimento.builder().atividade(atividade2).descricao("Escrita clara e concisa")
                .build();
        conhecimentoRepo.save(conhecimento2a);

        Conhecimento conhecimento2b = Conhecimento.builder().atividade(atividade2).descricao("Uso de planilhas")
                .build();
        conhecimentoRepo.save(conhecimento2b);
    }

    @Nested
    @DisplayName("Testes de Cenário de Sucesso")
    class Sucesso {
        @Test
        @DisplayName("Deve retornar o cadastro completo de atividades e conhecimentos para o Chefe da unidade")
        void deveRetornarCadastroCompleto_QuandoChefeDaUnidade() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocesso.getCodigo())
                            .header("Authorization", "Bearer " + tokenChefe))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subprocessoCodigo", is(subprocesso.getCodigo().intValue())))
                    .andExpect(jsonPath(UNIDADE_SIGLA_JSON_PATH, is("SENIC")))
                    .andExpect(jsonPath("$.atividades", hasSize(2)))
                    .andExpect(jsonPath("$.atividades[*].descricao",
                            containsInAnyOrder("Analisar documentação", "Elaborar relatórios")));
        }

        @Test
        @DisplayName("Deve permitir que ADMIN visualize o cadastro de qualquer unidade")
        void devePermitirAdminVisualizarCadastro() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocesso.getCodigo())
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(UNIDADE_SIGLA_JSON_PATH, is("SENIC")));
        }

        @Test
        @DisplayName("Deve permitir que GESTOR visualize o cadastro de qualquer unidade")
        void devePermitirGestorVisualizarCadastro() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocesso.getCodigo())
                            .header("Authorization", "Bearer " + tokenGestor))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(UNIDADE_SIGLA_JSON_PATH, is("SENIC")));
        }

        @Test
        @DisplayName("Deve permitir que SERVIDOR visualize o cadastro de qualquer unidade")
        void devePermitirServidorVisualizarCadastro() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocesso.getCodigo())
                            .header("Authorization", "Bearer " + tokenServidor))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(UNIDADE_SIGLA_JSON_PATH, is("SENIC")));
        }
    }

    @Nested
    @DisplayName("Testes de Casos de Borda e Falhas")
    class CasosDeBorda {
        @Test
        @DisplayName("Deve retornar 404 Not Found para um subprocesso inexistente")
        void deveRetornarNotFound_QuandoSubprocessoNaoExiste() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, 9999L)
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar uma lista de atividades vazia quando o mapa não tem atividades")
        void deveRetornarListaVazia_QuandoNaoHaAtividades() throws Exception {
            var mapaVazio = mapaRepo.save(new Mapa());
            Subprocesso subprocessoSemAtividades = SubprocessoFixture.subprocessoPadrao(processo, unidade);
            subprocessoSemAtividades.setCodigo(null);
            subprocessoSemAtividades.setMapa(mapaVazio);
            subprocessoSemAtividades = subprocessoRepo.save(subprocessoSemAtividades);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocessoSemAtividades.getCodigo())
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.subprocessoCodigo",
                                    is(subprocessoSemAtividades.getCodigo().intValue())))
                    .andExpect(jsonPath("$.atividades", hasSize(0)));
        }

        @Test
        @DisplayName("Deve retornar uma atividade com a lista de conhecimentos vazia")
        void deveRetornarAtividadeComConhecimentosVazios() throws Exception {
            Mapa mapaNovo = mapaRepo.save(new Mapa());

            Subprocesso subprocessoAtividadeSemConhecimento = SubprocessoFixture.subprocessoPadrao(processo, unidade);
            subprocessoAtividadeSemConhecimento.setCodigo(null);
            subprocessoAtividadeSemConhecimento.setMapa(mapaNovo);
            subprocessoAtividadeSemConhecimento = subprocessoRepo.save(subprocessoAtividadeSemConhecimento);

            Atividade atividadeSemConhecimento = AtividadeFixture.atividadePadrao(mapaNovo);
            atividadeSemConhecimento.setDescricao("Atividade sem conhecimento");
            atividadeRepo.save(atividadeSemConhecimento);

            mockMvc.perform(
                            get(API_SUBPROCESSOS_ID_CADASTRO, subprocessoAtividadeSemConhecimento.getCodigo())
                                    .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(
                            "$.subprocessoCodigo",
                            is(subprocessoAtividadeSemConhecimento.getCodigo().intValue())))
                    .andExpect(jsonPath("$.atividades", hasSize(1)))
                    .andExpect(
                            jsonPath("$.atividades[0].descricao", is("Atividade sem conhecimento")))
                    .andExpect(jsonPath("$.atividades[0].conhecimentos", hasSize(0)));
        }
    }
}
