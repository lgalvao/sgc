package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.alerta.model.AlertaRepo;
import sgc.analise.model.AnaliseRepo;
import sgc.fixture.MapaFixture;
import sgc.integracao.mocks.TestEventConfig;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.mapa.model.*;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.ProcessoDto;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@DisplayName("CDU-14: Analisar revisão de cadastro de atividades e conhecimentos")
@Import({
                TestSecurityConfig.class,
                TestThymeleafConfig.class,
                TestEventConfig.class
})
@Transactional
class CDU14IntegrationTest extends BaseIntegrationTest {
        private static final String APPLICATION_JSON = "application/json";
        private static final String API_SUBPROCESSOS_ID_DISPONIBILIZAR = "/api/subprocessos/{id}/disponibilizar-revisao";
        private static final String API_SUBPROCESSOS_ID_ACEITAR = "/api/subprocessos/{id}/aceitar-revisao-cadastro";
        private static final String API_SUBPROCESSOS_ID_HOMOLOGAR = "/api/subprocessos/{id}/homologar-revisao-cadastro";
        private static final String JSON_OBS_OK = "{\"observacoes\": \"OK\"}";

        @Autowired
        private UsuarioFacade usuarioService;
        @Autowired
        private AlertaRepo alertaRepo;
        @Autowired
        private AnaliseRepo analiseRepo;
        @Autowired
        private ConhecimentoRepo conhecimentoRepo;
        @Autowired
        private CompetenciaRepo competenciaRepo;
        @Autowired
        private MovimentacaoRepo movimentacaoRepo;
        @Autowired
        private EntityManager entityManager;

        private Unidade unidadeChefe;
        private Usuario chefe;
        private Usuario gestor;
        private Usuario admin;

        @BeforeEach
            void setUp() {
                // Use existing users and units from data.sql
                // Hierarchy: unit 100 (ADMIN) → unit 6 (GESTOR - COSIS) → unit 9 (CHEFE -
                // SEDIA)
                // Users: 111111111111 (ADMIN), 666666666666 (GESTOR), 333333333333 (CHEFE on
                // unit 9)

                admin = usuarioService.buscarPorLogin("111111111111");
                admin.setPerfilAtivo(Perfil.ADMIN);
                admin.setUnidadeAtivaCodigo(100L);
                admin.setAuthorities(Set.of(Perfil.ADMIN.toGrantedAuthority()));

                gestor = usuarioService.buscarPorLogin("666666666666");
                gestor.setPerfilAtivo(Perfil.GESTOR);
                gestor.setUnidadeAtivaCodigo(6L);
                gestor.setAuthorities(Set.of(Perfil.GESTOR.toGrantedAuthority()));

                chefe = usuarioService.buscarPorLogin("333333333333");
                chefe.setPerfilAtivo(Perfil.CHEFE);
                chefe.setUnidadeAtivaCodigo(9L);
                chefe.setAuthorities(Set.of(Perfil.CHEFE.toGrantedAuthority()));

                unidadeChefe = unidadeRepo.findById(9L).orElseThrow();

                // Unit 9 already has mapa 1002 in data.sql, so use it
                // Add test data to mapa 1002 if needed
                Mapa mapaVigente = mapaRepo.findById(1002L).orElseGet(() -> {
                        Mapa novoMapa = MapaFixture.mapaPadrao(null);
                        novoMapa.setCodigo(1002L);
                        return mapaRepo.save(novoMapa);
                });

                // Ensure mapa has at least one atividade with conhecimento for validation
                if (atividadeRepo.findByMapa_Codigo(mapaVigente.getCodigo()).isEmpty()) {
                        Atividade atividade = Atividade.builder().mapa(mapaVigente).descricao("Atividade CDU-14 Test")
                                        .build();
                        atividade = atividadeRepo.save(atividade);

                        Conhecimento conhecimento = Conhecimento.builder().descricao("Conhecimento CDU-14 Test")
                                        .atividade(atividade).build();
                        conhecimento = conhecimentoRepo.save(conhecimento);

                        atividade.getConhecimentos().add(conhecimento);

                        // Add competência for impact testing
                        Competencia competencia = Competencia.builder().descricao("Competencia CDU-14 Test")
                                        .mapa(mapaVigente).build();
                        competencia = competenciaRepo.save(competencia);
                        atividade.getCompetencias().add(competencia);
                        competencia.getAtividades().add(atividade);

                        atividadeRepo.save(atividade);
                }
        }

