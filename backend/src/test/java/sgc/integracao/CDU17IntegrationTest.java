package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.analise.model.Analise;
import sgc.analise.model.AnaliseRepo;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaAtividade;
import sgc.mapa.model.CompetenciaAtividadeRepo;
import sgc.mapa.model.CompetenciaRepo;
import sgc.comum.erros.ErroApi;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockGestor;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.model.UnidadeMapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.dto.DisponibilizarMapaReq;
import sgc.subprocesso.model.*;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-17: Disponibilizar Mapa de Competências")
@org.springframework.context.annotation.Import({TestSecurityConfig.class, sgc.integracao.mocks.TestThymeleafConfig.class})
class CDU17IntegrationTest {
    private static final String API_URL = "/api/subprocessos/{codigo}/disponibilizar-mapa";
    private static final String OBS_LITERAL = "Obs";
    private static final String SEDOC_LITERAL = "SEDOC";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Repositórios
    @Autowired
    private ProcessoRepo processoRepo;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private MapaRepo mapaRepo;
    @Autowired
    private AtividadeRepo atividadeRepo;
    @Autowired
    private CompetenciaRepo competenciaRepo;
    @Autowired
    private CompetenciaAtividadeRepo competenciaAtividadeRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private AnaliseRepo analiseRepo;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;

    private Unidade unidade;
    private Subprocesso subprocesso;
    private Mapa mapa;
    private Atividade atividade;
    private Competencia competencia;

    @BeforeEach
    void setUp() {
        // Limpar dados antes de cada teste
        movimentacaoRepo.deleteAll();
        alertaRepo.deleteAll();
        competenciaAtividadeRepo.deleteAll();
        atividadeRepo.deleteAll();
        competenciaRepo.deleteAll(); // Delete competencias before mapas
        unidadeMapaRepo.deleteAll(); // Delete unidade_mapa before mapas
        subprocessoRepo.deleteAll();
        mapaRepo.deleteAll();
        processoRepo.deleteAll();

        // Use existing users from data-postgresql.sql
        Usuario admin = usuarioRepo.findById(111111111111L).orElseThrow();
        Usuario gestor = usuarioRepo.findById(222222222222L).orElseThrow();
        Usuario titularUS = usuarioRepo.findById(1L).orElseThrow(); // Ana Paula Souza
        Usuario titularUT = usuarioRepo.findById(2L).orElseThrow(); // Carlos Henrique Lima

        // Use existing units from data-postgresql.sql
        Unidade sedoc = unidadeRepo.findById(15L).orElseThrow(); // SEDOC
        Unidade unidadeSuperior = unidadeRepo.findById(6L).orElseThrow(); // COSIS
        unidade = unidadeRepo.findById(8L).orElseThrow(); // SEDESENV

        // Criar Processo e Mapa
        // Dados de Teste
        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDescricao("Processo de Teste");
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);
        mapa = mapaRepo.save(new Mapa());

