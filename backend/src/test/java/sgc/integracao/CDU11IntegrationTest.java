package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockChefe;
import sgc.integracao.mocks.WithMockGestor;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.processo.modelo.SituacaoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.sgrh.modelo.Perfil;
import sgc.sgrh.modelo.Usuario;
import sgc.sgrh.modelo.UsuarioRepo;
import sgc.subprocesso.modelo.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-11: Visualizar cadastro de atividades e conhecimentos")
@Import(sgc.integracao.mocks.TestSecurityConfig.class)
class CDU11IntegrationTest {
    private static final String API_SUBPROCESSOS_ID_CADASTRO = "/api/subprocessos/{codigo}/cadastro";
    private static final String UNIDADE_SIGLA_JSON_PATH = "$.unidadeSigla";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProcessoRepo processoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private MapaRepo mapaRepo;
    @Autowired
    private AtividadeRepo atividadeRepo;
    @Autowired
    private ConhecimentoRepo conhecimentoRepo;
    @Autowired
    private UsuarioRepo usuarioRepo;

    // Test data
    private Unidade unidade;
    private Processo processo;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        // Unidade e Chefe
        unidade = new Unidade("Unidade Teste", "UT");
        var chefe = new Usuario();
        chefe.setTituloEleitoral(111122223333L);
        chefe.setPerfis(java.util.Set.of(Perfil.CHEFE));
        usuarioRepo.save(chefe);
        unidade.setTitular(chefe);
        unidadeRepo.save(unidade);

        // Processo
        processo = new Processo("Processo de Mapeamento", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDateTime.now().plusDays(30));
        processoRepo.save(processo);

        // Mapa e Subprocesso
        var mapa = mapaRepo.save(new Mapa());
        subprocesso = new Subprocesso(processo, unidade, mapa, SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO, processo.getDataLimite());
        subprocessoRepo.save(subprocesso);

        // Atividades e Conhecimentos
        Atividade atividade1 = new Atividade(mapa, "Analisar documentação");
        atividadeRepo.save(atividade1);
        Conhecimento conhecimento1 = new Conhecimento(atividade1, "Interpretação de textos técnicos");
        conhecimentoRepo.save(conhecimento1);

        Atividade atividade2 = new Atividade(mapa, "Elaborar relatórios");
        atividadeRepo.save(atividade2);
        Conhecimento conhecimento2a = new Conhecimento(atividade2, "Escrita clara e concisa");
        conhecimentoRepo.save(conhecimento2a);
        Conhecimento conhecimento2b = new Conhecimento(atividade2, "Uso de planilhas");
        conhecimentoRepo.save(conhecimento2b);
    }

    @Nested
    @DisplayName("Testes de Cenário de Sucesso")
    class Sucesso {

        @Test
        @WithMockChefe("111122223333")
        @DisplayName("Deve retornar o cadastro completo de atividades e conhecimentos para o Chefe da unidade")
        void deveRetornarCadastroCompleto_QuandoChefeDaUnidade() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocesso.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subprocessoId", is(subprocesso.getCodigo().intValue())))
                    .andExpect(jsonPath(UNIDADE_SIGLA_JSON_PATH, is("UT")))
                    .andExpect(jsonPath("$.atividades", hasSize(2)))
                    // Valida a primeira atividade e seu conhecimento
                    .andExpect(jsonPath("$.atividades[0].descricao", is("Analisar documentação")))
                    .andExpect(jsonPath("$.atividades[0].conhecimentos", hasSize(1)))
                    .andExpect(jsonPath("$.atividades[0].conhecimentos[0].descricao", is("Interpretação de textos técnicos")))
                    // Valida a segunda atividade e seus conhecimentos
                    .andExpect(jsonPath("$.atividades[1].descricao", is("Elaborar relatórios")))
                    .andExpect(jsonPath("$.atividades[1].conhecimentos", hasSize(2)))
                    .andExpect(jsonPath("$.atividades[1].conhecimentos[*].descricao",
                            containsInAnyOrder("Escrita clara e concisa", "Uso de planilhas")));
        }

        @Test
        @WithMockAdmin
        @DisplayName("Deve permitir que ADMIN visualize o cadastro de qualquer unidade")
        void devePermitirAdminVisualizarCadastro() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocesso.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(UNIDADE_SIGLA_JSON_PATH, is("UT")));
        }

        @Test
        @WithMockGestor
        @DisplayName("Deve permitir que GESTOR visualize o cadastro de qualquer unidade")
        void devePermitirGestorVisualizarCadastro() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocesso.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(UNIDADE_SIGLA_JSON_PATH, is("UT")));
        }

        @Test
        @WithMockUser
        @DisplayName("Deve permitir que SERVIDOR visualize o cadastro de qualquer unidade")
        void devePermitirServidorVisualizarCadastro() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocesso.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(UNIDADE_SIGLA_JSON_PATH, is("UT")));
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
            Subprocesso subprocessoSemAtividades = new Subprocesso(processo, unidade, new Mapa(), SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO, processo.getDataLimite());
            mapaRepo.save(subprocessoSemAtividades.getMapa());
            subprocessoRepo.save(subprocessoSemAtividades);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocessoSemAtividades.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subprocessoId", is(subprocessoSemAtividades.getCodigo().intValue())))
                    .andExpect(jsonPath("$.atividades", hasSize(0)));
        }

        @Test
        @WithMockAdmin
        @DisplayName("Deve retornar uma atividade com a lista de conhecimentos vazia")
        void deveRetornarAtividadeComConhecimentosVazios() throws Exception {
            Mapa mapaNovo = mapaRepo.save(new Mapa());
            Subprocesso subprocessoAtividadeSemConhecimento = new Subprocesso(processo, unidade, mapaNovo, SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO, processo.getDataLimite());
            subprocessoRepo.save(subprocessoAtividadeSemConhecimento);
            Atividade atividadeSemConhecimento = new Atividade(mapaNovo, "Atividade sem conhecimento");
            atividadeRepo.save(atividadeSemConhecimento);

            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocessoAtividadeSemConhecimento.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subprocessoId", is(subprocessoAtividadeSemConhecimento.getCodigo().intValue())))
                    .andExpect(jsonPath("$.atividades", hasSize(1)))
                    .andExpect(jsonPath("$.atividades[0].descricao", is("Atividade sem conhecimento")))
                    .andExpect(jsonPath("$.atividades[0].conhecimentos", hasSize(0)));
        }
    }
}