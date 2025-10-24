package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoDto;
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

import static org.assertj.core.api.Assertions.assertThat;
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

    private Long subprocessoId;
    private Unidade unidade, unidadeGestor, unidadeAdmin;
    private Usuario chefe, gestor, admin;

    @BeforeEach
    void setUp() throws Exception {
        // Carregar unidades e usuários do SQL
        unidadeAdmin = unidadeRepo.findById(100L).orElseThrow();
        unidadeGestor = unidadeRepo.findById(101L).orElseThrow();
        unidade = unidadeRepo.findById(102L).orElseThrow();
        admin = usuarioRepo.findById(111111111111L).orElseThrow();
        gestor = usuarioRepo.findById(222222222222L).orElseThrow();
        chefe = usuarioRepo.findById(333333333333L).orElseThrow();

        // Associar titulares às unidades
        unidadeAdmin.setTitular(admin);
        unidadeGestor.setTitular(gestor);
        unidade.setTitular(chefe);
        unidadeRepo.saveAll(List.of(unidadeAdmin, unidadeGestor, unidade));

        // Criar mapa vigente para a unidade
        Mapa mapaVigente = criarMapaComCompetenciasEAtividades();
        criarUnidadeMapa(unidade.getCodigo(), mapaVigente.getCodigo());

        // Criar e iniciar processo de revisão
        ProcessoDto processoDto = criarEIniciarProcessoDeRevisao();

        // Obter ID do subprocesso
        subprocessoId = obterSubprocessoId(processoDto.getCodigo());

        // Mudar estado do subprocesso para o início do fluxo do CDU-14
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
            // Chefe disponibiliza
            mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId)
                    .with(csrf()).with(user(chefe.getTituloEleitoral().toString())))
                .andExpect(status().isOk());

            // Gestor devolve
            mockMvc.perform(post("/api/subprocessos/{id}/devolver-revisao-cadastro", subprocessoId)
                    .with(csrf()).with(user(gestor.getTituloEleitoral().toString()))
                    .contentType("application/json")
                    .content("{\"motivo\": \"Teste\", \"observacoes\": \"Ajustar\"}"))
                .andExpect(status().isOk());

            // Assertivas
            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoId)).hasSize(1);
            assertThat(alertaRepo.findAll()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Fluxo de Aceite")
    class Aceite {
        @Test
        @DisplayName("GESTOR deve aceitar, alterando status e movendo para unidade superior")
        void gestorAceitaRevisao() throws Exception {
            // Chefe disponibiliza
            mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId)
                .with(csrf()).with(user(chefe.getTituloEleitoral().toString())))
                .andExpect(status().isOk());

            // Gestor aceita
            mockMvc.perform(post("/api/subprocessos/{id}/aceitar-revisao-cadastro", subprocessoId)
                .with(csrf()).with(user(gestor.getTituloEleitoral().toString()))
                .contentType("application/json")
                .content("{\"observacoes\": \"OK\"}"))
                .andExpect(status().isOk());

            // Assertivas
            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.AGUARDANDO_HOMOLOGACAO_CADASTRO);
        }
    }

    @Nested
    @DisplayName("Fluxo de Homologação")
    class Homologacao {
        @BeforeEach
        void setUpHomologacao() throws Exception {
            // Chefe disponibiliza e Gestor aceita
            mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId)
                .with(csrf()).with(user(chefe.getTituloEleitoral().toString())))
                .andExpect(status().isOk());
            mockMvc.perform(post("/api/subprocessos/{id}/aceitar-revisao-cadastro", subprocessoId)
                .with(csrf()).with(user(gestor.getTituloEleitoral().toString()))
                .contentType("application/json")
                .content("{\"observacoes\": \"OK\"}"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN homologa SEM impactos, alterando status para MAPA_HOMOLOGADO")
        void adminHomologaSemImpactos() throws Exception {
            // ADMIN homologa
            mockMvc.perform(post("/api/subprocessos/{id}/homologar-revisao-cadastro", subprocessoId)
                .with(csrf()).with(user(admin.getTituloEleitoral().toString()))
                .contentType("application/json")
                .content("{\"observacoes\": \"Homologado\"}"))
                .andExpect(status().isOk());

            // Assertivas
            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        }

        @Test
        @DisplayName("ADMIN homologa COM impactos, alterando status para REVISAO_CADASTRO_HOMOLOGADA")
        void adminHomologaComImpactos() throws Exception {
            // Adicionar atividade para gerar impacto
            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            atividadeRepo.save(new Atividade(sp.getMapa(), "Nova Atividade de Impacto"));

            // ADMIN homologa
            mockMvc.perform(post("/api/subprocessos/{id}/homologar-revisao-cadastro", subprocessoId)
                .with(csrf()).with(user(admin.getTituloEleitoral().toString()))
                .contentType("application/json")
                .content("{\"observacoes\": \"Homologado com impacto\"}"))
                .andExpect(status().isOk());

            // Assertivas
            sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        }
    }

    // Métodos auxiliares
    private Mapa criarMapaComCompetenciasEAtividades() {
        Mapa mapa = mapaRepo.save(new Mapa());
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

    private ProcessoDto criarEIniciarProcessoDeRevisao() throws Exception {
        var criarReq = new CriarProcessoReq("Processo Revisão", "REVISAO", LocalDateTime.now().plusDays(10), List.of(unidade.getCodigo()));
        String reqJson = objectMapper.writeValueAsString(criarReq);

        String resJson = mockMvc.perform(post("/api/processos")
                .with(csrf()).with(user(gestor.getTituloEleitoral().toString()))
                .contentType("application/json").content(reqJson))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

        ProcessoDto processoDto = objectMapper.readValue(resJson, ProcessoDto.class);

        mockMvc.perform(post("/api/processos/{id}/iniciar", processoDto.getCodigo())
                .with(csrf()).with(user(gestor.getTituloEleitoral().toString()))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(List.of(unidade.getCodigo()))))
            .andExpect(status().isOk());

        return processoDto;
    }

    private Long obterSubprocessoId(Long processoId) throws Exception {
        String resJson = mockMvc.perform(get("/api/processos/{id}/detalhes", processoId)
                .with(user(gestor.getTituloEleitoral().toString())))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        ProcessoDetalheDto detalhes = objectMapper.readValue(resJson, ProcessoDetalheDto.class);
        return detalhes.getResumoSubprocessos().get(0).getCodigo();
    }
}
