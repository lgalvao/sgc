package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;
import sgc.comum.*;
import sgc.fixture.*;
import sgc.mapa.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.time.format.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
class CDU14IntegrationTest extends BaseIntegrationTest {
    private static final String APPLICATION_JSON = "application/json";
    private static final String API_SUBPROCESSOS_ID_DISPONIBILIZAR = "/api/subprocessos/{codigo}/disponibilizar-revisao";
    private static final String API_SUBPROCESSOS_ID_ACEITAR = "/api/subprocessos/{codigo}/aceitar-revisao-cadastro";
    private static final String API_SUBPROCESSOS_ID_HOMOLOGAR = "/api/subprocessos/{codigo}/homologar-revisao-cadastro";
    private static final String JSON_TEXTO_OK = "{\"texto\": \"OK\"}";

    @Autowired
    private UsuarioAplicacaoService usuarioService;
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
    private NotificacaoEmailRepo notificacaoEmailRepo;
    @Autowired
    private EntityManager entityManager;
    private Unidade unidadeChefe;
    private Usuario chefe;
    private Usuario gestor;
    private Usuario admin;

    @BeforeEach
    void setUp() {
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

        Mapa mapaVigente = mapaRepo.findById(1002L).orElseGet(() -> {
            Processo processoVigente = ProcessoFixture.processoPadrao();
            processoVigente.setCodigo(null);
            processoVigente.setTipo(TipoProcesso.MAPEAMENTO);
            processoVigente.setSituacao(SituacaoProcesso.FINALIZADO);
            processoVigente.setDataFinalizacao(LocalDateTime.now().minusDays(1));
            processoVigente = processoRepo.save(processoVigente);

            Subprocesso subprocessoVigente = SubprocessoFixture.subprocessoPadrao(processoVigente, unidadeChefe);
            subprocessoVigente.setCodigo(null);
            subprocessoVigente.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
            subprocessoVigente.setDataFimEtapa1(LocalDateTime.now().minusDays(2));
            subprocessoVigente.setDataFimEtapa2(LocalDateTime.now().minusDays(1));
            subprocessoVigente = subprocessoRepo.save(subprocessoVigente);

            Mapa novoMapa = MapaFixture.mapaPadrao(subprocessoVigente);
            novoMapa.setCodigo(1002L);
            novoMapa = mapaRepo.save(novoMapa);
            subprocessoVigente.setMapa(novoMapa);
            subprocessoRepo.save(subprocessoVigente);
            return novoMapa;
        });

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
        Processo processo = criarEIniciarProcessoDeRevisao();

        Subprocesso sp = subprocessoRepo.listarPorProcessoComUnidade(processo.getCodigo()).stream()
                .findFirst()
                .orElseThrow();
        sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.save(sp);
        return sp.getCodigo();
    }

