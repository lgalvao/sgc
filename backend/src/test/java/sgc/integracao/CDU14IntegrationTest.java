package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.modelo.AlertaRepo;
import sgc.analise.modelo.AnaliseRepo;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapa;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoDto;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CDU-14: Analisar revisão de cadastro de atividades e conhecimentos")
@Sql("/create-test-data.sql")
@Transactional
class CDU14IntegrationTest {
    @Autowired
    private MockMvc mockMvc;
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
    private UnidadeMapaRepo unidadeMapaRepo;
    @Autowired
    private ConhecimentoRepo conhecimentoRepo;
    @Autowired
    private CompetenciaRepo competenciaRepo;
    @Autowired
    private CompetenciaAtividadeRepo competenciaAtividadeRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    private Long subprocessoId;
    private Unidade unidade;
    private Usuario chefe, gestor, admin;

    @BeforeEach
    void setUp() throws Exception {
        Unidade unidadeAdmin = unidadeRepo.findById(100L).orElseThrow();
        Unidade unidadeGestor = unidadeRepo.findById(101L).orElseThrow();
        unidade = unidadeRepo.findById(102L).orElseThrow();
        admin = usuarioRepo.findById(111111111111L).orElseThrow();
        gestor = usuarioRepo.findById(222222222222L).orElseThrow();
        chefe = usuarioRepo.findById(333333333333L).orElseThrow();

        unidadeAdmin.setTitular(admin);
        unidadeGestor.setTitular(gestor);
        unidade.setTitular(chefe);
        unidadeRepo.saveAll(List.of(unidadeAdmin, unidadeGestor, unidade));

        Mapa mapaVigente = criarMapaComCompetenciasEAtividades();
        System.out.println("CDU14IntegrationTest - setUp: Mapa Vigente Código: " + mapaVigente.getCodigo());
        criarUnidadeMapa(unidade.getCodigo(), mapaVigente.getCodigo());

        ProcessoDto processoDto = criarEIniciarProcessoDeRevisao(mapaVigente);

        subprocessoId = obterSubprocessoId(processoDto.getCodigo());

        Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.save(sp);
    }

