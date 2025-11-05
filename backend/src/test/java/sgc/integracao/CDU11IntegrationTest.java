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
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockChefe;
import sgc.integracao.mocks.WithMockGestor;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

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
        unidade = unidadeRepo.findById(11L).orElseThrow(); // SENIC
        var chefe = usuarioRepo.findById(333333333333L).orElseThrow(); // Existing Chefe Teste

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
                    .andExpect(jsonPath(UNIDADE_SIGLA_JSON_PATH, is("SENIC")))
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
