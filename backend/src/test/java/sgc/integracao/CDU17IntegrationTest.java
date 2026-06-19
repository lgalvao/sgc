package sgc.integracao;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.*;
import sgc.comum.Mensagens;
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
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UsuarioRepo;
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
@DisplayName("CDU-17: Disponibilizar mapa de Competências")
class CDU17IntegrationTest extends BaseIntegrationTest {
    private static final String API_URL = "/api/subprocessos/{codigo}/disponibilizar-mapa";
    private static final String OBS_LITERAL = "Obs";
    private static final String ADMIN_LITERAL = "ADMIN";
    private static final String SQL_ATUALIZAR_UNIDADE_SUPERIOR = """
            UPDATE SGC.VW_UNIDADE
            SET unidade_superior_codigo = ?
            WHERE codigo = ?
            """;

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
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    private Unidade unidade;
    private Unidade unidadeSuperior;
    private Subprocesso subprocesso;
    private Mapa mapa;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("MERGE INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) KEY(usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)",
                "111111111111", 1, "ADMIN");

        // Criar unidade via Fixture
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setNome("Unidade CDU-17");
        unidade.setSigla("U17");
        unidade = unidadeRepo.save(unidade);

        unidadeSuperior = UnidadeFixture.unidadePadrao();
        unidadeSuperior.setCodigo(null);
        unidadeSuperior.setNome("Unidade superior CDU-17");
        unidadeSuperior.setSigla("U17SUP");
        unidadeSuperior.setTipo(TipoUnidade.INTERMEDIARIA);
        unidadeSuperior = unidadeRepo.save(unidadeSuperior);

        // Add responsabilidade to prevent 404 during email notification
        jdbcTemplate.update("INSERT INTO SGC.VW_RESPONSABILIDADE (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio) VALUES (?, ?, ?, ?, ?)",
                unidade.getCodigo(), "111111111111", "00000", "TITULAR", LocalDateTime.now());
        jdbcTemplate.update("INSERT INTO SGC.VW_RESPONSABILIDADE (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio) VALUES (?, ?, ?, ?, ?)",
                unidadeSuperior.getCodigo(), "222222222222", "00001", "TITULAR", LocalDateTime.now());
        jdbcTemplate.update(SQL_ATUALIZAR_UNIDADE_SUPERIOR, unidadeSuperior.getCodigo(), unidade.getCodigo());

        unidade = unidadeRepo.findById(unidade.getCodigo()).orElseThrow();
        unidadeSuperior = unidadeRepo.findById(unidadeSuperior.getCodigo()).orElseThrow();