    @Nested
    @DisplayName("Fluxo de Devolução")
    class Devolucao {
        @Test
        @DisplayName("GESTOR deve devolver, alterando status e criando registros")
        void gestorDevolveRevisao() throws Exception {
            mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId)
                            .with(csrf()).with(user(chefe)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/subprocessos/{id}/devolver-revisao-cadastro", subprocessoId)
                            .with(csrf()).with(user(gestor))
                            .contentType("application/json")
                            .content("{\"motivo\": \"Teste\", \"observacoes\": \"Ajustar\"}"))
                    .andExpect(status().isOk());

            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoId)).hasSize(1);
            assertThat(alertaRepo.findAll()).hasSize(2);
            assertThat(movimentacaoRepo.findBySubprocessoCodigo(subprocessoId)).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Fluxo de Aceite")
    class Aceite {
        @Test
        @DisplayName("GESTOR deve aceitar, alterando status e criando todos os registros")
        void gestorAceitaRevisao() throws Exception {
            mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId)
                            .with(csrf()).with(user(chefe)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/subprocessos/{id}/aceitar-revisao-cadastro", subprocessoId)
                            .with(csrf()).with(user(gestor))
                            .contentType("application/json")
                            .content("{\"observacoes\": \"OK\"}"))
                    .andExpect(status().isOk());

            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.AGUARDANDO_HOMOLOGACAO_CADASTRO);
            assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoId)).hasSize(1);
            assertThat(alertaRepo.findAll()).hasSize(2);
            assertThat(movimentacaoRepo.findBySubprocessoCodigo(subprocessoId)).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Fluxo de Homologação")
    class Homologacao {
        @BeforeEach
        void setUpHomologacao() throws Exception {
            mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId)
                            .with(csrf()).with(user(chefe)))
                    .andExpect(status().isOk());
            mockMvc.perform(post("/api/subprocessos/{id}/aceitar-revisao-cadastro", subprocessoId)
                            .with(csrf()).with(user(gestor))
                            .contentType("application/json")
                            .content("{\"observacoes\": \"OK\"}"))
                    .andExpect(status().isOk());

            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            System.out.println("CDU14IntegrationTest - setUpHomologacao: Subprocesso Situação: " + sp.getSituacao());
        }

        @Test
        @DisplayName("ADMIN homologa SEM impactos, alterando status para MAPA_HOMOLOGADO")
        void adminHomologaSemImpactos() throws Exception {
            mockMvc.perform(post("/api/subprocessos/{id}/homologar-revisao-cadastro", subprocessoId)
                            .with(csrf()).with(user(admin))
                            .contentType("application/json")
                            .content("{\"observacoes\": \"Homologado\"}"))
                    .andExpect(status().isOk());

            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        }

        @Test
        @DisplayName("ADMIN homologa COM impactos, alterando status e criando movimentação")
        void adminHomologaComImpactos() throws Exception {
            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();

            // Remover uma atividade existente do mapa do subprocesso
            Atividade atividadeExistente = atividadeRepo.findByMapaCodigo(sp.getMapa().getCodigo()).stream().findFirst().orElseThrow();
            atividadeRepo.delete(atividadeExistente);

            // Recarregar o subprocesso para garantir que o mapa esteja atualizado
            sp = subprocessoRepo.findById(subprocessoId).orElseThrow();

            mockMvc.perform(post("/api/subprocessos/{id}/homologar-revisao-cadastro", subprocessoId)
                            .with(csrf()).with(user(admin))
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
            mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId)
                            .with(csrf()).with(user(chefe)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/subprocessos/{id}/devolver-revisao-cadastro", subprocessoId)
                            .with(csrf()).with(user(gestor))
                            .contentType("application/json")
                            .content("{\"motivo\": \"Teste Histórico\", \"observacoes\": \"Registrando análise\"}"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/subprocessos/{id}/historico-cadastro", subprocessoId)
                            .with(user(gestor)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].observacoes", is("Registrando análise")));
        }

        @Test
        @DisplayName("Deve retornar impactos no mapa corretamente")
        void deveRetornarImpactosNoMapa() throws Exception {
            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();

            // Remover uma atividade existente do mapa do subprocesso
            Atividade atividadeExistente = atividadeRepo.findByMapaCodigo(sp.getMapa().getCodigo()).stream().findFirst().orElseThrow();
            atividadeRepo.delete(atividadeExistente);

            // Recarregar o subprocesso para garantir que o mapa esteja atualizado
            sp = subprocessoRepo.findById(subprocessoId).orElseThrow();

            mockMvc.perform(get("/api/subprocessos/{codigo}/impactos-mapa", subprocessoId)
                            .with(user(chefe))) // Trocado para CHEFE que tem a permissão
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.temImpactos", is(true)))
                    .andExpect(jsonPath("$.competenciasImpactadas", hasSize(1)))
                    .andExpect(jsonPath("$.competenciasImpactadas[0].atividadesAfetadas", hasSize(1)))
                    .andExpect(jsonPath("$.competenciasImpactadas[0].tipoImpacto", is("ATIVIDADE_REMOVIDA")));
        }
    }

    @Nested
    @DisplayName("Falhas e Segurança")
    class FalhasESeguranca {
        @Test
        @DisplayName("CHEFE não pode homologar revisão")
        void chefeNaoPodeHomologar() throws Exception {
            mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId)
                            .with(csrf()).with(user(chefe)))
                    .andExpect(status().isOk());
            mockMvc.perform(post("/api/subprocessos/{id}/aceitar-revisao-cadastro", subprocessoId)
                            .with(csrf()).with(user(gestor))
                            .contentType("application/json")
                            .content("{\"observacoes\": \"OK\"}"))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/subprocessos/{id}/homologar-revisao-cadastro", subprocessoId)
                            .with(csrf()).with(user(chefe))
                            .contentType("application/json")
                            .content("{\"observacoes\": \"Tudo certo por mim\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Não pode homologar em estado inválido")
        void naoPodeHomologarEmEstadoInvalido() throws Exception {
            mockMvc.perform(post("/api/subprocessos/{id}/homologar-revisao-cadastro", subprocessoId)
                            .with(csrf()).with(user(admin))
                            .contentType("application/json")
                            .content("{\"observacoes\": \"Homologado fora de hora\"}"))
                    .andExpect(status().isConflict());
        }
    }

    private Mapa criarMapaComCompetenciasEAtividades() {
        Mapa mapa = mapaRepo.save(new Mapa());
        System.out.println("CDU14IntegrationTest - criarMapaComCompetenciasEAtividades: Mapa Código: " + mapa.getCodigo());
        Competencia c1 = competenciaRepo.save(new Competencia("Competência Teste", mapa));
        Atividade a1 = atividadeRepo.save(new Atividade(mapa, "Atividade Teste"));
        conhecimentoRepo.save(new Conhecimento("Conhecimento Teste", a1));
        competenciaAtividadeRepo.save(new CompetenciaAtividade(new CompetenciaAtividade.Id(c1.getCodigo(), a1.getCodigo()), c1, a1));
        return mapa;
    }

    private void criarUnidadeMapa(Long unidadeCodigo, Long mapaCodigo) {
        UnidadeMapa um = new UnidadeMapa(unidadeCodigo);
        um.setMapaVigenteCodigo(mapaCodigo);
        um.setDataVigencia(LocalDateTime.now());
        unidadeMapaRepo.save(um);
    }

    private ProcessoDto criarEIniciarProcessoDeRevisao(Mapa mapaBase) throws Exception {
        Mapa mapaSubprocesso = copiarMapa(mapaBase);

        Map<String, Object> criarReqMap = Map.of(
                "descricao", "Processo Revisão",
                "tipo", "REVISAO",
                "dataLimiteEtapa1", LocalDateTime.now().plusDays(10).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                "unidades", List.of(unidade.getCodigo())
        );
        String reqJson = objectMapper.writeValueAsString(criarReqMap);

        String resJson = mockMvc.perform(post("/api/processos")
                        .with(csrf()).with(user(gestor))
                        .contentType("application/json").content(reqJson))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

        ProcessoDto processoDto = objectMapper.readValue(resJson, ProcessoDto.class);

        mockMvc.perform(post("/api/processos/{id}/iniciar", processoDto.getCodigo())
                        .param("tipo", "REVISAO")
                        .with(csrf()).with(user(gestor))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of(unidade.getCodigo()))))
                .andExpect(status().isOk());

        // Associar o mapa copiado ao subprocesso recém-criado
        Subprocesso sp = subprocessoRepo.findByProcessoCodigo(processoDto.getCodigo()).stream().findFirst().orElseThrow();
        sp.setMapa(mapaSubprocesso);
        subprocessoRepo.save(sp);

        return processoDto;
    }

    private Long obterSubprocessoId(Long processoId) throws Exception {
        String resJson = mockMvc.perform(get("/api/processos/{id}/detalhes", processoId)
                        .with(user(gestor)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        ProcessoDetalheDto detalhes = objectMapper.readValue(resJson, ProcessoDetalheDto.class);
        return detalhes.getResumoSubprocessos().getFirst().getCodigo();
    }

    private Mapa copiarMapa(Mapa mapaOriginal) {
        Mapa novoMapa = mapaRepo.save(new Mapa());

        // Copiar atividades do mapa original para o novo mapa
        atividadeRepo.findByMapaCodigo(mapaOriginal.getCodigo()).forEach(atividadeOriginal -> {
            Hibernate.initialize(atividadeOriginal.getConhecimentos()); // Inicializa a coleção
            Atividade novaAtividade = atividadeRepo.save(new Atividade(novoMapa, atividadeOriginal.getDescricao()));
            // Copiar conhecimentos da atividade original para a nova atividade
            conhecimentoRepo.findByAtividadeCodigo(atividadeOriginal.getCodigo())
                    .forEach(conhecimentoOriginal -> conhecimentoRepo.save(new Conhecimento(conhecimentoOriginal.getDescricao(), novaAtividade)));
        });

        // Copiar competências do mapa original para o novo mapa
        competenciaRepo.findByMapaCodigo(mapaOriginal.getCodigo()).forEach(competenciaOriginal -> {
            Competencia novaCompetencia = competenciaRepo.save(new Competencia(competenciaOriginal.getDescricao(), novoMapa));
            // Copiar vínculos CompetenciaAtividade
            competenciaAtividadeRepo.findByCompetenciaCodigo(competenciaOriginal.getCodigo()).forEach(caOriginal -> {
                // Procurar a nova atividade correspondente no novo mapa
                atividadeRepo.findByMapaCodigoAndDescricao(novoMapa.getCodigo(), caOriginal.getAtividade().getDescricao())
                        .ifPresent(novaAtividade -> competenciaAtividadeRepo.save(new CompetenciaAtividade(
                                new CompetenciaAtividade.Id(novaCompetencia.getCodigo(), novaAtividade.getCodigo()),
                                novaCompetencia, novaAtividade
                        )));
            });
        });
        return novoMapa;
    }
}
