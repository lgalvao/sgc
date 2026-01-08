package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import sgc.Sgc;
import sgc.alerta.model.AlertaRepo;
import sgc.analise.model.AnaliseRepo;
import sgc.fixture.MapaFixture;
import sgc.fixture.UsuarioFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.mapa.model.*;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.model.*;
import sgc.processo.dto.ProcessoDto;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@DisplayName("CDU-14: Analisar revisão de cadastro de atividades e conhecimentos")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import({
        TestSecurityConfig.class,
        sgc.integracao.mocks.TestThymeleafConfig.class,
        sgc.integracao.mocks.TestEventConfig.class
})
@org.springframework.transaction.annotation.Transactional
class CDU14IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private AnaliseRepo analiseRepo;
    @Autowired
    private MapaRepo mapaRepo;
    @Autowired
    private AtividadeRepo atividadeRepo;
    @Autowired
    private ConhecimentoRepo conhecimentoRepo;
    @Autowired
    private CompetenciaRepo competenciaRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private jakarta.persistence.EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;
    @Autowired
    private sgc.organizacao.model.UsuarioPerfilRepo usuarioPerfilRepo;
    @MockitoBean
    private UsuarioService usuarioService;

    private Unidade unidade;
    private Usuario chefe;
    private Usuario gestor;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        // IDs controlados para evitar conflito
        Long idAdminUnit = 4000L;
        Long idGestorUnit = 4001L;
        Long idChefeUnit = 4002L;

        String sqlInsertUnidade = "INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular) VALUES (?, ?, ?, ?, ?, ?, ?)";

        // Verificar se SEDOC já existe
        Unidade unidadeAdmin = unidadeRepo.findBySigla("SEDOC").orElse(null);
        if (unidadeAdmin == null) {
            jdbcTemplate.update(sqlInsertUnidade, idAdminUnit, "SEDOC", "SEDOC", "OPERACIONAL", "ATIVA", null, null);
            unidadeAdmin = unidadeRepo.findById(idAdminUnit).orElseThrow();
        }

        // Criar DA-TEST e SA-TEST
        jdbcTemplate.update(sqlInsertUnidade, idGestorUnit, "DA Teste", "DA-TEST", "OPERACIONAL", "ATIVA", unidadeAdmin.getCodigo(), null);
        jdbcTemplate.update(sqlInsertUnidade, idChefeUnit, "SA Teste", "SA-TEST", "OPERACIONAL", "ATIVA", idGestorUnit, null);

        Unidade unidadeGestor = unidadeRepo.findById(idGestorUnit).orElseThrow();
        unidade = unidadeRepo.findById(idChefeUnit).orElseThrow();

        // Criar Usuários
        admin = UsuarioFixture.usuarioPadrao();
        admin.setTituloEleitoral("303030303030");
        admin.setUnidadeLotacao(unidadeAdmin);
        admin.setEmail("admin@test.com");
        admin = usuarioRepo.save(admin);

        gestor = UsuarioFixture.usuarioPadrao();
        gestor.setTituloEleitoral("404040404040");
        gestor.setUnidadeLotacao(unidadeGestor);
        gestor.setEmail("gestor@test.com");
        gestor = usuarioRepo.save(gestor);

        chefe = UsuarioFixture.usuarioPadrao();
        chefe.setTituloEleitoral("505050505050");
        chefe.setUnidadeLotacao(unidade);
        chefe.setEmail("chefe@test.com");
        chefe = usuarioRepo.save(chefe);

        // Update titular using JDBC
        jdbcTemplate.update("UPDATE sgc.vw_unidade SET titulo_titular = ? WHERE codigo = ?",
                chefe.getTituloEleitoral(), unidade.getCodigo());

        entityManager.refresh(unidade);

        // Configuração do Mock UsuarioService
        when(usuarioService.buscarPerfisUsuario(admin.getTituloEleitoral()))
                .thenReturn(
                        List.of(
                                new PerfilDto(
                                        admin.getTituloEleitoral(),
                                        unidadeAdmin.getCodigo(),
                                        "SEDOC",
                                        Perfil.ADMIN.name())));
        when(usuarioService.buscarPerfisUsuario(gestor.getTituloEleitoral()))
                .thenReturn(
                        List.of(
                                new PerfilDto(
                                        gestor.getTituloEleitoral(),
                                        unidadeGestor.getCodigo(),
                                        "DA-TEST",
                                        Perfil.GESTOR.name())));
        when(usuarioService.buscarPerfisUsuario(chefe.getTituloEleitoral()))
                .thenReturn(
                        List.of(
                                new PerfilDto(
                                        chefe.getTituloEleitoral(),
                                        unidade.getCodigo(),
                                        "SA-TEST",
                                        Perfil.CHEFE.name())));

        when(usuarioService.buscarPorLogin(admin.getTituloEleitoral()))
                .thenReturn(admin);
        when(usuarioService.buscarPorLogin(gestor.getTituloEleitoral()))
                .thenReturn(gestor);
        when(usuarioService.buscarPorLogin(chefe.getTituloEleitoral()))
                .thenReturn(chefe);

        UsuarioFixture.adicionarPerfil(admin, unidadeAdmin, Perfil.ADMIN);
        UsuarioFixture.adicionarPerfil(gestor, unidadeGestor, Perfil.GESTOR);
        UsuarioFixture.adicionarPerfil(chefe, unidade, Perfil.CHEFE);
        
        // Persist UsuarioPerfil for proper authentication
        admin.getAtribuicoes().forEach(usuarioPerfilRepo::save);
        gestor.getAtribuicoes().forEach(usuarioPerfilRepo::save);
        chefe.getAtribuicoes().forEach(usuarioPerfilRepo::save);

        Mapa mapaVigente = MapaFixture.mapaPadrao(null);
        mapaVigente.setCodigo(null);
        mapaVigente = mapaRepo.save(mapaVigente);

        Atividade atividade = new Atividade(mapaVigente, "Atividade Existente");
        atividade = atividadeRepo.save(atividade);
        
        // Adicionar conhecimento à atividade para passar na validação
        Conhecimento conhecimento = new Conhecimento("Conhecimento Teste", atividade);
        conhecimento = conhecimentoRepo.save(conhecimento);
        
        // Adicionar o conhecimento à lista de conhecimentos da atividade para garantir a relação bidirecional
        atividade.getConhecimentos().add(conhecimento);
        atividadeRepo.save(atividade);

        // Adicionar competência e vincular à atividade para teste de impacto
        Competencia competencia = new Competencia("Competencia Teste", mapaVigente);
        competencia = competenciaRepo.save(competencia);
        atividade.getCompetencias().add(competencia);
        competencia.getAtividades().add(atividade); // Manter bidirecionalidade para testes sem clear()
        atividadeRepo.save(atividade);

        UnidadeMapa unidadeMapa = new UnidadeMapa(unidade.getCodigo(), mapaVigente);
        unidadeMapaRepo.save(unidadeMapa);

        // Note: Not flushing here to avoid detaching entities that are mocked
    }

    private Long criarEComecarProcessoDeRevisao() throws Exception {
        ProcessoDto processoDto = criarEIniciarProcessoDeRevisao();

        Subprocesso sp =
                subprocessoRepo.findByProcessoCodigo(processoDto.getCodigo()).stream()
                        .findFirst()
                        .orElseThrow();
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.save(sp);
        return sp.getCodigo();
    }

    private ProcessoDto criarEIniciarProcessoDeRevisao() throws Exception {
        Map<String, Object> criarReqMap =
                Map.of(
                        "descricao",
                        "Processo Revisão",
                        "tipo",
                        "REVISAO",
                        "dataLimiteEtapa1",
                        LocalDateTime.now()
                                .plusDays(10)
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                        "unidades",
                        List.of(unidade.getCodigo()));
        String reqJson = objectMapper.writeValueAsString(criarReqMap);

        String resJson =
                mockMvc.perform(
                                post("/api/processos")
                                        .with(csrf())
                                        .with(user(admin))
                                        .contentType("application/json")
                                        .content(reqJson))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        ProcessoDto processoDto = objectMapper.readValue(resJson, ProcessoDto.class);

        Map<String, Object> iniciarReqMap =
                Map.of("tipo", "REVISAO", "unidades", List.of(unidade.getCodigo()));
        String iniciarReqJson = objectMapper.writeValueAsString(iniciarReqMap);

        mockMvc.perform(
                        post("/api/processos/{codigo}/iniciar", processoDto.getCodigo())
                                .with(csrf())
                                .with(user(admin))
                                .contentType("application/json")
                                .content(iniciarReqJson))
                .andExpect(status().isOk());

        entityManager.flush();

        return processoDto;
    }

    @Nested
    @DisplayName("Fluxo de Devolução")
    class Devolucao {
        @Test
        @DisplayName("GESTOR deve devolver, alterando status e criando registros")
        void gestorDevolveRevisao() throws Exception {

            Long subprocessoId = criarEComecarProcessoDeRevisao();

            mockMvc.perform(
                            post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId)
                                    .with(csrf())
                                    .with(user(chefe)))
                    .andExpect(status().isOk());

            mockMvc.perform(
                            post("/api/subprocessos/{id}/devolver-revisao-cadastro", subprocessoId)
                                    .with(csrf())
                                    .with(user(gestor))
                                    .contentType("application/json")
                                    .content(
                                            "{\"motivo\": \"Teste\", \"observacoes\":"
                                                    + " \"Ajustar\"}"))
                    .andExpect(status().isOk());

            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            assertThat(sp.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoId))
                    .hasSize(1);
            assertThat(alertaRepo.findByProcessoCodigo(sp.getProcesso().getCodigo())).hasSize(2);
            assertThat(movimentacaoRepo.findBySubprocessoCodigo(subprocessoId)).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Fluxo de Aceite")
    class Aceite {
        @Test
        @DisplayName("GESTOR deve aceitar, alterando status e criando todos os registros")
        void gestorAceitaRevisao() throws Exception {
            Long subprocessoId = criarEComecarProcessoDeRevisao();
            mockMvc.perform(
                            post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId)
                                    .with(csrf())
                                    .with(user(chefe)))
                    .andExpect(status().isOk());

            mockMvc.perform(
                            post("/api/subprocessos/{id}/aceitar-revisao-cadastro", subprocessoId)
                                    .with(csrf())
                                    .with(user(gestor))
                                    .contentType("application/json")
                                    .content("{\"observacoes\": \"OK\"}"))
                    .andExpect(status().isOk());

            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            assertThat(sp.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
            assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoId))
                    .hasSize(1);
            assertThat(alertaRepo.findByProcessoCodigo(sp.getProcesso().getCodigo())).hasSize(2);
            assertThat(movimentacaoRepo.findBySubprocessoCodigo(subprocessoId)).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Fluxo de Homologação")
    class Homologacao {
        private Long subprocessoId;

        @BeforeEach
        void setUpHomologacao() throws Exception {
            subprocessoId = criarEComecarProcessoDeRevisao();
            mockMvc.perform(
                            post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId)
                                    .with(csrf())
                                    .with(user(chefe)))
                    .andExpect(status().isOk());
            mockMvc.perform(
                            post("/api/subprocessos/{id}/aceitar-revisao-cadastro", subprocessoId)
                                    .with(csrf())
                                    .with(user(gestor))
                                    .contentType("application/json")
                                    .content("{\"observacoes\": \"OK\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN homologa SEM impactos, alterando status para MAPA_HOMOLOGADO")
        void adminHomologaSemImpactos() throws Exception {
            mockMvc.perform(
                            post("/api/subprocessos/{id}/homologar-revisao-cadastro", subprocessoId)
                                    .with(csrf())
                                    .with(user(admin))
                                    .contentType("application/json")
                                    .content("{\"observacoes\": \"Homologado\"}"))
                    .andExpect(status().isOk());

            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO);
        }

        @Test
        @DisplayName("ADMIN homologa COM impactos, alterando status e criando movimentação")
        void adminHomologaComImpactos() throws Exception {
            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();

            Atividade atividadeExistente =
                    atividadeRepo.findByMapaCodigo(sp.getMapa().getCodigo()).stream()
                            .findFirst()
                            .orElseThrow();
            atividadeRepo.delete(atividadeExistente);
            entityManager.flush();

            mockMvc.perform(
                            post("/api/subprocessos/{id}/homologar-revisao-cadastro", subprocessoId)
                                    .with(csrf())
                                    .with(user(admin))
                                    .contentType("application/json")
                                    .content("{\"observacoes\": \"Homologado com impacto\"}"))
                    .andExpect(status().isOk());

            sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            assertThat(movimentacaoRepo.findBySubprocessoCodigo(subprocessoId)).hasSize(4);
        }
    }

    @Nested
    @DisplayName("Endpoints de Consulta")
    class EndpointsDeConsulta {
        @Test
        @DisplayName("Deve retornar histórico de análise corretamente")
        void deveRetornarHistoricoDeAnalise() throws Exception {
            Long subprocessoId = criarEComecarProcessoDeRevisao();
            mockMvc.perform(
                            post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId)
                                    .with(csrf())
                                    .with(user(chefe)))
                    .andExpect(status().isOk());

            mockMvc.perform(
                            post("/api/subprocessos/{id}/devolver-revisao-cadastro", subprocessoId)
                                    .with(csrf())
                                    .with(user(gestor))
                                    .contentType("application/json")
                                    .content(
                                            "{\"motivo\": \"Teste Histórico\", \"observacoes\":"
                                                    + " \"Registrando análise\"}"))
                    .andExpect(status().isOk());

            mockMvc.perform(
                            get("/api/subprocessos/{id}/historico-cadastro", subprocessoId)
                                    .with(user(gestor)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].observacoes", is("Registrando análise")));
        }

        @Test
        @DisplayName("Deve retornar impactos no mapa corretamente")
        void deveRetornarImpactosNoMapa() throws Exception {
            Long subprocessoId = criarEComecarProcessoDeRevisao();
            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();

            Atividade atividadeExistente =
                    atividadeRepo.findByMapaCodigo(sp.getMapa().getCodigo()).stream()
                            .findFirst()
                            .orElseThrow();
            atividadeRepo.delete(atividadeExistente);
            entityManager.flush();

            mockMvc.perform(
                            get("/api/subprocessos/{codigo}/impactos-mapa", subprocessoId)
                                    .with(user(chefe)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.temImpactos", is(true)))
                    .andExpect(jsonPath("$.competenciasImpactadas", hasSize(1)))
                    .andExpect(
                            jsonPath("$.competenciasImpactadas[0].atividadesAfetadas", hasSize(1)))
                    .andExpect(
                            jsonPath(
                                    "$.competenciasImpactadas[0].tipoImpacto",
                                    is("ATIVIDADE_REMOVIDA")));
        }
    }

    @Nested
    @DisplayName("Falhas e Segurança")
    class FalhasESeguranca {
        @Test
        @DisplayName("CHEFE não pode homologar revisão")
        void chefeNaoPodeHomologar() throws Exception {
            Long subprocessoId = criarEComecarProcessoDeRevisao();
            mockMvc.perform(
                            post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId)
                                    .with(csrf())
                                    .with(user(chefe)))
                    .andExpect(status().isOk());
            mockMvc.perform(
                            post("/api/subprocessos/{id}/aceitar-revisao-cadastro", subprocessoId)
                                    .with(csrf())
                                    .with(user(gestor))
                                    .contentType("application/json")
                                    .content("{\"observacoes\": \"OK\"}"))
                    .andExpect(status().isOk());

            mockMvc.perform(
                            post("/api/subprocessos/{id}/homologar-revisao-cadastro", subprocessoId)
                                    .with(csrf())
                                    .with(user(chefe))
                                    .contentType("application/json")
                                    .content("{\"observacoes\": \"Tudo certo por mim\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Não pode homologar em estado inválido")
        void naoPodeHomologarEmEstadoInvalido() throws Exception {
            Long subprocessoId = criarEComecarProcessoDeRevisao();
            mockMvc.perform(
                            post("/api/subprocessos/{id}/homologar-revisao-cadastro", subprocessoId)
                                    .with(csrf())
                                    .with(user(admin))
                                    .contentType("application/json")
                                    .content("{\"observacoes\": \"Homologado fora de hora\"}"))
                    .andExpect(status().is(422));
        }
    }
}
