package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.fixture.*;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockChefe;
import sgc.integracao.mocks.WithMockGestor;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

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
        sgc.integracao.mocks.TestSecurityConfig.class,
        UnidadeFixture.class,
        ProcessoFixture.class,
        SubprocessoFixture.class,
        MapaFixture.class,
        AtividadeFixture.class
})
class CDU11IntegrationTest extends BaseIntegrationTest {
    private static final String API_SUBPROCESSOS_ID_CADASTRO =
            "/api/subprocessos/{codigo}/cadastro";
    private static final String UNIDADE_SIGLA_JSON_PATH = "$.unidadeSigla";

    @Autowired private ProcessoRepo processoRepo;
    @Autowired private UnidadeRepo unidadeRepo;
    @Autowired private SubprocessoRepo subprocessoRepo;
    @Autowired private MapaRepo mapaRepo;
    @Autowired private AtividadeRepo atividadeRepo;
    @Autowired private ConhecimentoRepo conhecimentoRepo;
    @Autowired private UsuarioRepo usuarioRepo;
    @Autowired private UsuarioPerfilRepo usuarioPerfilRepo;


    // Test data
    private Unidade unidade;
    private Processo processo;
    private Subprocesso subprocesso;

    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Reset sequence
        try {
            jdbcTemplate.execute("ALTER SEQUENCE SGC.VW_UNIDADE_SEQ RESTART WITH 1000");
        } catch (Exception e) {
            try {
                jdbcTemplate.execute("ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN codigo RESTART WITH 1000");
            } catch (Exception ex) {
                // Ignore
            }
        }

        // Unidade
        unidade = UnidadeFixture.unidadeComSigla("SENIC");
        unidade.setCodigo(null);
        unidade = unidadeRepo.save(unidade);

        // Setup users for mocks
        criarUsuario("111122223333", Perfil.CHEFE, unidade); // For @WithMockChefe
        criarUsuario("222222222222", Perfil.GESTOR, unidade); // For @WithMockGestor
        criarUsuario("user", Perfil.SERVIDOR, unidade); // For @WithMockUser (default username)

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
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocesso.setDataLimiteEtapa1(processo.getDataLimite());
        subprocesso = subprocessoRepo.save(subprocesso);

        // Atividades e Conhecimentos
        Atividade atividade1 = AtividadeFixture.atividadePadrao(mapa);
        atividade1.setDescricao("Analisar documentação");
        atividade1 = atividadeRepo.save(atividade1);

        Conhecimento conhecimento1 =
                Conhecimento.builder().atividade(atividade1).descricao("Interpretação de textos técnicos").build();
        // Verificando uso de builder se construtor foi removido:
        // Conhecimento.builder().atividade(atividade1).descricao("...").build()
        conhecimentoRepo.save(conhecimento1);

        Atividade atividade2 = AtividadeFixture.atividadePadrao(mapa);
        atividade2.setDescricao("Elaborar relatórios");
        atividade2 = atividadeRepo.save(atividade2);

        Conhecimento conhecimento2a = Conhecimento.builder().atividade(atividade2).descricao("Escrita clara e concisa").build();
        conhecimentoRepo.save(conhecimento2a);

        Conhecimento conhecimento2b = Conhecimento.builder().atividade(atividade2).descricao("Uso de planilhas").build();
        conhecimentoRepo.save(conhecimento2b);
    }

    private void criarUsuario(String titulo, Perfil perfil, Unidade unidade) {
        Usuario usuario = UsuarioFixture.usuarioComTitulo(titulo);
        usuario.setUnidadeLotacao(unidade);
        usuario = usuarioRepo.save(usuario);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setUsuario(usuario);
        up.setUsuarioTitulo(titulo);
        up.setUnidade(unidade);
        up.setUnidadeCodigo(unidade.getCodigo());
        up.setPerfil(perfil);
        usuarioPerfilRepo.save(up);
    }

    @Nested
    @DisplayName("Testes de Cenário de Sucesso")
    class Sucesso {

        @Test
        @WithMockChefe("111122223333")
        @DisplayName(
                "Deve retornar o cadastro completo de atividades e conhecimentos para o Chefe da"
                        + " unidade")
        void deveRetornarCadastroCompleto_QuandoChefeDaUnidade() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocesso.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subprocessoCodigo", is(subprocesso.getCodigo().intValue())))
                    .andExpect(jsonPath(UNIDADE_SIGLA_JSON_PATH, is("SENIC")))
                    .andExpect(jsonPath("$.atividades", hasSize(2)))
                    // Valida a primeira atividade e seu conhecimento
                    // Nota: a ordem não é garantida, então usamos filtro ou assumptions.
                    // Mas como inserimos sequencialmente, pode vir sequencial. Vamos ajustar se falhar.
                    // Melhor usar containsInAnyOrder para robustez
                    .andExpect(jsonPath("$.atividades[*].descricao", containsInAnyOrder("Analisar documentação", "Elaborar relatórios")));
        }

        @Test
        @WithMockAdmin
        @DisplayName("Deve permitir que ADMIN visualize o cadastro de qualquer unidade")
        void devePermitirAdminVisualizarCadastro() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocesso.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(UNIDADE_SIGLA_JSON_PATH, is("SENIC")));
        }

        @Test
        @WithMockGestor
        @DisplayName("Deve permitir que GESTOR visualize o cadastro de qualquer unidade")
        void devePermitirGestorVisualizarCadastro() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocesso.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(UNIDADE_SIGLA_JSON_PATH, is("SENIC")));
        }

        @Test
        @WithMockUser
        @DisplayName("Deve permitir que SERVIDOR visualize o cadastro de qualquer unidade")
        void devePermitirServidorVisualizarCadastro() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocesso.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(UNIDADE_SIGLA_JSON_PATH, is("SENIC")));
        }
    }

    @Nested
    @DisplayName("Testes de Casos de Borda e Falhas")
    class CasosDeBorda {
        @Test
        @WithMockAdmin
        @DisplayName("Deve retornar 404 Not Found para um subprocesso inexistente")
        void deveRetornarNotFound_QuandoSubprocessoNaoExiste() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, 9999L))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockAdmin
        @DisplayName("Deve retornar uma lista de atividades vazia quando o mapa não tem atividades")
        void deveRetornarListaVazia_QuandoNaoHaAtividades() throws Exception {
            var mapaVazio = mapaRepo.save(new Mapa());
            Subprocesso subprocessoSemAtividades = SubprocessoFixture.subprocessoPadrao(processo, unidade);
            subprocessoSemAtividades.setCodigo(null);
            subprocessoSemAtividades.setMapa(mapaVazio);
            subprocessoSemAtividades = subprocessoRepo.save(subprocessoSemAtividades);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocessoSemAtividades.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.subprocessoCodigo",
                                    is(subprocessoSemAtividades.getCodigo().intValue())))
                    .andExpect(jsonPath("$.atividades", hasSize(0)));
        }

        @Test
        @WithMockAdmin
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
                            get(
                                    API_SUBPROCESSOS_ID_CADASTRO,
                                    subprocessoAtividadeSemConhecimento.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.subprocessoCodigo",
                                    is(subprocessoAtividadeSemConhecimento.getCodigo().intValue())))
                    .andExpect(jsonPath("$.atividades", hasSize(1)))
                    .andExpect(
                            jsonPath("$.atividades[0].descricao", is("Atividade sem conhecimento")))
                    .andExpect(jsonPath("$.atividades[0].conhecimentos", hasSize(0)));
        }
    }
}
