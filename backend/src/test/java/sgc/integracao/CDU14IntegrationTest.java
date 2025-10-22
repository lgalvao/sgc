package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import sgc.alerta.modelo.AlertaRepo;
import sgc.analise.modelo.AnaliseRepo;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockChefe;
import sgc.integracao.mocks.WithMockGestor;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapa;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoDto;
import sgc.sgrh.Perfil;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CDU-14: Analisar revisão de cadastro de atividades e conhecimentos")
@Import(sgc.integracao.mocks.TestSecurityConfig.class)
class CDU14IntegrationTest {    @Autowired
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
    private WebApplicationContext context;

    private Long subprocessoId;
    private Unidade unidade, unidadeGestor, unidadeAdmin;
    private Usuario chefe;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = webAppContextSetup(context).apply(springSecurity()).build();

        // Limpeza de dados
        subprocessoRepo.deleteAll();
        mapaRepo.deleteAll();
        atividadeRepo.deleteAll();
        unidadeMapaRepo.deleteAll();
        alertaRepo.deleteAll();
        analiseRepo.deleteAll();

        // Hierarquia de Unidades e Usuários (carregados do data.sql)
        unidadeAdmin = unidadeRepo.findById(100L).orElseThrow();
        unidadeGestor = unidadeRepo.findById(101L).orElseThrow();
        unidade = unidadeRepo.findById(102L).orElseThrow();
        chefe = usuarioRepo.findById(333333333333L).orElseThrow();

        // Mapa Vigente e suas atividades (setup manual, pois não há API para isso)
        Mapa mapaVigente = criarMapa();
        criarAtividade(mapaVigente, "Atividade Vigente 1");
        criarAtividade(mapaVigente, "Atividade Vigente 2");
        criarUnidadeMapa(unidade.getCodigo(), mapaVigente.getCodigo());

        // Criação do Processo via API
        var criarProcessoReq = new CriarProcessoReq(
            "Processo de Revisão Teste",
            "REVISAO",
            LocalDateTime.now().plusDays(10),
            List.of(unidade.getCodigo())
        );
        String criarProcessoJson = objectMapper.writeValueAsString(criarProcessoReq);

        var processoResult = mockMvc.perform(post("/api/processos")
                .with(csrf())
                .contentType("application/json")
                .content(criarProcessoJson))
            .andExpect(status().isCreated())
            .andReturn();
        var processoDto = objectMapper.readValue(processoResult.getResponse().getContentAsString(), ProcessoDto.class);

        // Iniciar Processo via API para criar o subprocesso
        mockMvc.perform(post("/api/processos/{id}/iniciar", processoDto.getCodigo())
                .with(csrf())
                .param("tipo", "REVISAO"))
            .andExpect(status().isOk());

        // Obter o ID do subprocesso criado a partir dos detalhes do processo
        var detalhesResult = mockMvc.perform(get("/api/processos/{id}/detalhes", processoDto.getCodigo()))
            .andExpect(status().isOk())
            .andReturn();
        var detalhesDto = objectMapper.readValue(detalhesResult.getResponse().getContentAsString(), ProcessoDetalheDto.class);
        this.subprocessoId = detalhesDto.getResumoSubprocessos().stream()
            .filter(s -> s.getUnidadeCodigo().equals(unidade.getCodigo()))
            .findFirst().orElseThrow()
            .getCodigo();

