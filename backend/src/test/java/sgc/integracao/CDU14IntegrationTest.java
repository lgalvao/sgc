package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import sgc.alerta.model.AlertaRepo;
import sgc.analise.model.AnaliseRepo;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.processo.dto.ProcessoDto;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;
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

@SpringBootTest
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
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private jakarta.persistence.EntityManager entityManager;
    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    @MockitoBean
    private SgrhService sgrhService;

    private Unidade unidade;
    private Usuario chefe;
    private Usuario gestor;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        unidade = unidadeRepo.findById(102L).orElseThrow();
        admin = usuarioRepo.findById("111111111111").orElseThrow();
        gestor = usuarioRepo.findById("222222222222").orElseThrow();
        chefe = usuarioRepo.findById("333333333333").orElseThrow();

        // Configuração do Mock SgrhService
        when(sgrhService.buscarPerfisUsuario(admin.getTituloEleitoral().toString()))
                .thenReturn(
                        List.of(
                                new PerfilDto(
                                        admin.getTituloEleitoral().toString(),
                                        100L,
                                        "SEDOC",
                                        Perfil.ADMIN.name())));
        when(sgrhService.buscarPerfisUsuario(gestor.getTituloEleitoral().toString()))
                .thenReturn(
                        List.of(
                                new PerfilDto(
                                        gestor.getTituloEleitoral().toString(),
                                        101L,
                                        "DA",
                                        Perfil.GESTOR.name())));
        when(sgrhService.buscarPerfisUsuario(chefe.getTituloEleitoral().toString()))
                .thenReturn(
                        List.of(
                                new PerfilDto(
                                        chefe.getTituloEleitoral().toString(),
                                        102L,
                                        "SA",
                                        Perfil.CHEFE.name())));

        when(sgrhService.buscarUsuarioPorLogin(admin.getTituloEleitoral().toString()))
                .thenReturn(admin);
        when(sgrhService.buscarUsuarioPorLogin(gestor.getTituloEleitoral().toString()))
                .thenReturn(gestor);
        when(sgrhService.buscarUsuarioPorLogin(chefe.getTituloEleitoral().toString()))
                .thenReturn(chefe);

        Unidade unidadeAdmin = unidadeRepo.findById(100L).orElseThrow();
        Unidade unidadeGestor = unidadeRepo.findById(101L).orElseThrow();

        // Atualiza titular via JDBC para contornar restrição @Immutable da entidade Unidade
        jdbcTemplate.update("UPDATE sgc.vw_unidade SET titulo_titular = ? WHERE codigo = ?",
                chefe.getTituloEleitoral(), unidade.getCodigo());

        // Recarrega a unidade para refletir a alteração no contexto JPA
        entityManager.clear();
        unidade = unidadeRepo.findById(102L).orElseThrow();

        java.util.Set<sgc.sgrh.model.UsuarioPerfil> adminAttrs = new java.util.HashSet<>();
        adminAttrs.add(sgc.sgrh.model.UsuarioPerfil.builder()
                .usuario(admin)
                .perfil(Perfil.ADMIN)
                .unidade(unidadeAdmin)
                .build());
        admin.setAtribuicoes(adminAttrs);

        java.util.Set<sgc.sgrh.model.UsuarioPerfil> gestorAttrs = new java.util.HashSet<>();
        gestorAttrs.add(sgc.sgrh.model.UsuarioPerfil.builder()
                .usuario(gestor)
                .perfil(Perfil.GESTOR)
                .unidade(unidadeGestor)
                .build());
        gestor.setAtribuicoes(gestorAttrs);

        java.util.Set<sgc.sgrh.model.UsuarioPerfil> chefeAttrs = new java.util.HashSet<>();
        chefeAttrs.add(sgc.sgrh.model.UsuarioPerfil.builder()
                .usuario(chefe)
                .perfil(Perfil.CHEFE)
                .unidade(unidade)
                .build());
        chefe.setAtribuicoes(chefeAttrs);
    }

    // Métodos de setup
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
        entityManager.clear();

        Subprocesso sp =
                subprocessoRepo.findByProcessoCodigo(processoDto.getCodigo()).stream()
                        .findFirst()
                        .orElseThrow();
        Mapa mapaRevisao = mapaRepo.findById(201L).orElseThrow();
        sp.setMapa(mapaRevisao);
        subprocessoRepo.save(sp);

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
                    .andExpect(status().isConflict());
        }
    }
}