        private Long criarEComecarProcessoDeRevisao() throws Exception {
                ProcessoDto processoDto = criarEIniciarProcessoDeRevisao();

                Subprocesso sp = subprocessoRepo.findByProcessoCodigo(processoDto.getCodigo()).stream()
                                .findFirst()
                                .orElseThrow();
                sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
                subprocessoRepo.save(sp);
                return sp.getCodigo();
        }

        private ProcessoDto criarEIniciarProcessoDeRevisao() throws Exception {
                Map<String, Object> criarReqMap = Map.of(
                                "descricao",
                                "Processo Revisão CDU-14",
                                "tipo",
                                "REVISAO",
                                "dataLimiteEtapa1",
                                LocalDateTime.now()
                                                .plusDays(10)
                                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                                "unidades",
                                List.of(unidadeChefe.getCodigo()));
                String reqJson = objectMapper.writeValueAsString(criarReqMap);

                String resJson = mockMvc.perform(
                                post("/api/processos")
                                                .with(csrf())
                                                .with(user(admin))
                                                .contentType(APPLICATION_JSON)
                                                .content(reqJson))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                ProcessoDto processoDto = objectMapper.readValue(resJson, ProcessoDto.class);

                Map<String, Object> iniciarReqMap = Map.of("tipo", "REVISAO", "unidades",
                                List.of(unidadeChefe.getCodigo()));
                String iniciarReqJson = objectMapper.writeValueAsString(iniciarReqMap);

                mockMvc.perform(
                                post("/api/processos/{codigo}/iniciar", processoDto.getCodigo())
                                                .with(csrf())
                                                .with(user(admin))
                                                .contentType(APPLICATION_JSON)
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
                        mockMvc.perform(post(API_SUBPROCESSOS_ID_DISPONIBILIZAR, subprocessoId)
                                        .with(csrf())
                                        .with(user(chefe)))
                                        .andExpect(status().isOk());

                        mockMvc.perform(
                                        post("/api/subprocessos/{id}/devolver-revisao-cadastro", subprocessoId)
                                                        .with(csrf())
                                                        .with(user(gestor))
                                                        .contentType(APPLICATION_JSON)
                                                        .content(
                                                                        "{\"motivo\": \"Teste\", \"observacoes\":"
                                                                                        + " \"Ajustar\"}"))
                                        .andExpect(status().isOk());

                        Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
                        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
                        assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoId)).hasSize(1);
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
                        mockMvc.perform(post(API_SUBPROCESSOS_ID_DISPONIBILIZAR, subprocessoId)
                                        .with(csrf())
                                        .with(user(chefe)))
                                        .andExpect(status().isOk());

