package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para os endpoints unificados de SubprocessoController.
 * Exercita consultas e operações em subprocessos sob HTTP e permissões reais.
 */
@Tag("integration")
@Transactional
@DisplayName("SubprocessoController — integração")
class SubprocessoControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    private Subprocesso subprocesso;
    private Processo processo;
    private Mapa mapa;
    private Unidade unidade102;

    @BeforeEach
    void setUp() {
        // Carrega a unidade 102 (chefe do data.sql)
        unidade102 = unidadeRepo.findById(102L).orElseThrow();

        // Cria processo em andamento
        processo = ProcessoFixture.processoEmAndamento();
        processo.setCodigo(null);
        processo.setDescricao("Processo SubprocessoController Test");
        processo.adicionarParticipantes(java.util.Set.of(unidade102));
        processo = processoRepo.save(processo);

        // Cria subprocesso na unidade 102
        subprocesso = SubprocessoFixture.novoSubprocesso(processo, unidade102);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(30));
        subprocesso = subprocessoRepo.save(subprocesso);

        // Cria mapa associado
        mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapa = mapaRepo.save(mapa);

        registrarMovimentacaoInicial(subprocesso);

        entityManager.flush();
        entityManager.clear();

        subprocesso = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
    }

    @Test
    @DisplayName("ADMIN deve listar todos os subprocessos")
    @WithMockAdmin
    void listarSubprocessos_admin_sucesso() throws Exception {
        mockMvc.perform(get("/api/subprocessos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("CHEFE deve obter detalhes do seu subprocesso")
    @WithMockChefe
    void obterPorCodigo_chefe_sucesso() throws Exception {
        mockMvc.perform(get("/api/subprocessos/{codSubprocesso}", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subprocesso.codigo").value(subprocesso.getCodigo()))
                .andExpect(jsonPath("$.subprocesso.unidade.sigla").value("SUB-UNIT"));
    }

    @Test
    @DisplayName("CHEFE deve obter apenas o status do seu subprocesso")
    @WithMockChefe
    void obterStatus_chefe_sucesso() throws Exception {
        mockMvc.perform(get("/api/subprocessos/{codSubprocesso}/status", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(subprocesso.getCodigo()))
                .andExpect(jsonPath("$.situacao").value("MAPEAMENTO_CADASTRO_EM_ANDAMENTO"));
    }

    @Test
    @DisplayName("CHEFE deve buscar subprocesso por processo e unidade sigla")
    @WithMockChefe
    void buscarPorProcessoEUnidade_chefe_sucesso() throws Exception {
        mockMvc.perform(get("/api/subprocessos/buscar")
                        .param("codProcesso", processo.getCodigo().toString())
                        .param("siglaUnidade", "SUB-UNIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(subprocesso.getCodigo()));
    }

    @Test
    @DisplayName("CHEFE deve obter o contexto de edição do seu subprocesso")
    @WithMockChefe
    void obterContextoEdicao_chefe_sucesso() throws Exception {
        mockMvc.perform(get("/api/subprocessos/{codSubprocesso}/contexto-edicao", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subprocesso.codigo").value(subprocesso.getCodigo()));
    }

    @Test
    @DisplayName("CHEFE deve obter o contexto de cadastro de atividades")
    @WithMockChefe
    void obterContextoCadastroAtividades_chefe_sucesso() throws Exception {
        mockMvc.perform(get("/api/subprocessos/{codSubprocesso}/contexto-cadastro-atividades", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detalhes.subprocesso.codigo").value(subprocesso.getCodigo()));
    }

    @Test
    @DisplayName("CHEFE deve obter contexto de edição buscando por processo e unidade sigla")
    @WithMockChefe
    void obterContextoEdicaoPorProcessoEUnidade_chefe_sucesso() throws Exception {
        mockMvc.perform(get("/api/subprocessos/contexto-edicao/buscar")
                        .param("codProcesso", processo.getCodigo().toString())
                        .param("siglaUnidade", "SUB-UNIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subprocesso.codigo").value(subprocesso.getCodigo()));
    }

    @Test
    @DisplayName("CHEFE deve obter contexto de cadastro buscando por processo e unidade sigla")
    @WithMockChefe
    void obterContextoCadastroPorProcessoEUnidade_chefe_sucesso() throws Exception {
        mockMvc.perform(get("/api/subprocessos/contexto-cadastro-atividades/buscar")
                        .param("codProcesso", processo.getCodigo().toString())
                        .param("siglaUnidade", "SUB-UNIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detalhes.subprocesso.codigo").value(subprocesso.getCodigo()));
    }

    @Test
    @DisplayName("CHEFE deve validar se o cadastro está pronto para disponibilização")
    @WithMockChefe
    void validarCadastro_chefe_sucesso() throws Exception {
        mockMvc.perform(get("/api/subprocessos/{codSubprocesso}/validar-cadastro", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").isBoolean());
    }

    @Test
    @DisplayName("CHEFE deve obter o cadastro de atividades do seu subprocesso")
    @WithMockChefe
    void obterCadastro_chefe_sucesso() throws Exception {
        mockMvc.perform(get("/api/subprocessos/{codSubprocesso}/cadastro", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atividades").isArray());
    }

    @Test
    @DisplayName("CHEFE deve obter o mapa para visualização")
    @WithMockChefe
    void obterMapaParaVisualizacao_chefe_sucesso() throws Exception {
        mockMvc.perform(get("/api/subprocessos/{codSubprocesso}/mapa-visualizacao", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidade.sigla").value("SUB-UNIT"));
    }

    @Test
    @DisplayName("CHEFE deve obter o mapa completo")
    @WithMockChefe
    void obterMapaCompleto_chefe_sucesso() throws Exception {
        mockMvc.perform(get("/api/subprocessos/{codSubprocesso}/mapa-completo", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atividades").isArray());
    }

    @Test
    @DisplayName("CHEFE deve listar atividades para importação de subprocesso finalizado")
    @WithMockChefe
    void listarAtividadesParaImportacao_chefe_sucesso() throws Exception {
        Processo processoFinalizado = ProcessoFixture.processoPadrao();
        processoFinalizado.setCodigo(null);
        processoFinalizado.setDescricao("Processo finalizado para importação");
        processoFinalizado.setSituacao(SituacaoProcesso.FINALIZADO);
        processoFinalizado.setDataFinalizacao(LocalDateTime.now().minusDays(1));
        processoFinalizado.adicionarParticipantes(java.util.Set.of(unidade102));
        processoFinalizado = processoRepo.save(processoFinalizado);

        Subprocesso subprocessoFinalizado = SubprocessoFixture.novoSubprocesso(processoFinalizado, unidade102);
        subprocessoFinalizado.setCodigo(null);
        subprocessoFinalizado.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        subprocessoFinalizado = subprocessoRepo.save(subprocessoFinalizado);

        Mapa mapaFinalizado = new Mapa();
        mapaFinalizado.setSubprocesso(subprocessoFinalizado);
        mapaFinalizado = mapaRepo.save(mapaFinalizado);

        subprocessoFinalizado.setMapa(mapaFinalizado);
        subprocessoFinalizado = subprocessoRepo.saveAndFlush(subprocessoFinalizado);

        Atividade atividade = AtividadeFixture.atividadePadrao(mapaFinalizado);
        atividade.setDescricao("Atividade pronta para importação");
        atividadeRepo.saveAndFlush(atividade);
        registrarMovimentacaoInicial(subprocessoFinalizado);

        String corpo = mockMvc.perform(get("/api/subprocessos/{codSubprocesso}/atividades-importacao", subprocessoFinalizado.getCodigo()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(corpo).contains("Atividade pronta para importação");
    }

    @Test
    @DisplayName("CHEFE deve obter sugestões do subprocesso")
    @WithMockChefe
    void obterSugestoes_chefe_sucesso() throws Exception {
        mapa.setSugestoes("Sugestão de integração do subprocesso");
        mapaRepo.saveAndFlush(mapa);

        String corpo = mockMvc.perform(get("/api/subprocessos/{codSubprocesso}/sugestoes", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(objectMapper.readTree(corpo).get("sugestoes").stringValue()).isEqualTo("Sugestão de integração do subprocesso");
    }

    @Test
    @DisplayName("CHEFE deve obter histórico de validação")
    @WithMockChefe
    void obterHistoricoValidacao_chefe_sucesso() throws Exception {
        entityManager.persist(Analise.builder()
                .tipo(TipoAnalise.VALIDACAO)
                .subprocesso(subprocesso)
                .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                .dataHora(LocalDateTime.now())
                .unidadeCodigo(unidade102.getCodigo())
                .usuarioTitulo("111111111111")
                .observacoes("Validação concluída")
                .build());
        entityManager.flush();

        String corpo = mockMvc.perform(get("/api/subprocessos/{codSubprocesso}/historico-validacao", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(corpo).contains("Validação concluída", "ACEITE_MAPEAMENTO");
    }
}
