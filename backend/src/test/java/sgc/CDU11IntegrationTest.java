package sgc;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import sgc.comum.enums.SituacaoProcesso;
import sgc.comum.enums.SituacaoSubprocesso;
import sgc.processo.enums.TipoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.Mapa;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.comum.modelo.UsuarioRepo;
import sgc.atividade.modelo.Atividade;
import sgc.conhecimento.modelo.Conhecimento;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-11: Visualizar cadastro de atividades e conhecimentos")
class CDU11IntegrationTest {
    private static final String API_SUBPROCESSOS_ID_CADASTRO = "/api/subprocessos/{id}/cadastro";
    private static final String UNIDADE_SIGLA_JSON_PATH = "$.unidadeSigla";
    private static final String ADMIN_ROLE = "ADMIN";

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

    // Test data
    private Unidade unidade;
    private Processo processo;
    private Subprocesso subprocesso;
    private Atividade atividade1;
    private Conhecimento conhecimento1;

    @BeforeEach
    void setUp() {
        // Unidade e Chefe
        unidade = new Unidade("Unidade Teste", "UT");
        var chefe = new sgc.comum.modelo.Usuario();
        chefe.setTitulo("chefe_ut");
        usuarioRepo.save(chefe);
        unidade.setTitular(chefe);
        unidadeRepo.save(unidade);

        // Processo
        processo = new Processo("Processo de Mapeamento", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDate.now().plusDays(30));
        processoRepo.save(processo);

        // Mapa e Subprocesso
        var mapa = mapaRepo.save(new Mapa());
        subprocesso = new Subprocesso(processo, unidade, mapa, SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO, processo.getDataLimite());
        subprocessoRepo.save(subprocesso);

        // Atividades e Conhecimentos
        atividade1 = new Atividade(mapa, "Analisar documentação");
        atividadeRepo.save(atividade1);
        conhecimento1 = new Conhecimento(atividade1, "Interpretação de textos técnicos");
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
        @WithMockUser(username = "chefe_ut", roles = {"CHEFE"})
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
        @WithMockUser(username = "admin", roles = {ADMIN_ROLE})
        @DisplayName("Deve permitir que ADMIN visualize o cadastro de qualquer unidade")
        void devePermitirAdminVisualizarCadastro() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(UNIDADE_SIGLA_JSON_PATH, is("UT")));
        }

        @Test
        @WithMockUser(username = "gestor", roles = {"GESTOR"})
        @DisplayName("Deve permitir que GESTOR visualize o cadastro de qualquer unidade")
        void devePermitirGestorVisualizarCadastro() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(UNIDADE_SIGLA_JSON_PATH, is("UT")));
        }

        @Test
        @WithMockUser(username = "servidor", roles = {"SERVIDOR"})
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
        @WithMockUser(roles = ADMIN_ROLE)
        @DisplayName("Deve retornar 404 Not Found para um subprocesso inexistente")
        void deveRetornarNotFound_QuandoSubprocessoNaoExiste() throws Exception {
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, 9999L))
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = ADMIN_ROLE)
        @DisplayName("Deve retornar uma lista de atividades vazia quando o mapa não tem atividades")
        void deveRetornarListaVazia_QuandoNaoHaAtividades() throws Exception {
            // Arrange
            Subprocesso subprocessoSemAtividades = new Subprocesso(processo, unidade, new Mapa(), SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO, processo.getDataLimite());
            mapaRepo.save(subprocessoSemAtividades.getMapa());
            subprocessoRepo.save(subprocessoSemAtividades);

            // Act & Assert
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocessoSemAtividades.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subprocessoId", is(subprocessoSemAtividades.getCodigo().intValue())))
                .andExpect(jsonPath("$.atividades", hasSize(0)));
        }

        @Test
        @WithMockUser(roles = ADMIN_ROLE)
        @DisplayName("Deve retornar uma atividade com a lista de conhecimentos vazia")
        void deveRetornarAtividadeComConhecimentosVazios() throws Exception {
            // Arrange
            Mapa mapaNovo = mapaRepo.save(new Mapa());
            Subprocesso subprocessoAtividadeSemConhecimento = new Subprocesso(processo, unidade, mapaNovo, SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO, processo.getDataLimite());
            subprocessoRepo.save(subprocessoAtividadeSemConhecimento);
            Atividade atividadeSemConhecimento = new Atividade(mapaNovo, "Atividade sem conhecimento");
            atividadeRepo.save(atividadeSemConhecimento);

            // Act & Assert
            mockMvc.perform(get(API_SUBPROCESSOS_ID_CADASTRO, subprocessoAtividadeSemConhecimento.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subprocessoId", is(subprocessoAtividadeSemConhecimento.getCodigo().intValue())))
                .andExpect(jsonPath("$.atividades", hasSize(1)))
                .andExpect(jsonPath("$.atividades[0].descricao", is("Atividade sem conhecimento")))
                .andExpect(jsonPath("$.atividades[0].conhecimentos", hasSize(0)));
        }
    }
}