package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.integracao.mocks.WithMockChefe;
import sgc.integracao.mocks.WithMockGestor;
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.dto.CriarConhecimentoRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração para o ciclo de vida de atividades e conhecimentos.
 * Exercita AtividadeService, AtividadeController e MapaManutencaoService via HTTP real.
 */
@Tag("integration")
@Transactional
@DisplayName("AtividadeService — integração")
class AtividadeServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    private Subprocesso subprocesso;
    private Mapa mapa;

    @BeforeEach
    void setUp() {
        // Carrega unidade 102 (Unidade do chefe mockado no data.sql)
        Unidade unidade = unidadeRepo.findById(102L).orElseThrow();

        // Cria processo em andamento
        Processo processo = ProcessoFixture.processoEmAndamento();
        processo.setCodigo(null);
        processo.setDescricao("Processo Atividade Integration");
        processo = processoRepo.save(processo);

        // Cria subprocesso em MAPEAMENTO_CADASTRO_EM_ANDAMENTO (permite edição de atividades)
        subprocesso = SubprocessoFixture.novoSubprocesso(processo, unidade);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(30));
        subprocesso = subprocessoRepo.save(subprocesso);

        // Cria mapa associado ao subprocesso
        mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapa = mapaRepo.save(mapa);

        registrarMovimentacaoInicial(subprocesso);

        entityManager.flush();
        entityManager.clear();

        subprocesso = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        mapa = mapaRepo.findById(mapa.getCodigo()).orElseThrow();
    }

    // ----------------------------------------------------------------
    // Criar atividade
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Deve criar atividade com sucesso como CHEFE")
    @WithMockChefe
    void criarAtividade_sucesso() throws Exception {
        CriarAtividadeRequest req = new CriarAtividadeRequest(mapa.getCodigo(), "Atividade de integração");

        mockMvc.perform(post("/api/atividades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        entityManager.flush();
        entityManager.clear();

        boolean existe = !atividadeRepo.findAll().isEmpty();
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("Deve rejeitar criação de atividade como GESTOR")
    @WithMockGestor
    void criarAtividade_semPermissaoDeRole_proibido() throws Exception {
        CriarAtividadeRequest req = new CriarAtividadeRequest(mapa.getCodigo(), "Tentativa gestor");

        mockMvc.perform(post("/api/atividades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ----------------------------------------------------------------
    // Atualizar atividade
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Deve atualizar atividade existente como CHEFE")
    @WithMockChefe
    void atualizarAtividade_sucesso() throws Exception {
        // Cria atividade diretamente via repo
        Atividade atividade = new Atividade();
        atividade.setMapa(mapa);
        atividade.setDescricao("Antes");
        atividade = atividadeRepo.save(atividade);
        entityManager.flush();
        entityManager.clear();

        AtualizarAtividadeRequest req = new AtualizarAtividadeRequest("Depois");

        mockMvc.perform(post("/api/atividades/{codigo}/atualizar", atividade.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Atividade atualizada = atividadeRepo.findById(atividade.getCodigo()).orElseThrow();
        assertThat(atualizada.getDescricao()).isEqualTo("Depois");
    }

    // ----------------------------------------------------------------
    // Excluir atividade
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Deve excluir atividade como CHEFE")
    @WithMockChefe
    void excluirAtividade_sucesso() throws Exception {
        Atividade atividade = new Atividade();
        atividade.setMapa(mapa);
        atividade.setDescricao("Para excluir");
        atividade = atividadeRepo.save(atividade);
        entityManager.flush();
        entityManager.clear();

        Long codigo = atividade.getCodigo();

        mockMvc.perform(post("/api/atividades/{codAtividade}/excluir", codigo)
                        .with(csrf()))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        boolean ainda = atividadeRepo.existsById(codigo);
        assertThat(ainda).isFalse();
    }

    // ----------------------------------------------------------------
    // Criar conhecimento
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Deve criar conhecimento em atividade existente como CHEFE")
    @WithMockChefe
    void criarConhecimento_sucesso() throws Exception {
        Atividade atividade = new Atividade();
        atividade.setMapa(mapa);
        atividade.setDescricao("Atividade com conhecimento");
        atividade = atividadeRepo.save(atividade);
        entityManager.flush();
        entityManager.clear();

        CriarConhecimentoRequest req = new CriarConhecimentoRequest(atividade.getCodigo(), "Conhecimento A");

        mockMvc.perform(post("/api/atividades/{codAtividade}/conhecimentos", atividade.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    // ----------------------------------------------------------------
    // Obter atividade por código
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar atividade por código como CHEFE")
    @WithMockChefe
    void obterAtividade_sucesso() throws Exception {
        Atividade atividade = new Atividade();
        atividade.setMapa(mapa);
        atividade.setDescricao("Atividade consulta");
        atividade = atividadeRepo.save(atividade);
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/atividades/{codAtividade}", atividade.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    assertThat(body).contains("Atividade consulta");
                });
    }

    // ----------------------------------------------------------------
    // Listar conhecimentos
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Deve listar conhecimentos de atividade como CHEFE")
    @WithMockChefe
    void listarConhecimentos_sucesso() throws Exception {
        Atividade atividade = new Atividade();
        atividade.setMapa(mapa);
        atividade.setDescricao("Atividade lista conhecimentos");
        atividade = atividadeRepo.save(atividade);
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/atividades/{codAtividade}/conhecimentos", atividade.getCodigo()))
                .andExpect(status().isOk());
    }
}