    private Processo criarEIniciarProcessoDeRevisao() throws Exception {
        Map<String, Object> criarReqMap = Map.of(
                "descricao",
                "Processo revisão CDU-14",
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

        Processo processo = objectMapper.readValue(resJson, Processo.class);

        Map<String, Object> iniciarReqMap = Map.of("tipo", "REVISAO", "unidades",
                List.of(unidadeChefe.getCodigo()));
        String iniciarReqJson = objectMapper.writeValueAsString(iniciarReqMap);

        mockMvc.perform(
                        post("/api/processos/{codigo}/iniciar", processo.getCodigo())
                                .with(csrf())
                                .with(user(admin))
                                .contentType(APPLICATION_JSON)
                                .content(iniciarReqJson))
                .andExpect(status().isOk());

        entityManager.flush();

        return processo;
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
                            post("/api/subprocessos/{codigo}/devolver-revisao-cadastro", subprocessoId)
                                    .with(csrf())
                                    .with(user(gestor))
                                    .contentType(APPLICATION_JSON)
                                    .content(
                                            "{\"motivo\": \"Teste\", \"justificativa\":"
                                                    + " \"Ajustar\"}"))
                    .andExpect(status().isOk());

            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoId)).hasSize(1);
            List<Alerta> alertas = alertaRepo.findByProcessoCodigo(sp.getProcesso().getCodigo());
            assertThat(alertas).anySatisfy(alerta -> {
                assertThat(alerta.getUnidadeDestino().getSigla()).isEqualTo(unidadeChefe.getSigla());
                assertThat(alerta.getDescricao())
                        .isEqualTo(Mensagens.ALERTA_REVISAO_DEVOLVIDA.formatted(unidadeChefe.getSigla()));
            });
            assertThat(alertas.stream()
                    .filter(alerta -> Mensagens.ALERTA_REVISAO_DEVOLVIDA.formatted(unidadeChefe.getSigla())
                            .equals(alerta.getDescricao()))
                    .map(alerta -> alerta.getUnidadeDestino().getSigla())
                    .toList())
                    .containsExactly(unidadeChefe.getSigla());
            assertThat(movimentacaoRepo.findBySubprocessoCodigo(subprocessoId)).hasSize(3);

            List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll().stream()
                    .filter(n -> n.getTipoNotificacao() == TipoNotificacao.REVISAO_CADASTRO_DEVOLVIDA)
                    .toList();
            assertThat(notificacoes).anySatisfy(notificacao -> {
                assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo(unidadeChefe.getSigla());
                assertThat(notificacao.getDestinatario()).isEqualTo("%s@tre-pe.jus.br".formatted(unidadeChefe.getSigla().toLowerCase(Locale.ROOT)));
                assertThat(notificacao.getAssunto())
                        .isEqualTo("SGC: Revisão do cadastro de atividades e conhecimentos da %s devolvida para ajustes"
                                .formatted(unidadeChefe.getSigla()));
                assertThat(notificacao.getCorpoHtml())
                        .contains("foi devolvida para ajustes")
                        .contains("Processo revisão CDU-14")
                        .contains("Justificativa:")
                        .contains("Ajustar");
                assertThat(notificacao.getSituacao()).isIn(SituacaoNotificacao.PENDENTE, SituacaoNotificacao.ENVIADO);
            });
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
                                    .content(JSON_TEXTO_OK))
                    .andExpect(status().isOk());

            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
            assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoId)).hasSize(1);
            List<Alerta> alertas = alertaRepo.findByProcessoCodigo(sp.getProcesso().getCodigo());
            assertThat(alertas.stream()
                    .map(Alerta::getDescricao)
                    .toList())
                    .contains(Mensagens.ALERTA_REVISAO_ACEITA.formatted(unidadeChefe.getSigla()));
            assertThat(alertas.stream()
                    .filter(alerta -> Mensagens.ALERTA_REVISAO_ACEITA.formatted(unidadeChefe.getSigla())
                            .equals(alerta.getDescricao()))
                    .map(alerta -> alerta.getUnidadeDestino().getSigla())
                    .toList())
                    .containsExactly("STIC");
            assertThat(movimentacaoRepo.findBySubprocessoCodigo(subprocessoId)).hasSize(3);

            // Esperamos pelo menos 2 e-mails: Início de Processo e Aceite
            aguardarEmail(2);
            assertThat(algumEmailContem("submetid")).isTrue();

            List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll().stream()
                    .filter(n -> n.getTipoNotificacao() == TipoNotificacao.REVISAO_CADASTRO_ACEITA)
                    .toList();
            assertThat(notificacoes).anySatisfy(notificacao -> {
                assertThat(notificacao.getAssunto())
                        .isEqualTo("SGC: Revisão do cadastro de atividades e conhecimentos da %s submetido para análise"
                                .formatted(unidadeChefe.getSigla()));
                assertThat(notificacao.getCorpoHtml())
                        .contains("foi submetida para análise por essa unidade")
                        .contains("Processo revisão CDU-14");
                assertThat(notificacao.getSituacao()).isIn(SituacaoNotificacao.PENDENTE, SituacaoNotificacao.ENVIADO);
            });
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
                                    .content(JSON_TEXTO_OK))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN homologa SEM impactos, alterando status para REVISAO_CADASTRO_HOMOLOGADA")
        void adminHomologaSemImpactos() throws Exception {
            admin.setUnidadeAtivaCodigo(2L); // No STIC após aceite do Gestor
            mockMvc.perform(post(API_SUBPROCESSOS_ID_HOMOLOGAR, subprocessoId)
                            .with(csrf())
                            .with(user(admin))
                            .contentType(APPLICATION_JSON)
                            .content("{\"texto\": \"Homologado\"}"))
                    .andExpect(status().isOk());

            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
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

            admin.setUnidadeAtivaCodigo(2L); // No STIC após aceite do Gestor
            mockMvc.perform(post(API_SUBPROCESSOS_ID_HOMOLOGAR, subprocessoId)
                            .with(csrf())
                            .with(user(admin))
                            .contentType(APPLICATION_JSON)
                            .content("{\"texto\": \"Homologado com impacto\"}"))
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
                            post("/api/subprocessos/{codigo}/devolver-revisao-cadastro", subprocessoId)
                                    .with(csrf())
                                    .with(user(gestor))
                                    .contentType(APPLICATION_JSON)
                                    .content(
                                            "{\"motivo\": \"Teste histórico\", \"justificativa\":"
                                                    + " \"Registrando análise\"}"))
                    .andExpect(status().isOk());

            mockMvc.perform(
                            get("/api/subprocessos/{codigo}/historico-cadastro", subprocessoId)
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
            sp.getMapa().getAtividades().remove(atividadeExistente);
            atividadeExistente.getCompetencias().forEach(comp -> comp.getAtividades().remove(atividadeExistente));
            atividadeRepo.delete(atividadeExistente);
            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(
                            get("/api/subprocessos/{codigo}/impactos-mapa", subprocessoId)
                                    .with(user(chefe)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.temImpactos", is(true)))
                    .andExpect(jsonPath("$.competenciasImpactadas", hasSize(1)))
                    .andExpect(
                            jsonPath("$.competenciasImpactadas[0].atividadesAfetadas",
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
                                    .content(JSON_TEXTO_OK))
                    .andExpect(status().isOk());

            mockMvc.perform(
                            post(API_SUBPROCESSOS_ID_HOMOLOGAR, subprocessoId)
                                    .with(csrf())
                                    .with(user(chefe))
                                    .contentType(APPLICATION_JSON)
                                    .content("{\"texto\": \"Tudo certo por mim\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Não pode homologar em estado inválido")
        void naoPodeHomologarEmEstadoInvalido() throws Exception {
            Long subprocessoId = criarEComecarProcessoDeRevisao();

            mockMvc.perform(
                            post(API_SUBPROCESSOS_ID_HOMOLOGAR, subprocessoId)
                                    .with(csrf())
                                    .with(user(admin))
                                    .contentType(APPLICATION_JSON)
                                    .content("{\"texto\": \"Homologado fora de hora\"}"))
                    .andExpect(status().isForbidden());
        }
    }
}
