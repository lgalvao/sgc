package sgc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.enums.SituacaoProcesso;
import sgc.comum.enums.SituacaoSubprocesso;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.processo.enums.TipoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "chefe", roles = {"CHEFE"})
@Import(TestSecurityConfig.class)
@Transactional
@DisplayName("CDU-08: Manter Cadastro de Atividades e Conhecimentos")
class CDU08IntegrationTest {

    private static final String API_ATIVIDADES = "/api/atividades";
    private static final String API_CONHECIMENTOS = "/api/conhecimentos";
    private static final String API_ATIVIDADES_ID = "/api/atividades/{id}";
    private static final String API_CONHECIMENTOS_ID = "/api/conhecimentos/{id}";
    private static final String DESCRICAO_JSON_PATH = "$.descricao";

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Repositories
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
    private sgc.comum.modelo.UsuarioRepo usuarioRepo;

    // Test data
    private Unidade unidadeChefe;
    private Processo processoMapeamento;
    private Subprocesso subprocessoMapeamento;
    private Mapa mapaMapeamento;

    private Processo processoRevisao;
    private Subprocesso subprocessoRevisao;
    private Mapa mapaRevisao;

    @BeforeEach
    void setUp() {
        // Common data
        var chefe = new sgc.comum.modelo.Usuario();
        chefe.setTitulo("chefe");
        chefe = usuarioRepo.save(chefe);
        unidadeChefe = new Unidade("Unidade Teste", "UT");
        unidadeChefe.setTitular(chefe);
        unidadeRepo.save(unidadeChefe);

        // Data for Mapeamento process
        processoMapeamento = new Processo("Processo de Mapeamento", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDate.now().plusDays(30));
        processoRepo.save(processoMapeamento);

        mapaMapeamento = mapaRepo.save(new Mapa());
        subprocessoMapeamento = new Subprocesso(processoMapeamento, unidadeChefe, mapaMapeamento, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, processoMapeamento.getDataLimite());
        subprocessoRepo.save(subprocessoMapeamento);

        // Data for Revisão process
        processoRevisao = new Processo("Processo de Revisão", TipoProcesso.REVISAO, SituacaoProcesso.EM_ANDAMENTO, LocalDate.now().plusDays(30));
        processoRepo.save(processoRevisao);

        mapaRevisao = mapaRepo.save(new Mapa());
        subprocessoRevisao = new Subprocesso(processoRevisao, unidadeChefe, mapaRevisao, SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, processoRevisao.getDataLimite());
        subprocessoRepo.save(subprocessoRevisao);
    }

    @Nested
    @DisplayName("Testes de Criação (POST)")
    class Criacao {

