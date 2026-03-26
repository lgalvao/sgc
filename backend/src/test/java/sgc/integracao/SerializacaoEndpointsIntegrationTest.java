package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@Import(TestLoginHelper.class)
@DisplayName("Integração: Serialização de endpoints com Open Session in View desabilitado")
class SerializacaoEndpointsIntegrationTest extends BaseIntegrationTest {
    private static final String SQL_MERGE_PERFIL = """
            MERGE INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil)
            KEY(usuario_titulo, unidade_codigo, perfil)
            VALUES (?, ?, ?)
            """;
    private static final String SQL_INSERIR_RESPONSABILIDADE = """
            INSERT INTO SGC.VW_RESPONSABILIDADE
            (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
            VALUES (?, ?, ?, ?, ?)
            """;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TestLoginHelper loginHelper;
    @Autowired
    private ConhecimentoRepo conhecimentoRepo;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private AlertaRepo alertaRepo;

    private Unidade unidade;
    private Unidade unidadeCriacao;
    private Processo processo;
    private Subprocesso subprocesso;
    private Subprocesso subprocessoSemMapa;
    private Usuario usuarioConsulta;
    private String tokenAdmin;
    private String tokenChefe;

    @BeforeEach
    void setUp() throws Exception {
        unidade = UnidadeFixture.unidadeComSigla("SLZY");
        unidade.setCodigo(null);
        unidade.setNome("Secretaria de Lazy Loading");
        unidade.setUnidadeSuperior(unidadeRepo.findById(1L).orElseThrow());
        unidade.setTituloTitular("3");
        unidade.setMatriculaTitular("00000003");
        unidade = unidadeRepo.saveAndFlush(unidade);

        unidadeCriacao = UnidadeFixture.unidadeComSigla("SNOV");
        unidadeCriacao.setCodigo(null);
        unidadeCriacao.setNome("Secretaria de Novos Subprocessos");
        unidadeCriacao.setUnidadeSuperior(unidadeRepo.findById(1L).orElseThrow());
        unidadeCriacao.setTituloTitular("8");
        unidadeCriacao.setMatriculaTitular("00000008");
        unidadeCriacao = unidadeRepo.saveAndFlush(unidadeCriacao);

        jdbcTemplate.update(SQL_MERGE_PERFIL, "3", unidade.getCodigo(), Perfil.CHEFE.name());
        jdbcTemplate.update(SQL_MERGE_PERFIL, "6", 1L, Perfil.ADMIN.name());
        jdbcTemplate.update(
                SQL_INSERIR_RESPONSABILIDADE,
                unidade.getCodigo(),
                "3",
                "00000003",
                "TITULAR",
                LocalDateTime.now().minusDays(1)
        );
        jdbcTemplate.update(
                SQL_INSERIR_RESPONSABILIDADE,
                unidadeCriacao.getCodigo(),
                "8",
                "00000008",
                "TITULAR",
                LocalDateTime.now().minusDays(1)
        );

        tokenChefe = loginHelper.loginChefe(mockMvc, "3", unidade.getCodigo());
        tokenAdmin = loginHelper.loginAdmin(mockMvc, "6");

        processo = ProcessoFixture.processoEmAndamento();
        processo.setCodigo(null);
        processo.setDescricao("Processo de serialização");
        processo.adicionarParticipantes(Set.of(unidade));
        processo = processoRepo.saveAndFlush(processo);

        Mapa mapa = mapaRepo.saveAndFlush(new Mapa());

        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setMapa(mapa);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocesso = subprocessoRepo.saveAndFlush(subprocesso);

        mapa.setSubprocesso(subprocesso);
        mapaRepo.saveAndFlush(mapa);

        subprocessoSemMapa = SubprocessoFixture.subprocessoPadrao(processo, unidadeCriacao);
        subprocessoSemMapa.setCodigo(null);
        subprocessoSemMapa.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
        subprocessoSemMapa.setMapa(null);
        subprocessoSemMapa = subprocessoRepo.saveAndFlush(subprocessoSemMapa);

        Atividade atividade = AtividadeFixture.atividadePadrao(mapa);
        atividade.setDescricao("Atividade de serialização");
        atividade = atividadeRepo.saveAndFlush(atividade);

        Conhecimento conhecimento = Conhecimento.builder()
                .atividade(atividade)
                .descricao("Conhecimento de serialização")
                .build();
        conhecimento = conhecimentoRepo.saveAndFlush(conhecimento);
        atividade.getConhecimentos().add(conhecimento);
        atividadeRepo.saveAndFlush(atividade);

        Usuario chefe = usuarioRepo.findByTituloComUnidadeLotacao("3").orElseThrow();
        Movimentacao movimentacao = Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidade)
                .unidadeDestino(unidade)
                .usuario(chefe)
                .descricao("Movimentação inicial")
                .dataHora(LocalDateTime.now())
                .build();
        movimentacaoRepo.saveAndFlush(movimentacao);