        // Criar Subprocesso
        subprocesso = new Subprocesso();
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setMapa(mapa);
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA); // Estado inicial válido
        subprocesso = subprocessoRepo.save(subprocesso);

        // Criar Atividade e Competência
        atividade = atividadeRepo.save(new Atividade(mapa, "Atividade de Teste"));
        competencia = competenciaRepo.save(new Competencia(mapa, "Competência de Teste"));
    }

    @Nested
    @DisplayName("Testes de Sucesso")
    class Sucesso {

        @Test
        @DisplayName("Deve disponibilizar mapa com sucesso quando todos os dados estão corretos")
        @WithMockAdmin
        void disponibilizarMapa_comDadosValidos_retornaOk() throws Exception {
            // Arrange: Associar atividade e competência
            var id = new CompetenciaAtividade.Id(atividade.getCodigo(), competencia.getCodigo());
            competenciaAtividadeRepo.save(new CompetenciaAtividade(id, competencia, atividade));

            // Arrange: Adicionar uma análise de validação antiga para testar a limpeza
            Analise analiseAntiga = new Analise();
            analiseAntiga.setSubprocesso(subprocesso);
            analiseAntiga.setObservacoes("Análise antiga que deve ser removida.");
            analiseRepo.save(analiseAntiga);

            LocalDateTime dataLimite = LocalDateTime.now().plusDays(10);
            String observacoes = "Observações de teste para o mapa.";
            DisponibilizarMapaReq request = new DisponibilizarMapaReq(observacoes, dataLimite);

            // Act & Assert
            mockMvc.perform(post(API_URL, subprocesso.getCodigo())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Mapa de competências disponibilizado com sucesso."));

            // Verificar o estado final no banco de dados
            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow(() -> new AssertionError("Subprocesso não encontrado após atualização."));
            assertThat(spAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
            assertThat(spAtualizado.getDataLimiteEtapa2()).isEqualTo(dataLimite);

            Mapa mapaAtualizado = mapaRepo.findById(mapa.getCodigo()).orElseThrow(() -> new AssertionError("Mapa não encontrado após atualização."));
            assertThat(mapaAtualizado.getSugestoes()).isEqualTo(observacoes);

            // Verificar Movimentação
            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            Movimentacao mov = movimentacoes.getFirst();
            assertThat(mov.getUnidadeOrigem().getSigla()).isEqualTo(SEDOC_LITERAL);
            assertThat(mov.getUnidadeDestino().getSigla()).isEqualTo(unidade.getSigla());
            assertThat(mov.getDescricao()).isEqualTo("Disponibilização do mapa de competências para validação");

            // Verificar Alerta
            Optional<Alerta> alertaOpt = alertaRepo.findAll().stream().findFirst();
            assertThat(alertaOpt).isPresent();
            Alerta alerta = alertaOpt.get();
            assertThat(alerta.getDescricao()).isEqualTo("Mapa de competências da unidade " + unidade.getSigla() + " disponibilizado para análise");
            assertThat(alerta.getUnidadeOrigem().getSigla()).isEqualTo(SEDOC_LITERAL);
            assertThat(alerta.getUnidadeDestino().getSigla()).isEqualTo(unidade.getSigla());

            // Verificar Limpeza do Histórico
            List<Analise> analisesRestantes = analiseRepo.findBySubprocessoCodigo(subprocesso.getCodigo());
            assertThat(analisesRestantes).isEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Falha")
    class Falha {
        @Test
        @DisplayName("Não deve disponibilizar mapa com usuário sem permissão (não ADMIN)")
        @WithMockGestor
        void disponibilizarMapa_semPermissao_retornaForbidden() throws Exception {
            // Arrange: Associar atividade e competência para passar na validação de negócio
            var id = new CompetenciaAtividade.Id(atividade.getCodigo(), competencia.getCodigo());
            competenciaAtividadeRepo.save(new CompetenciaAtividade(id, competencia, atividade));

            DisponibilizarMapaReq request = new DisponibilizarMapaReq(OBS_LITERAL, LocalDateTime.now().plusDays(10));

            mockMvc.perform(post(API_URL, subprocesso.getCodigo())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Não deve disponibilizar mapa se subprocesso não está no estado correto")
        @WithMockAdmin
        void disponibilizarMapa_comEstadoInvalido_retornaConflict() throws Exception {
            subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
            subprocessoRepo.save(subprocesso);

            DisponibilizarMapaReq request = new DisponibilizarMapaReq(OBS_LITERAL, LocalDateTime.now().plusDays(10));

            mockMvc.perform(post(API_URL, subprocesso.getCodigo())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Não deve disponibilizar mapa se houver atividade sem competência associada")
        @WithMockAdmin
        void disponibilizarMapa_comAtividadeNaoAssociada_retornaBadRequest() throws Exception {
            // Arrange: Associar a competência do setup a uma atividade dummy para isolar a falha.
            Atividade dummyActivity = atividadeRepo.save(new Atividade(mapa, "Dummy Activity"));
            var id = new CompetenciaAtividade.Id(dummyActivity.getCodigo(), competencia.getCodigo());
            competenciaAtividadeRepo.save(new CompetenciaAtividade(id, competencia, dummyActivity));
            // A 'atividade' principal (criada no setUp) permanece não associada.

            DisponibilizarMapaReq request = new DisponibilizarMapaReq(OBS_LITERAL, LocalDateTime.now().plusDays(10));

            String responseBody = mockMvc.perform(post(API_URL, subprocesso.getCodigo())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andReturn().getResponse().getContentAsString();

            // Assert: Desserializar a resposta e verificar os detalhes do erro
            ErroApi erroApi = objectMapper.readValue(responseBody, ErroApi.class);
            assertThat(erroApi.getDetails()).isNotNull();

            @SuppressWarnings("unchecked")
            List<String> atividades = (List<String>) erroApi.getDetails().get("atividadesNaoAssociadas");
            assertThat(atividades).isNotNull().contains(atividade.getDescricao());
        }

        @Test
        @DisplayName("Não deve disponibilizar mapa se houver competência sem atividade associada")
        @WithMockAdmin
        void disponibilizarMapa_comCompetenciaNaoAssociada_retornaBadRequest() throws Exception {
            // Arrange: Associar a atividade, mas deixar uma competência solta
            var id = new CompetenciaAtividade.Id(atividade.getCodigo(), competencia.getCodigo());
            competenciaAtividadeRepo.save(new CompetenciaAtividade(id, competencia, atividade));
            competenciaRepo.save(new Competencia(mapa, "Competência Solta"));

            DisponibilizarMapaReq request = new DisponibilizarMapaReq(OBS_LITERAL, LocalDateTime.now().plusDays(10));

            mockMvc.perform(post(API_URL, subprocesso.getCodigo())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.details.competenciasNaoAssociadas").exists());
        }
    }
}