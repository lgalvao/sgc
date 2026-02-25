package sgc.integracao;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.fixture.MapaFixture;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockGestor;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@DisplayName("CDU-17: Disponibilizar Mapa de Competências")
class CDU17IntegrationTest extends BaseIntegrationTest {
    private static final String API_URL = "/api/subprocessos/{codigo}/disponibilizar-mapa";
    private static final String OBS_LITERAL = "Obs";
    private static final String ADMIN_LITERAL = "ADMIN";

    @Autowired
    private CompetenciaRepo competenciaRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private AnaliseRepo analiseRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Unidade unidade;
    private Subprocesso subprocesso;
    private Mapa mapa;

    @BeforeEach
    void setUp() {
        // Ensure Admin user has profile in ADMIN (ID 1)
        jdbcTemplate.update("MERGE INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) KEY(usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)",
                "111111111111", 1, "ADMIN");

        // Criar Unidade via Fixture
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setNome("Unidade CDU-17");
        unidade.setSigla("U17");
        unidade = unidadeRepo.save(unidade);

        // Criar Processo via Fixture
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.REVISAO);
        processo = processoRepo.save(processo);

        // Criar Mapa
        mapa = MapaFixture.mapaPadrao(null);
        mapa.setCodigo(null);
        mapa = mapaRepo.save(mapa);

        // Criar Subprocesso via Fixture
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setMapa(mapa);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        subprocesso.setDataLimiteEtapa2(null);
        subprocesso.setDataFimEtapa2(null);
        subprocesso = subprocessoRepo.save(subprocesso);

        Unidade adminUnit = unidadeRepo.findById(1L).orElseThrow();
        Movimentacao movAdmin = Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidade)
                .unidadeDestino(adminUnit)
                .descricao("Enviado para Admin para Ajuste")
                .dataHora(LocalDateTime.now())
                .build();
        movimentacaoRepo.save(movAdmin);

        Atividade atividade = Atividade.builder().mapa(mapa).descricao("Atividade Valida").build();
        atividade = atividadeRepo.save(atividade);

        Competencia competencia = Competencia.builder().descricao("Competencia Valida").mapa(mapa).build();
        competencia = competenciaRepo.save(competencia);

        // Associar (ManyToMany manually if needed or via helper methods)
        atividade.getCompetencias().add(competencia);
        atividadeRepo.save(atividade);

        competencia.getAtividades().add(atividade);
        competenciaRepo.save(competencia);
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
            DisponibilizarMapaRequest request = new DisponibilizarMapaRequest(dataLimite, observacoes);

            // Print error if 500
            mockMvc.perform(
                            post(API_URL, subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.mensagem").value("Mapa de competências disponibilizado."));

            Subprocesso spAtualizado = subprocessoRepo
                    .findById(subprocesso.getCodigo())
                    .orElseThrow();

            assertThat(spAtualizado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);

            assertThat(spAtualizado.getDataLimiteEtapa2()).isEqualTo(dataLimite.atStartOfDay());

            Mapa mapaAtualizado = mapaRepo.findById(mapa.getCodigo()).orElseThrow();
            assertThat(mapaAtualizado.getSugestoes()).isEqualTo(observacoes);

            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                    subprocesso.getCodigo());
            assertThat(movimentacoes).hasSizeGreaterThanOrEqualTo(1);

            Movimentacao mov = movimentacoes.getFirst();
            assertThat(mov.getUnidadeOrigem().getSigla()).isEqualTo(ADMIN_LITERAL);
            assertThat(mov.getUnidadeDestino().getSigla()).isEqualTo(unidade.getSigla());
            assertThat(mov.getDescricao()).isEqualTo("Disponibilização do mapa de competências para validação");

            List<Alerta> alertas = alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo());
            assertThat(alertas).hasSize(1);

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
            DisponibilizarMapaRequest request = new DisponibilizarMapaRequest(
                    LocalDate.now().plusDays(10), OBS_LITERAL
            );

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
            subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            subprocessoRepo.save(subprocesso);

            DisponibilizarMapaRequest request = new DisponibilizarMapaRequest(LocalDate.now().plusDays(10), OBS_LITERAL);

            mockMvc.perform(post(API_URL, subprocesso.getCodigo())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableContent());
        }

        @Test
        @DisplayName("Não deve disponibilizar mapa se houver atividade sem competência associada")
        @WithMockAdmin
        void disponibilizarMapa_comAtividadeNaoAssociada_retornaBadRequest() throws Exception {
            // Cria uma nova atividade sem competências para criar o cenário de erro
            Atividade atividadeSolta = Atividade.builder().mapa(mapa).descricao("Atividade Solta").build();
            atividadeRepo.save(atividadeSolta);

            DisponibilizarMapaRequest request =
                    new DisponibilizarMapaRequest(LocalDate.now().plusDays(10), OBS_LITERAL);

            mockMvc.perform(
                            post(API_URL, subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.message").value(Matchers.containsString("Todas as atividades devem estar associadas a pelo menos uma competência.")));
        }

        @Test
        @DisplayName("Não deve disponibilizar mapa se houver competência sem atividade associada")
        @WithMockAdmin
        void disponibilizarMapa_comCompetenciaNaoAssociada_retornaBadRequest() throws Exception {
            competenciaRepo.save(Competencia.builder().descricao("Competência Solta").mapa(mapa).build());

            DisponibilizarMapaRequest request = new DisponibilizarMapaRequest(
                    LocalDate.now().plusDays(10), OBS_LITERAL
            );

            mockMvc.perform(post(API_URL, subprocesso.getCodigo())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.message").value("Todas as competências devem estar associadas a pelo menos uma atividade."));
        }
    }
}