        @Test
        @DisplayName("Deve adicionar uma nova atividade ao mapa do subprocesso")
        void deveAdicionarNovaAtividade() throws Exception {
            String novaAtividadeJson = String.format("{\"descricao\": \"Nova Atividade de Teste\", \"mapaCodigo\": %d}", mapaMapeamento.getCodigo());

            mockMvc.perform(post(API_ATIVIDADES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(novaAtividadeJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath(DESCRICAO_JSON_PATH, is("Nova Atividade de Teste")));

            List<Atividade> atividades = atividadeRepo.findByMapaCodigo(mapaMapeamento.getCodigo());
            assertThat(atividades).hasSize(1);
            assertThat(atividades.get(0).getDescricao()).isEqualTo("Nova Atividade de Teste");
        }

        @Test
        @DisplayName("Deve adicionar um novo conhecimento a uma atividade existente")
        void deveAdicionarNovoConhecimento() throws Exception {
            Atividade atividade = atividadeRepo.save(new Atividade(mapaMapeamento, "Atividade para Conhecimento"));
            String novoConhecimentoJson = String.format("{\"descricao\": \"Novo Conhecimento de Teste\", \"atividadeCodigo\": %d}", atividade.getCodigo());

            mockMvc.perform(post(API_CONHECIMENTOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(novoConhecimentoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath(DESCRICAO_JSON_PATH, is("Novo Conhecimento de Teste")));

            List<Conhecimento> conhecimentos = conhecimentoRepo.findByAtividadeCodigo(atividade.getCodigo());
            assertThat(conhecimentos).hasSize(1);
            assertThat(conhecimentos.get(0).getDescricao()).isEqualTo("Novo Conhecimento de Teste");
        }
    }

    @Nested
    @DisplayName("Testes de Edição (PUT)")
    class Edicao {

        @Test
        @DisplayName("Deve editar a descrição de uma atividade existente")
        void deveEditarAtividade() throws Exception {
            Atividade atividade = atividadeRepo.save(new Atividade(mapaMapeamento, "Atividade Original"));
            String atividadeEditadaJson = "{\"descricao\": \"Atividade Editada\"}";

            mockMvc.perform(put(API_ATIVIDADES_ID, atividade.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(atividadeEditadaJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DESCRICAO_JSON_PATH, is("Atividade Editada")));

            Atividade atividadeDoBanco = atividadeRepo.findById(atividade.getCodigo()).orElseThrow();
            assertThat(atividadeDoBanco.getDescricao()).isEqualTo("Atividade Editada");
        }

        @Test
        @DisplayName("Deve editar a descrição de um conhecimento existente")
        void deveEditarConhecimento() throws Exception {
            Atividade atividade = atividadeRepo.save(new Atividade(mapaMapeamento, "Atividade para Edição de Conhecimento"));
            Conhecimento conhecimento = conhecimentoRepo.save(new Conhecimento(atividade, "Conhecimento Original"));
            String conhecimentoEditadoJson = String.format(
                "{\"descricao\": \"Conhecimento Editado\", \"atividadeCodigo\": %d}",
                atividade.getCodigo()
            );

            mockMvc.perform(put(API_CONHECIMENTOS_ID, conhecimento.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(conhecimentoEditadoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DESCRICAO_JSON_PATH, is("Conhecimento Editado")));

            Conhecimento conhecimentoDoBanco = conhecimentoRepo.findById(conhecimento.getCodigo()).orElseThrow();
            assertThat(conhecimentoDoBanco.getDescricao()).isEqualTo("Conhecimento Editado");
        }
    }

    @Nested
    @DisplayName("Testes de Remoção (DELETE)")
    class Remocao {

        @Test
        @DisplayName("Deve remover um conhecimento de uma atividade")
        void deveRemoverConhecimento() throws Exception {
            Atividade atividade = atividadeRepo.save(new Atividade(mapaMapeamento, "Atividade para Remoção de Conhecimento"));
            Conhecimento conhecimento = conhecimentoRepo.save(new Conhecimento(atividade, "Conhecimento a ser Removido"));

            mockMvc.perform(delete(API_CONHECIMENTOS_ID, conhecimento.getCodigo()))
                .andExpect(status().isNoContent());

            assertThat(conhecimentoRepo.findById(conhecimento.getCodigo())).isEmpty();
        }

        @Test
        @DisplayName("Deve remover uma atividade e todos os seus conhecimentos associados")
        void deveRemoverAtividadeEConhecimentos() throws Exception {
            Atividade atividade = atividadeRepo.save(new Atividade(mapaMapeamento, "Atividade para Remoção Completa"));
            Conhecimento conhecimento1 = conhecimentoRepo.save(new Conhecimento(atividade, "Conhecimento 1"));
            Conhecimento conhecimento2 = conhecimentoRepo.save(new Conhecimento(atividade, "Conhecimento 2"));

            mockMvc.perform(delete(API_ATIVIDADES_ID, atividade.getCodigo()))
                .andExpect(status().isNoContent());

            assertThat(atividadeRepo.findById(atividade.getCodigo())).isEmpty();
            assertThat(conhecimentoRepo.findById(conhecimento1.getCodigo())).isEmpty();
            assertThat(conhecimentoRepo.findById(conhecimento2.getCodigo())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Importação")
    class Importacao {

        private Processo processoFonte;
        private Unidade unidadeFonte;
        private Mapa mapaFonte;
        private Atividade atividadeFonte1;
        private Atividade atividadeFonte2;

        @BeforeEach
        void setUp() {
            unidadeFonte = unidadeRepo.save(new Unidade("Unidade Fonte", "UF"));
            processoFonte = new Processo("Processo Fonte Finalizado", TipoProcesso.MAPEAMENTO, SituacaoProcesso.FINALIZADO, LocalDate.now().minusDays(10));
            processoRepo.save(processoFonte);

            mapaFonte = mapaRepo.save(new Mapa());
            Subprocesso subprocessoFonte = new Subprocesso(processoFonte, unidadeFonte, mapaFonte, SituacaoSubprocesso.MAPA_HOMOLOGADO, processoFonte.getDataLimite());
            subprocessoRepo.save(subprocessoFonte);

            atividadeFonte1 = new Atividade(mapaFonte, "Atividade Fonte 1");
            atividadeRepo.save(atividadeFonte1);
            conhecimentoRepo.save(new Conhecimento(atividadeFonte1, "Conhecimento Fonte 1.1"));

            atividadeFonte2 = new Atividade(mapaFonte, "Atividade Fonte 2");
            atividadeRepo.save(atividadeFonte2);

            // Add a pre-existing activity to the target map to test the non-import rule
            atividadeRepo.save(new Atividade(mapaMapeamento, "Atividade Fonte 2"));
        }

        @Test
        @DisplayName("Deve importar atividades e conhecimentos de outro mapa finalizado")
        void deveImportarAtividadesEConhecimentos() throws Exception {
            // Corrigido para usar o ID do subprocesso de origem, não do processo.
            Subprocesso subprocessoFonte = subprocessoRepo.findByMapaCodigo(mapaFonte.getCodigo())
                .orElseThrow(() -> new IllegalStateException("Subprocesso fonte não encontrado para o mapa " + mapaFonte.getCodigo()));

            String importRequestJson = String.format("{\"subprocessoOrigemId\": %d}", subprocessoFonte.getCodigo());

            mockMvc.perform(post("/api/subprocessos/{id}/importar-atividades", subprocessoMapeamento.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(importRequestJson))
                .andExpect(status().isOk());

            // Verify that only the non-existing activity was imported
            List<Atividade> atividadesImportadas = atividadeRepo.findByMapaCodigo(mapaMapeamento.getCodigo());
            assertThat(atividadesImportadas).hasSize(2); // "Atividade Fonte 2" (pre-existing) + "Atividade Fonte 1" (imported)

            Atividade atividadeImportada = atividadesImportadas.stream()
                .filter(a -> a.getDescricao().equals("Atividade Fonte 1"))
                .findFirst().orElseThrow();

            assertThat(atividadeImportada.getDescricao()).isEqualTo("Atividade Fonte 1");
            List<Conhecimento> conhecimentosImportados = conhecimentoRepo.findByAtividadeCodigo(atividadeImportada.getCodigo());
            assertThat(conhecimentosImportados).hasSize(1);
            assertThat(conhecimentosImportados.get(0).getDescricao()).isEqualTo("Conhecimento Fonte 1.1");
        }
    }

    @Nested
    @DisplayName("Testes de Segurança e Validação")
    class SegurancaEValidacao {

        @Test
        @DisplayName("Não deve adicionar atividade se subprocesso estiver em estado final")
        void naoDeveAdicionarAtividadeSubprocessoFinalizado() throws Exception {
            subprocessoMapeamento.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
            subprocessoRepo.save(subprocessoMapeamento);

            String novaAtividadeJson = "{\"descricao\": \"Atividade Inválida\"}";

            mockMvc.perform(post(API_ATIVIDADES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(novaAtividadeJson))
                .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("Não deve adicionar atividade com descrição vazia")
        void naoDeveAdicionarAtividadeComDescricaoVazia() throws Exception {
            String novaAtividadeJson = "{\"descricao\": \"\"}";

            mockMvc.perform(post(API_ATIVIDADES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(novaAtividadeJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "outro", roles = {"GESTOR"})
        @DisplayName("Não deve permitir acesso a usuário não autorizado")
        void naoDevePermitirAcessoUsuarioNaoAutorizado() throws Exception {
            var outroUsuario = new sgc.comum.modelo.Usuario();
            outroUsuario.setTitulo("outro");
            usuarioRepo.save(outroUsuario);

            String novaAtividadeJson = String.format("{\"descricao\": \"Atividade Nao Autorizada\", \"mapaCodigo\": %d}", mapaMapeamento.getCodigo());

            mockMvc.perform(post(API_ATIVIDADES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(novaAtividadeJson))
                .andExpect(status().isForbidden());
        }
    }
}