        Alerta alerta = Alerta.builder()
                .processo(processo)
                .unidadeOrigem(unidade)
                .unidadeDestino(unidade)
                .descricao("Alerta serialização painel")
                .dataHora(LocalDateTime.now())
                .build();
        alertaRepo.saveAndFlush(alerta);

        usuarioConsulta = usuarioRepo.findByTitulosComUnidadeLotacao(List.of("1", "3", "6", "8")).stream()
                .filter(usuario -> !usuarioRepo.findByUnidadeLotacaoCodigo(usuario.getUnidadeLotacao().getCodigo()).isEmpty())
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("GET /api/subprocessos/{codigo} deve serializar detalhe sem lazy loading")
    void deveSerializarDetalheSubprocesso() throws Exception {
        mockMvc.perform(get("/api/subprocessos/{codigo}", subprocesso.getCodigo())
                        .header("Authorization", "Bearer " + tokenChefe))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subprocesso.codigo", is(subprocesso.getCodigo().intValue())))
                .andExpect(jsonPath("$.subprocesso.unidade.sigla", is("SLZY")))
                .andExpect(jsonPath("$.responsavel.usuario.tituloEleitoral", is("3")))
                .andExpect(jsonPath("$.movimentacoes[0].usuarioTitulo", is("3")));
    }

    @Test
    @DisplayName("GET /api/subprocessos/{codigo}/contexto-edicao deve serializar contexto completo sem lazy loading")
    void deveSerializarContextoEdicao() throws Exception {
        mockMvc.perform(get("/api/subprocessos/{codigo}/contexto-edicao", subprocesso.getCodigo())
                        .header("Authorization", "Bearer " + tokenChefe))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidade.sigla", is("SLZY")))
                .andExpect(jsonPath("$.subprocesso.codigo", is(subprocesso.getCodigo().intValue())))
                .andExpect(jsonPath("$.detalhes.movimentacoes[0].usuarioNome", not(blankOrNullString())))
                .andExpect(jsonPath("$.mapa.codigo", is(subprocesso.getMapa().getCodigo().intValue())))
                .andExpect(jsonPath("$.atividadesDisponiveis[0].descricao", is("Atividade de serialização")));
    }