                        mockMvc.perform(
                                        post(API_SUBPROCESSOS_ID_ACEITAR, subprocessoId)
                                                        .with(csrf())
                                                        .with(user(gestor))
                                                        .contentType(APPLICATION_JSON)
                                                        .content(JSON_OBS_OK))
                                        .andExpect(status().isOk());

                        Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
                        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
                        assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoId)).hasSize(1);
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
                                        post(API_SUBPROCESSOS_ID_DISPONIBILIZAR, subprocessoId)
                                                        .with(csrf())
                                                        .with(user(chefe)))
                                        .andExpect(status().isOk());
                        mockMvc.perform(
                                        post(API_SUBPROCESSOS_ID_ACEITAR, subprocessoId)
                                                        .with(csrf())
                                                        .with(user(gestor))
                                                        .contentType(APPLICATION_JSON)
                                                        .content(JSON_OBS_OK))
                                        .andExpect(status().isOk());
                }

                @Test
                @DisplayName("ADMIN homologa SEM impactos, alterando status para MAPA_HOMOLOGADO")
                void adminHomologaSemImpactos() throws Exception {
                        mockMvc.perform(post(API_SUBPROCESSOS_ID_HOMOLOGAR, subprocessoId)
                                        .with(csrf())
                                        .with(user(admin))
                                        .contentType(APPLICATION_JSON)
                                        .content("{\"observacoes\": \"Homologado\"}"))
                                        .andExpect(status().isOk());

                        Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
                        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO);
                }

                @Test
                @DisplayName("ADMIN homologa COM impactos, alterando status e criando movimentação")
                void adminHomologaComImpactos() throws Exception {
                        Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();

                        Atividade atividadeExistente = atividadeRepo.findByMapa_Codigo(sp.getMapa().getCodigo()).stream()
                                        .findFirst()
                                        .orElseThrow();
                        atividadeRepo.delete(atividadeExistente);
                        entityManager.flush();

                        mockMvc.perform(post(API_SUBPROCESSOS_ID_HOMOLOGAR, subprocessoId)
                                        .with(csrf())
                                        .with(user(admin))
                                        .contentType(APPLICATION_JSON)
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
                        mockMvc.perform(post(API_SUBPROCESSOS_ID_DISPONIBILIZAR, subprocessoId)
                                        .with(csrf())
                                        .with(user(chefe)))
                                        .andExpect(status().isOk());

                        mockMvc.perform(
                                        post("/api/subprocessos/{id}/devolver-revisao-cadastro", subprocessoId)
                                                        .with(csrf())
                                                        .with(user(gestor))
                                                        .contentType(APPLICATION_JSON)
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

                        Atividade atividadeExistente = atividadeRepo.findByMapa_Codigo(sp.getMapa().getCodigo()).stream()
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
                                                        jsonPath("$.competenciasImpactadas[0].atividades",
                                                                        hasSize(1)));
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
                                        post(API_SUBPROCESSOS_ID_DISPONIBILIZAR, subprocessoId)
                                                        .with(csrf())
                                                        .with(user(chefe)))
                                        .andExpect(status().isOk());
                        mockMvc.perform(
                                        post(API_SUBPROCESSOS_ID_ACEITAR, subprocessoId)
                                                        .with(csrf())
                                                        .with(user(gestor))
                                                        .contentType(APPLICATION_JSON)
                                                        .content(JSON_OBS_OK))
                                        .andExpect(status().isOk());

                        mockMvc.perform(
                                        post(API_SUBPROCESSOS_ID_HOMOLOGAR, subprocessoId)
                                                        .with(csrf())
                                                        .with(user(chefe))
                                                        .contentType(APPLICATION_JSON)
                                                        .content("{\"observacoes\": \"Tudo certo por mim\"}"))
                                        .andExpect(status().isForbidden());
                }

                @Test
                @DisplayName("Não pode homologar em estado inválido")
                void naoPodeHomologarEmEstadoInvalido() throws Exception {
                        Long subprocessoId = criarEComecarProcessoDeRevisao();

                        // Após refatoração de segurança, a validação de estado é feita no
                        // AccessControlService
                        // Retorna 403 (Forbidden) em vez de 422 (Unprocessable Entity)
                        // Isso é mais correto do ponto de vista de segurança: verificar permissões
                        // antes de validações de negócio
                        mockMvc.perform(
                                        post(API_SUBPROCESSOS_ID_HOMOLOGAR, subprocessoId)
                                                        .with(csrf())
                                                        .with(user(admin))
                                                        .contentType(APPLICATION_JSON)
                                                        .content("{\"observacoes\": \"Homologado fora de hora\"}"))
                                        .andExpect(status().isForbidden());
                }
        }
}