        // Disponibilizar o cadastro para análise (ação do Chefe)
        mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao-cadastro", this.subprocessoId)
                .with(csrf())
                .with(user(chefe.getTituloEleitoral().toString())))
            .andExpect(status().isOk());
    }

    @Nested
    @DisplayName("Testes para o fluxo de 'Devolver para Ajustes'")
    class DevolverCadastroTest {
        @Test
        @DisplayName("GESTOR deve devolver revisão do cadastro, alterar status, registrar análise, movimentação e alerta")
        @WithMockGestor
        void testGestorDevolveRevisaoCadastro() throws Exception {
            // Ação
            mockMvc.perform(post("/api/subprocessos/{id}/devolver-revisao-cadastro", subprocessoId)
                            .with(csrf())
                            .contentType("application/json")
                            .content("{\"motivo\": \"Teste\", \"observacoes\": \"Ajustar X, Y, Z\"}"))
                    .andExpect(status().isOk());

            // Verificações via API
            mockMvc.perform(get("/api/subprocessos/{id}", subprocessoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("REVISAO_CADASTRO_EM_ANDAMENTO"));

            // Verificações diretas no banco (para análise, movimentação e alertas, que não possuem GET específico)
            var analises = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoId);
            assertThat(analises).hasSize(1);
            assertThat(analises.getFirst().getAcao()).isEqualTo(sgc.analise.modelo.TipoAcaoAnalise.DEVOLUCAO_REVISAO);
            assertThat(analises.getFirst().getObservacoes()).isEqualTo("Ajustar X, Y, Z");
            assertThat(analises.getFirst().getUnidadeSigla()).isEqualTo(unidadeGestor.getSigla());

            mockMvc.perform(get("/api/subprocessos/{id}/historico-revisao-cadastro", subprocessoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unidadeDestino.sigla").value(unidade.getSigla()));

            var alertas = alertaRepo.findAll();
            assertThat(alertas).hasSize(1);
            assertThat(alertas.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidade.getSigla());
            assertThat(alertas.getFirst().getDescricao()).contains("devolvido para ajustes");
        }
    }

    @Nested
    @DisplayName("Testes para o fluxo de 'Registrar Aceite'")
    class AceitarCadastroTest {
        @Test
        @DisplayName("GESTOR deve aceitar revisão do cadastro, registrar análise, e mover para unidade superior")
        @WithMockGestor
        void testGestorAceitaRevisaoCadastro() throws Exception {
            mockMvc.perform(post("/api/subprocessos/{id}/aceitar-revisao-cadastro", subprocessoId)
                            .with(csrf())
                            .contentType("application/json")
                            .content("{\"observacoes\": \"Tudo certo.\"}"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/subprocessos/{id}", subprocessoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("AGUARDANDO_HOMOLOGACAO_CADASTRO"));

            var analises = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoId);
            assertThat(analises).hasSize(1);
            assertThat(analises.getFirst().getAcao()).isEqualTo(sgc.analise.modelo.TipoAcaoAnalise.ACEITE_REVISAO);
            assertThat(analises.getFirst().getUnidadeSigla()).isEqualTo(unidadeGestor.getSigla());

            mockMvc.perform(get("/api/subprocessos/{id}/historico-revisao-cadastro", subprocessoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unidadeOrigem.sigla").value(unidadeGestor.getSigla()))
                .andExpect(jsonPath("$[0].unidadeDestino.sigla").value(unidadeAdmin.getSigla()));
        }
    }

    @Nested
    @DisplayName("Testes para o fluxo de 'Homologar'")
    class HomologarCadastroTest {
        @BeforeEach
        void setup() {
            // Este setup anula o setup da classe pai para criar um subprocesso
            // diretamente no estado AGUARDANDO_HOMOLOGACAO_CADASTRO.
            // A lógica para chegar a este estado via API já foi testada no `AceitarCadastroTest`.
            // Para manter o teste de homologação focado, criamos o estado manualmente.
            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            sp.setSituacao(SituacaoSubprocesso.AGUARDANDO_HOMOLOGACAO_CADASTRO);
            subprocessoRepo.save(sp);
        }

        @Test
        @DisplayName("ADMIN deve homologar revisão do cadastro SEM impactos, alterando status para MAPA_HOMOLOGADO")
        @WithMockAdmin
        void testHomologarRevisaoCadastro_SemImpactos() throws Exception {
            // Cenário: Garantir que o mapa do subprocesso seja idêntico ao mapa vigente
            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            Mapa mapaVigente = mapaRepo.findById(unidadeMapaRepo.findByUnidadeCodigo(unidade.getCodigo()).orElseThrow().getMapaVigenteCodigo()).orElseThrow();
            List<Atividade> atividadesVigentes = atividadeRepo.findByMapaCodigo(mapaVigente.getCodigo());
            atividadeRepo.saveAll(atividadesVigentes.stream()
                .map(a -> new Atividade(sp.getMapa(), a.getDescricao()))
                .toList());

            // Ação
            String jsonContent = objectMapper.writeValueAsString(Map.of("observacoes", "Homologado sem impactos"));
            mockMvc.perform(post("/api/subprocessos/{codigo}/homologar-revisao-cadastro", subprocessoId)
                            .with(csrf())
                            .contentType("application/json")
                            .content(jsonContent))
                    .andExpect(status().isOk());

            // Verificações
            mockMvc.perform(get("/api/subprocessos/{id}", subprocessoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("REVISAO_CADASTRO_HOMOLOGADA"));

            mockMvc.perform(get("/api/subprocessos/{id}/historico-revisao-cadastro", subprocessoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Cadastro de atividades e conhecimentos homologado"))
                .andExpect(jsonPath("$[0].unidadeOrigem.sigla").value(unidadeAdmin.getSigla()))
                .andExpect(jsonPath("$[0].unidadeDestino.sigla").value(unidadeAdmin.getSigla()));
        }

        @Test
        @DisplayName("ADMIN deve homologar revisão do cadastro COM impactos, alterando status para REVISAO_CADASTRO_HOMOLOGADA e criando movimentação")
        @WithMockAdmin
        void testHomologarRevisaoCadastro_ComImpactos() throws Exception {
            // Cenário: Adicionar uma atividade extra para simular impacto
            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            atividadeRepo.save(new Atividade(sp.getMapa(), "Atividade com impacto extra"));


            // Ação
            String jsonContent = objectMapper.writeValueAsString(Map.of("observacoes", "Homologado com impactos"));
            mockMvc.perform(post("/api/subprocessos/{id}/homologar-revisao-cadastro", subprocessoId)
                            .with(csrf())
                            .contentType("application/json")
                            .content(jsonContent))
                    .andExpect(status().isOk());

            // Verificações
            mockMvc.perform(get("/api/subprocessos/{id}", subprocessoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("REVISAO_CADASTRO_HOMOLOGADA"));

            mockMvc.perform(get("/api/subprocessos/{id}/historico-revisao-cadastro", subprocessoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Cadastro de atividades e conhecimentos homologado"))
                .andExpect(jsonPath("$[0].unidadeOrigem.sigla").value(unidadeAdmin.getSigla()));
        }
    }

    // Métodos auxiliares para criação de entidades
    private Mapa criarMapa() {
        return mapaRepo.save(new Mapa());
    }

    private Atividade criarAtividade(Mapa mapa, String descricao) {
        return atividadeRepo.save(new Atividade(mapa, descricao));
    }

    private UnidadeMapa criarUnidadeMapa(Long unidadeCodigo, Long mapaVigenteCodigo) {
        UnidadeMapa unidadeMapa = new sgc.mapa.modelo.UnidadeMapa(unidadeCodigo);
        unidadeMapa.setMapaVigenteCodigo(mapaVigenteCodigo);
        unidadeMapa.setDataVigencia(LocalDateTime.now());
        return unidadeMapaRepo.save(unidadeMapa);
    }
}