        // Criar processo via Fixture
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.REVISAO);
        processo = processoRepo.save(processo);

        // Criar subprocesso via Fixture
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        subprocesso.setDataLimiteEtapa2(null);
        subprocesso.setDataFimEtapa2(null);
        subprocesso = subprocessoRepo.save(subprocesso);

        mapa = MapaFixture.mapaPadrao(subprocesso);
        mapa.setCodigo(null);
        mapa = mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);
        subprocesso = subprocessoRepo.save(subprocesso);

        Unidade adminUnit = unidadeRepo.findById(1L).orElseThrow();
        Movimentacao movAdmin = Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidade)
                .unidadeDestino(adminUnit)
                .usuario(usuarioRepo.findById("111111111111").orElseThrow())
                .descricao("Enviado para Admin para Ajuste")
                .dataHora(LocalDateTime.now())
                .build();
        movimentacaoRepo.save(movAdmin);

        Atividade atividade = Atividade.builder().mapa(mapa).descricao("Atividade valida").build();
        atividade = atividadeRepo.save(atividade);

        Competencia competencia = Competencia.builder().descricao("Competencia valida").mapa(mapa).build();
        competencia = competenciaRepo.save(competencia);

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
            analiseAntiga.setTipo(TipoAnalise.VALIDACAO);
            analiseAntiga.setAcao(TipoAcaoAnalise.ACEITE_REVISAO);
            analiseAntiga.setUnidadeCodigo(unidade.getCodigo());
            analiseAntiga.setUsuarioTitulo("111111111111");
            analiseAntiga.setDataHora(LocalDateTime.now().minusDays(1));
            analiseAntiga.setObservacoes("Análise antiga que deve ser removida.");
            analiseRepo.save(analiseAntiga);

            LocalDate dataLimite = subprocesso.getDataLimiteEtapa1().toLocalDate().plusDays(1);
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

            List<Movimentacao> movimentacoes = movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(
                    subprocesso.getCodigo());
            assertThat(movimentacoes).hasSizeGreaterThanOrEqualTo(1);

            Movimentacao mov = movimentacoes.getFirst();
            assertThat(mov.getUnidadeOrigem().getSigla()).isEqualTo(ADMIN_LITERAL);
            assertThat(mov.getUnidadeDestino().getSigla()).isEqualTo(unidade.getSigla());
            assertThat(mov.getDescricao()).isEqualTo(Mensagens.HIST_MAPA_DISPONIBILIZADO);

            List<Alerta> alertas = alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo());
            assertThat(alertas).hasSize(1);
            assertThat(alertas.getFirst().getDescricao())
                    .isEqualTo(Mensagens.ALERTA_MAPA_DISPONIBILIZADO.formatted(unidade.getSigla()));
            assertThat(alertas.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidade.getSigla());

            List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll().stream()
                    .filter(n -> n.getTipoNotificacao() == TipoNotificacao.MAPA_DISPONIBILIZADO)
                    .filter(n -> n.getUsuarioDestinoTitulo() == null)
                    .toList();
            assertThat(notificacoes).hasSizeGreaterThanOrEqualTo(2);

            NotificacaoEmail notificacaoUnidade = notificacoes.stream()
                    .filter(n -> unidade.getSigla().equals(n.getUnidadeDestinoSigla()))
                    .findFirst()
                    .orElseThrow();
            assertThat(notificacaoUnidade.getDestinatario()).isEqualTo("u17@tre-pe.jus.br");
            assertThat(notificacaoUnidade.getAssunto()).isEqualTo("SGC: Mapa de competências disponibilizado");
            assertThat(notificacaoUnidade.getCorpoHtml())
                    .contains("Prezado(a) responsável pela <strong>U17</strong>")
                    .contains("O mapa de competências de sua unidade foi disponibilizado no contexto do processo")
                    .contains(subprocesso.getProcesso().getDescricao())
                    .contains("A validação deste mapa já pode ser realizada no Sistema de Gestão de Competências");
            assertThat(notificacaoUnidade.getSituacao()).isIn(SituacaoNotificacao.PENDENTE, SituacaoNotificacao.ENVIADO);

            NotificacaoEmail notificacaoSuperior = notificacoes.stream()
                    .filter(n -> unidadeSuperior.getSigla().equals(n.getUnidadeDestinoSigla()))
                    .findFirst()
                    .orElseThrow();
            assertThat(notificacaoSuperior.getDestinatario()).isEqualTo("u17sup@tre-pe.jus.br");
            assertThat(notificacaoSuperior.getAssunto()).isEqualTo("SGC: Mapa de competências disponibilizado - U17");
            assertThat(notificacaoSuperior.getCorpoHtml())
                    .contains("Prezado(a) responsável pela <strong>U17SUP</strong>")
                    .contains("O mapa de competências da <strong>U17</strong> foi disponibilizado no")
                    .contains("A validação deste mapa já pode ser realizada no Sistema de Gestão de Competências");
            assertThat(notificacaoSuperior.getSituacao()).isIn(SituacaoNotificacao.PENDENTE, SituacaoNotificacao.ENVIADO);

            aguardarEmail(2);
            assertThat(algumEmailPara(notificacaoUnidade.getDestinatario())).isTrue();
            assertThat(algumEmailPara(notificacaoSuperior.getDestinatario())).isTrue();
            assertThat(algumEmailComAssunto("[SGC-TEST] Mapa de competências disponibilizado")).isTrue();
            assertThat(algumEmailComAssunto("[SGC-TEST] Mapa de competências disponibilizado - U17")).isTrue();
            assertThat(algumEmailContem("A validação deste mapa já pode ser realizada no Sistema de Gestão de Competências")).isTrue();

            List<Analise> analisesRestantes = analiseRepo.findBySubprocessoCodigo(subprocesso.getCodigo());
            assertThat(analisesRestantes).hasSize(1);
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
            Atividade atividadeSolta = Atividade.builder().mapa(mapa).descricao("Atividade solta").build();
            atividadeRepo.save(atividadeSolta);

            DisponibilizarMapaRequest request =
                    new DisponibilizarMapaRequest(LocalDate.now().plusDays(10), OBS_LITERAL);

            mockMvc.perform(
                            post(API_URL, subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.message").value(Matchers.containsString(Mensagens.ATIVIDADES_DEVEM_TER_COMPETENCIA)));
        }

        @Test
        @DisplayName("Não deve disponibilizar mapa se houver competência sem atividade associada")
        @WithMockAdmin
        void disponibilizarMapa_comCompetenciaNaoAssociada_retornaBadRequest() throws Exception {
            competenciaRepo.save(Competencia.builder().descricao("Competência solta").mapa(mapa).build());

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

        @Test
        @DisplayName("Não deve disponibilizar mapa com data limite menor ou igual à data de fim da etapa anterior")
        @WithMockAdmin
        void disponibilizarMapa_comDataLimiteMenorOuIgualDataFimEtapaAnterior_retornaUnprocessable() throws Exception {
            subprocesso.setDataLimiteEtapa1(LocalDate.now().plusDays(8).atStartOfDay());
            subprocesso.setDataFimEtapa1(LocalDate.now().plusDays(10).atStartOfDay());
            subprocessoRepo.save(subprocesso);

            DisponibilizarMapaRequest request = new DisponibilizarMapaRequest(LocalDate.now().plusDays(10), OBS_LITERAL);

            mockMvc.perform(post(API_URL, subprocesso.getCodigo())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.message").value(Mensagens.DATA_LIMITE_MAIOR_QUE_FIM_ETAPA_ANTERIOR));
        }
    }
}
