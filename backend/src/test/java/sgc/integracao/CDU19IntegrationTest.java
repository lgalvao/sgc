package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.*;
import sgc.comum.Mensagens;
import sgc.fixture.MapaFixture;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.integracao.mocks.WithMockChefe;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@DisplayName("CDU-19: Validar mapa de competências")
class CDU19IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    private Unidade unidade;
    private Unidade unidadeSuperior;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        // Use existing units from data.sql to match @WithMockChefe user
        // Unit 6 (COSIS - INTERMEDIARIA) is the parent
        // Unit 9 (SEDIA - OPERACIONAL) is subordinate to 6
        // User '333333333333' has CHEFE profile for unit 9
        unidadeSuperior = unidadeRepo.findById(6L)
                .orElseThrow(() -> new RuntimeException("Unit 6 not found in data.sql"));

        unidade = unidadeRepo.findById(9L)
                .orElseThrow(() -> new RuntimeException("Unit 9 not found in data.sql"));

        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setDescricao("Processo para CDU-19");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);

        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        subprocesso = subprocessoRepo.save(subprocesso);
        registrarMovimentacaoInicial(subprocesso);

        Mapa mapa = MapaFixture.mapaPadrao(subprocesso);
        mapa.setCodigo(null);
        mapa = mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);
        subprocesso = subprocessoRepo.save(subprocesso);
    }

    @Nested
    @DisplayName("Testes para o fluxo de 'Apresentar sugestões'")
    class ApresentarSugestoesTest {
        @Test
        @DisplayName(
                "Deve apresentar sugestões, alterar status, mas não criar movimentação ou alerta")
        @WithMockChefe("333333333333")
            // CHEFE of unit 9
        void testApresentarSugestoes_Sucesso() throws Exception {
            String sugestoes = "Minha sugestão de teste";

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{codigo}/apresentar-sugestoes",
                                    subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType("application/json")
                                    .content("{\"texto\": \"" + sugestoes + "\"}"))
                    .andExpect(status().isOk());

            Subprocesso subprocessoAtualizado =
                    subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);
            assertThat(subprocessoAtualizado.getMapa().getSugestoes()).isEqualTo(sugestoes);

            List<Movimentacao> movimentacoes =
                    movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(
                            subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(2);
            assertThat(movimentacoes.getFirst().getDescricao())
                    .isEqualTo(Mensagens.HIST_MAPA_SUGESTOES_APRESENTADAS);

            assertThat(movimentacoes.getFirst().getUnidadeOrigem().getSigla())
                    .isEqualTo(unidade.getSigla());

            assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla())
                    .isEqualTo(unidadeSuperior.getSigla());
            List<Alerta> alertas =
                    alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo());

            assertThat(alertas).hasSize(1);
            assertThat(alertas.getFirst().getDescricao())
                    .isEqualTo(Mensagens.ALERTA_MAPA_SUGESTOES.formatted(unidade.getSigla()));
            assertThat(alertas.getFirst().getUnidadeDestino().getSigla())
                    .isEqualTo(unidadeSuperior.getSigla());

            List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll().stream()
                    .filter(n -> n.getTipoNotificacao() == TipoNotificacao.MAPA_SUGESTOES_APRESENTADAS)
                    .filter(n -> n.getUsuarioDestinoTitulo() == null)
                    .toList();
            assertThat(notificacoes).hasSize(1);

            NotificacaoEmail notificacao = notificacoes.getFirst();
            assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo(unidadeSuperior.getSigla());
            assertThat(notificacao.getDestinatario())
                    .isEqualTo("%s@tre-pe.jus.br".formatted(unidadeSuperior.getSigla().toLowerCase(Locale.ROOT)));
            assertThat(notificacao.getAssunto())
                    .isEqualTo("SGC: Sugestões apresentadas para o mapa de competências da %s".formatted(unidade.getSigla()));
            assertThat(notificacao.getCorpoHtml())
                    .contains("Prezado(a) responsável pela <strong>%s</strong>".formatted(unidadeSuperior.getSigla()))
                    .contains("A unidade <strong>%s</strong> apresentou sugestões para o mapa de".formatted(unidade.getSigla()))
                    .contains("competências elaborado no processo")
                    .contains("Processo para CDU-19")
                    .contains("A análise dessas sugestões já pode ser realizada no Sistema de Gestão de Competências");
            assertThat(notificacao.getSituacao()).isIn(SituacaoNotificacao.PENDENTE, SituacaoNotificacao.ENVIADO);
        }
    }

    @Nested
    @DisplayName("Testes para o fluxo de 'Validar mapa'")
    class ValidarMapaTest {
        @Test
        @DisplayName("Deve validar o mapa, alterar status, registrar movimentação e criar alerta")
        @WithMockChefe("333333333333")
            // CHEFE of unit 9
        void testValidarMapa_Sucesso() throws Exception {
            mockMvc.perform(
                            post("/api/subprocessos/{codigo}/validar-mapa", subprocesso.getCodigo())
                                    .with(csrf()))
                    .andExpect(status().isOk());

            Subprocesso subprocessoAtualizado =
                    subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);

            List<Movimentacao> movimentacoes =
                    movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(
                            subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(2);
            assertThat(movimentacoes.getFirst().getDescricao())
                    .isEqualTo(Mensagens.HIST_MAPA_VALIDADO);
            assertThat(movimentacoes.getFirst().getUnidadeOrigem().getSigla())
                    .isEqualTo(unidade.getSigla());
            assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla())
                    .isEqualTo(unidadeSuperior.getSigla());

            List<Alerta> alertas =
                    alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo());
            assertThat(alertas).hasSize(1);
            assertThat(alertas.getFirst().getDescricao())
                    .isEqualTo(Mensagens.ALERTA_MAPA_VALIDACAO_PENDENTE.formatted(unidade.getSigla()));
            assertThat(alertas.getFirst().getUnidadeDestino().getSigla())
                    .isEqualTo(unidadeSuperior.getSigla());

            List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll().stream()
                    .filter(n -> n.getTipoNotificacao() == TipoNotificacao.MAPA_VALIDADO)
                    .filter(n -> n.getUsuarioDestinoTitulo() == null)
                    .toList();
            assertThat(notificacoes).hasSize(1);

            NotificacaoEmail notificacao = notificacoes.getFirst();
            assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo(unidadeSuperior.getSigla());
            assertThat(notificacao.getDestinatario())
                    .isEqualTo("%s@tre-pe.jus.br".formatted(unidadeSuperior.getSigla().toLowerCase(Locale.ROOT)));
            assertThat(notificacao.getAssunto())
                    .isEqualTo("SGC: Validação do mapa de competências da %s submetida para análise".formatted(unidade.getSigla()));
            assertThat(notificacao.getCorpoHtml())
                    .contains("Prezado(a) responsável pela <strong>%s</strong>".formatted(unidadeSuperior.getSigla()))
                    .contains("A unidade <strong>%s</strong> validou o mapa de competências".formatted(unidade.getSigla()))
                    .contains("Processo para CDU-19")
                    .contains("A análise dessa validação já pode ser realizada no Sistema de Gestão de Competências");
            assertThat(notificacao.getSituacao()).isIn(SituacaoNotificacao.PENDENTE, SituacaoNotificacao.ENVIADO);
        }
    }
}
