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
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.dto.DisponibilizarMapaReq;
import sgc.subprocesso.model.*;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
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
        movimentacaoRepo.deleteAll();
        alertaRepo.deleteAll();
        atividadeRepo.deleteAll();
        competenciaRepo.deleteAll();
        unidadeMapaRepo.deleteAll();
        subprocessoRepo.deleteAll();
        mapaRepo.deleteAll();
        processoRepo.deleteAll();

        Unidade sedoc = unidadeRepo.findById(15L).orElseThrow();
        Unidade unidadeSuperior = unidadeRepo.findById(6L).orElseThrow();
        unidade = unidadeRepo.findById(8L).orElseThrow();

        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDescricao("Processo de Teste");
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);
        mapa = mapaRepo.save(new Mapa());

        subprocesso = new Subprocesso();
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setMapa(mapa);
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        subprocesso = subprocessoRepo.save(subprocesso);

        atividade = atividadeRepo.save(new Atividade(mapa, "Atividade de Teste"));
        competencia = competenciaRepo.save(new Competencia("Competência de Teste", mapa));
    }

    @Nested
    @DisplayName("Testes de Sucesso")
    class Sucesso {

        @Test
        @DisplayName("Deve disponibilizar mapa quando todos os dados estão corretos")
        @WithMockAdmin
        void disponibilizarMapa_comDadosValidos_retornaOk() throws Exception {
            competencia.setAtividades(new HashSet<>(Set.of(atividade)));
            competenciaRepo.save(competencia);

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

            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow(() -> new AssertionError("Subprocesso não encontrado após atualização."));
            assertThat(spAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
            assertThat(spAtualizado.getDataLimiteEtapa2()).isEqualTo(dataLimite.atStartOfDay());

            Mapa mapaAtualizado = mapaRepo.findById(mapa.getCodigo()).orElseThrow(() -> new AssertionError("Mapa não encontrado após atualização."));
            assertThat(mapaAtualizado.getSugestoes()).isEqualTo(observacoes);

            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            Movimentacao mov = movimentacoes.getFirst();
            assertThat(mov.getUnidadeOrigem().getSigla()).isEqualTo(SEDOC_LITERAL);
            assertThat(mov.getUnidadeDestino().getSigla()).isEqualTo(unidade.getSigla());
            assertThat(mov.getDescricao()).isEqualTo("Disponibilização do mapa de competências para validação");

            Optional<Alerta> alertaOpt = alertaRepo.findAll().stream().findFirst();
            assertThat(alertaOpt).isPresent();
            Alerta alerta = alertaOpt.get();
            assertThat(alerta.getDescricao()).isEqualTo("Mapa de competências da unidade " + unidade.getSigla() + " disponibilizado para análise");
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
            competencia.setAtividades(new HashSet<>(Set.of(atividade)));
            competenciaRepo.save(competencia);

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
            // Apenas a 'competencia' está associada a uma atividade, a 'atividade' principal do teste não está.
            Atividade dummyActivity = atividadeRepo.save(new Atividade(mapa, "Dummy Activity"));
            dummyActivity.setCompetencias(new HashSet<>(Set.of(competencia)));
            atividadeRepo.save(dummyActivity);

            DisponibilizarMapaReq request = new DisponibilizarMapaReq(LocalDate.now().plusDays(10), OBS_LITERAL);

            String responseBody = mockMvc.perform(post(API_URL, subprocesso.getCodigo())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andReturn().getResponse().getContentAsString();

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
            competencia.setAtividades(new HashSet<>(Set.of(atividade)));
            competenciaRepo.save(competencia);
            competenciaRepo.save(new Competencia("Competência Solta", mapa));

            DisponibilizarMapaReq request = new DisponibilizarMapaReq(LocalDate.now().plusDays(10), OBS_LITERAL);

            mockMvc.perform(post(API_URL, subprocesso.getCodigo())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.details.competenciasNaoAssociadas").exists());
        }
    }
}
