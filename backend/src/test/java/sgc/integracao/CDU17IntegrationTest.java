package sgc.integracao;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
import sgc.comum.erros.ErroApi;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockGestor;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.subprocesso.dto.DisponibilizarMapaReq;
import sgc.subprocesso.model.*;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-17: Disponibilizar Mapa de Competências")
@Import({ TestSecurityConfig.class, sgc.integracao.mocks.TestThymeleafConfig.class })
class CDU17IntegrationTest extends BaseIntegrationTest {
    private static final String API_URL = "/api/subprocessos/{codigo}/disponibilizar-mapa";
    private static final String OBS_LITERAL = "Obs";
    private static final String SEDOC_LITERAL = "SEDOC";


    @Autowired
    private ObjectMapper objectMapper;

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
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private AnaliseRepo analiseRepo;

    private Unidade unidade;
    private Subprocesso subprocesso;
    private Mapa mapa;
    private Atividade atividade;
    private Competencia competencia;

    @BeforeEach
    void setUp() {
        // Use dados pré-carregados do data.sql (CDU-17 test data)
        unidade = unidadeRepo.findById(8L).orElseThrow(); // SEDESENV
        subprocesso = subprocessoRepo.findById(1700L).orElseThrow();
        mapa = mapaRepo.findById(1700L).orElseThrow();
        atividade = atividadeRepo.findById(17001L).orElseThrow();
        competencia = competenciaRepo.findById(17001L).orElseThrow();

        // Limpa movimentações, alertas e análises relacionadas ao subprocesso de teste
        movimentacaoRepo.deleteAll(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1700L));
        alertaRepo.deleteAll(alertaRepo.findByProcessoCodigo(1700L));
        analiseRepo.deleteAll(analiseRepo.findBySubprocessoCodigo(1700L));

        // Garante que o subprocesso está no estado correto
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        subprocesso.setDataLimiteEtapa2(null);
        subprocesso.setDataFimEtapa2(null);
        subprocessoRepo.save(subprocesso);

        // Garante que o mapa não tem observações
        mapa.setSugestoes(null);
        mapaRepo.save(mapa);
    }

    @Nested
    @DisplayName("Testes de Sucesso")
    class Sucesso {

        @Test
        @DisplayName("Deve disponibilizar mapa quando todos os dados estão corretos")
        @WithMockAdmin
        void disponibilizarMapa_comDadosValidos_retornaOk() throws Exception {
            Analise analiseAntiga = new Analise();
            analiseAntiga.setSubprocesso(subprocesso);
            analiseAntiga.setObservacoes("Análise antiga que deve ser removida.");
            analiseRepo.save(analiseAntiga);

            LocalDate dataLimite = LocalDate.now().plusDays(10);
            String observacoes = "Observações de teste para o mapa.";
            DisponibilizarMapaReq request = new DisponibilizarMapaReq(dataLimite, observacoes);

            mockMvc.perform(post(API_URL, subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Mapa de competências disponibilizado."));

            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo())
                    .orElseThrow(() -> new AssertionError("Subprocesso não encontrado após atualização."));
            assertThat(spAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
            assertThat(spAtualizado.getDataLimiteEtapa2()).isEqualTo(dataLimite.atStartOfDay());

            Mapa mapaAtualizado = mapaRepo.findById(mapa.getCodigo())
                    .orElseThrow(() -> new AssertionError("Mapa não encontrado após atualização."));
            assertThat(mapaAtualizado.getSugestoes()).isEqualTo(observacoes);

            List<Movimentacao> movimentacoes = movimentacaoRepo
                    .findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            Movimentacao mov = movimentacoes.getFirst();
            assertThat(mov.getUnidadeOrigem().getSigla()).isEqualTo(SEDOC_LITERAL);
            assertThat(mov.getUnidadeDestino().getSigla()).isEqualTo(unidade.getSigla());
            assertThat(mov.getDescricao()).isEqualTo("Disponibilização do mapa de competências para validação");

            List<Alerta> alertas = alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo());
            assertThat(alertas).hasSize(1);
            Alerta alerta = alertas.getFirst();
            assertThat(alerta.getDescricao()).isEqualTo(
                    "Mapa de competências da unidade " + unidade.getSigla() + " disponibilizado para análise");
            assertThat(alerta.getUnidadeOrigem().getSigla()).isEqualTo(SEDOC_LITERAL);
            assertThat(alerta.getUnidadeDestino().getSigla()).isEqualTo(unidade.getSigla());

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
            DisponibilizarMapaReq request = new DisponibilizarMapaReq(LocalDate.now().plusDays(10), OBS_LITERAL);

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

            DisponibilizarMapaReq request = new DisponibilizarMapaReq(LocalDate.now().plusDays(10), OBS_LITERAL);

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
            // Cria uma nova atividade sem competências para criar o cenário de erro
            Atividade atividadeSolta = new Atividade(mapa, "Atividade Solta");
            atividadeRepo.save(atividadeSolta);

            DisponibilizarMapaReq request = new DisponibilizarMapaReq(LocalDate.now().plusDays(10), OBS_LITERAL);

            String responseBody = mockMvc.perform(post(API_URL, subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableContent())
                    .andReturn().getResponse().getContentAsString();

            ErroApi erroApi = objectMapper.readValue(responseBody, ErroApi.class);
            assertThat(erroApi.getDetails()).isNotNull();

            @SuppressWarnings("unchecked")
            List<String> atividades = (List<String>) erroApi.getDetails().get("atividadesNaoAssociadas");
            assertThat(atividades).isNotNull().contains("Atividade Solta");
        }

        @Test
        @DisplayName("Não deve disponibilizar mapa se houver competência sem atividade associada")
        @WithMockAdmin
        void disponibilizarMapa_comCompetenciaNaoAssociada_retornaBadRequest() throws Exception {
            Competencia competenciaSolta = competenciaRepo.save(new Competencia("Competência Solta", mapa));

            DisponibilizarMapaReq request = new DisponibilizarMapaReq(LocalDate.now().plusDays(10), OBS_LITERAL);

            mockMvc.perform(post(API_URL, subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.details.competenciasNaoAssociadas").exists());
        }
    }
}