    @Test
    @DisplayName("GET /api/subprocessos/{codigo}/cadastro deve serializar atividades e conhecimentos sem lazy loading")
    void deveSerializarCadastroSubprocesso() throws Exception {
        mockMvc.perform(get("/api/subprocessos/{codigo}/cadastro", subprocesso.getCodigo())
                        .header("Authorization", "Bearer " + tokenChefe))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is(subprocesso.getCodigo().intValue())))
                .andExpect(jsonPath("$.unidade.sigla", is("SLZY")))
                .andExpect(jsonPath("$.atividades[0].descricao", is("Atividade de serialização")))
                .andExpect(jsonPath("$.atividades[0].conhecimentos[0].descricao", is("Conhecimento de serialização")));
    }

    @Test
    @DisplayName("GET /api/processos/{codigo} deve serializar resumo do processo sem lazy loading")
    void deveSerializarResumoProcesso() throws Exception {
        mockMvc.perform(get("/api/processos/{codigo}", processo.getCodigo())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is(processo.getCodigo().intValue())))
                .andExpect(jsonPath("$.descricao", is("Processo de serialização")))
                .andExpect(jsonPath("$.unidadesParticipantes", containsString("SLZY")));
    }

    @Test
    @DisplayName("GET /api/processos/{codigo}/subprocessos deve serializar lista sem lazy loading")
    void deveSerializarListaDeSubprocessosDoProcesso() throws Exception {
        mockMvc.perform(get("/api/processos/{codigo}/subprocessos", processo.getCodigo())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo", hasItem(subprocesso.getCodigo().intValue())))
                .andExpect(jsonPath("$[*].unidade.sigla", hasItem("SLZY")))
                .andExpect(jsonPath("$[*].processoDescricao", hasItem("Processo de serialização")));
    }

    @Test
    @DisplayName("GET /api/usuarios/{titulo} deve serializar unidade do usuário sem lazy loading")
    void deveSerializarUsuarioPorTitulo() throws Exception {
        mockMvc.perform(get("/api/usuarios/{titulo}", usuarioConsulta.getTituloEleitoral())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloEleitoral", is(usuarioConsulta.getTituloEleitoral())))
                .andExpect(jsonPath("$.nome", not(blankOrNullString())))
                .andExpect(jsonPath("$.unidade.sigla", not(blankOrNullString())));
    }

    @Test
    @DisplayName("GET /api/unidades/{codigo}/usuarios deve serializar lista de usuários da unidade sem lazy loading")
    void deveSerializarUsuariosDaUnidade() throws Exception {
        mockMvc.perform(get("/api/unidades/{codigo}/usuarios", usuarioConsulta.getUnidadeLotacao().getCodigo())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tituloEleitoral", hasItem(usuarioConsulta.getTituloEleitoral())))
                .andExpect(jsonPath("$[*].unidade.sigla", not(empty())));
    }

    @Test
    @DisplayName("GET /api/painel/alertas deve serializar página de alertas sem lazy loading")
    void deveSerializarPainelAlertas() throws Exception {
        mockMvc.perform(get("/api/painel/alertas")
                        .header("Authorization", "Bearer " + tokenChefe))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.descricao == 'Alerta serialização painel')]", hasSize(1)))
                .andExpect(jsonPath("$.content[0].processo", is("Processo de serialização")))
                .andExpect(jsonPath("$.content[0].origem", is("SLZY")));
    }

    @Test
    @DisplayName("GET /api/mapas/{codigo} deve serializar resumo do mapa sem lazy loading")
    void deveSerializarResumoMapa() throws Exception {
        mockMvc.perform(get("/api/mapas/{codigo}", subprocesso.getMapa().getCodigo())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is(subprocesso.getMapa().getCodigo().intValue())))
                .andExpect(jsonPath("$.subprocessoCodigo", is(subprocesso.getCodigo().intValue())));
    }

    @Test
    @DisplayName("GET /api/mapas deve serializar lista de resumos sem lazy loading")
    void deveSerializarListaDeMapas() throws Exception {
        mockMvc.perform(get("/api/mapas")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo", hasItem(subprocesso.getMapa().getCodigo().intValue())))
                .andExpect(jsonPath("$[*].subprocessoCodigo", hasItem(subprocesso.getCodigo().intValue())));
    }

    @Test
    @DisplayName("POST /api/mapas deve criar e serializar resumo sem lazy loading")
    void deveCriarMapaComRespostaEmDto() throws Exception {
        String corpo = objectMapper.writeValueAsString(Map.of(
                "subprocessoCodigo", subprocessoSemMapa.getCodigo(),
                "observacoesDisponibilizacao", "Observação do mapa",
                "sugestoes", "Sugestões do mapa"
        ));

        mockMvc.perform(post("/api/mapas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").isNumber())
                .andExpect(jsonPath("$.subprocessoCodigo", is(subprocessoSemMapa.getCodigo().intValue())))
                .andExpect(jsonPath("$.observacoesDisponibilizacao", is("Observação do mapa")))
                .andExpect(jsonPath("$.sugestoes", is("Sugestões do mapa")));
    }

    @Test
    @DisplayName("POST /api/mapas/{codigo}/atualizar deve serializar resumo sem lazy loading")
    void deveAtualizarMapaComRespostaEmDto() throws Exception {
        String corpo = objectMapper.writeValueAsString(Map.of(
                "observacoesDisponibilizacao", "Observação atualizada",
                "sugestoes", "Sugestões atualizadas"
        ));

        mockMvc.perform(post("/api/mapas/{codigo}/atualizar", subprocesso.getMapa().getCodigo())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is(subprocesso.getMapa().getCodigo().intValue())))
                .andExpect(jsonPath("$.subprocessoCodigo", is(subprocesso.getCodigo().intValue())))
                .andExpect(jsonPath("$.observacoesDisponibilizacao", is("Observação atualizada")))
                .andExpect(jsonPath("$.sugestoes", is("Sugestões atualizadas")));
    }

    @Test
    @DisplayName("POST /api/subprocessos deve criar e serializar resumo sem lazy loading")
    void deveCriarSubprocessoComRespostaEmDto() throws Exception {
        String corpo = objectMapper.writeValueAsString(Map.of(
                "codProcesso", processo.getCodigo(),
                "codUnidade", unidadeCriacao.getCodigo(),
                "dataLimiteEtapa1", LocalDateTime.now().plusDays(20),
                "dataLimiteEtapa2", LocalDateTime.now().plusDays(40)
        ));

        mockMvc.perform(post("/api/subprocessos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").isNumber())
                .andExpect(jsonPath("$.codProcesso", is(processo.getCodigo().intValue())))
                .andExpect(jsonPath("$.unidade.sigla", is("SNOV")));
    }

    @Test
    @DisplayName("POST /api/subprocessos/{codigo}/atualizar deve serializar resumo sem lazy loading")
    void deveAtualizarSubprocessoComRespostaEmDto() throws Exception {
        LocalDateTime novaDataLimite = LocalDateTime.now().plusDays(25);
        String corpo = objectMapper.writeValueAsString(Map.of(
                "codUnidade", unidade.getCodigo(),
                "codMapa", subprocesso.getMapa().getCodigo(),
                "dataLimiteEtapa1", novaDataLimite
        ));

        mockMvc.perform(post("/api/subprocessos/{codigo}/atualizar", subprocesso.getCodigo())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is(subprocesso.getCodigo().intValue())))
                .andExpect(jsonPath("$.unidade.sigla", is("SLZY")))
                .andExpect(jsonPath("$.codMapa", is(subprocesso.getMapa().getCodigo().intValue())));
    }
}
