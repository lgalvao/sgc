package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.AlertaRepo;
import sgc.analise.model.Analise;
import sgc.analise.model.AnaliseRepo;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockChefe;
import sgc.integracao.mocks.WithMockChefeSecurityContextFactory;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.*;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@WithMockChefe("333333333333")
@Import({
    TestSecurityConfig.class,
    WithMockChefeSecurityContextFactory.class,
    sgc.integracao.mocks.TestThymeleafConfig.class
})
@Transactional
@DisplayName("CDU-10: Disponibilizar Revisão do Cadastro de Atividades e Conhecimentos")
class CDU10IntegrationTest extends BaseIntegrationTest {

    @Autowired private ProcessoRepo processoRepo;
    @Autowired private UnidadeRepo unidadeRepo;
    @Autowired private SubprocessoRepo subprocessoRepo;
    @Autowired private MapaRepo mapaRepo;
    @Autowired private AtividadeRepo atividadeRepo;
    @Autowired private ConhecimentoRepo conhecimentoRepo;
    @Autowired private CompetenciaRepo competenciaRepo;
    @Autowired private MovimentacaoRepo movimentacaoRepo;
    @Autowired private AlertaRepo alertaRepo;
    @Autowired private AnaliseRepo analiseRepo;

    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean
    private sgc.subprocesso.service.SubprocessoNotificacaoService subprocessoNotificacaoService;

    private Unidade unidadeChefe;
    private Unidade unidadeSuperior;
    private Subprocesso subprocessoRevisao;

    @BeforeEach
    void setUp() {
        unidadeSuperior = unidadeRepo.findById(6L).orElseThrow();
        unidadeChefe = unidadeRepo.findById(10L).orElseThrow();

        Processo processoRevisao =
                new Processo(
                        "Processo de Revisão",
                        TipoProcesso.REVISAO,
                        SituacaoProcesso.EM_ANDAMENTO,
                        LocalDateTime.now().plusDays(30));
        processoRepo.save(processoRevisao);

        var mapa = mapaRepo.save(new sgc.mapa.model.Mapa());
        subprocessoRevisao =
                new Subprocesso(
                        processoRevisao,
                        unidadeChefe,
                        mapa,
                        SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
                        processoRevisao.getDataLimite());
        subprocessoRepo.save(subprocessoRevisao);
    }

    @Nested
    @DisplayName("Testes para Disponibilizar Revisão do Cadastro")
    class DisponibilizarRevisaoCadastro {
        @Test
        @DisplayName(
                "Deve disponibilizar a revisão do cadastro quando todas as condições são atendidas")
        void deveDisponibilizarRevisaoComSucesso() throws Exception {
            var competencia =
                    competenciaRepo.save(
                            new Competencia("Competência de Teste", subprocessoRevisao.getMapa()));
            var atividade = new Atividade(subprocessoRevisao.getMapa(), "Atividade de Teste");
            atividade.getCompetencias().add(competencia);
            atividadeRepo.save(atividade);
            competencia.getAtividades().add(atividade);
            competenciaRepo.save(competencia);
            conhecimentoRepo.save(new Conhecimento("Conhecimento de Teste", atividade));

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/disponibilizar-revisao",
                                    subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.message",
                                    is("Revisão do cadastro de atividades disponibilizada")));

            Subprocesso subprocessoAtualizado =
                    subprocessoRepo.findById(subprocessoRevisao.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
            assertThat(subprocessoAtualizado.getDataFimEtapa1()).isNotNull();

            List<Movimentacao> movimentacoes =
                    movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                            subprocessoAtualizado.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            Movimentacao movimentacao = movimentacoes.getFirst();
            assertThat(movimentacao.getDescricao())
                    .isEqualTo("Disponibilização da revisão do cadastro de atividades");
            assertThat(movimentacao.getUnidadeOrigem().getCodigo())
                    .isEqualTo(unidadeChefe.getCodigo());
            assertThat(movimentacao.getUnidadeDestino().getCodigo())
                    .isEqualTo(unidadeSuperior.getCodigo());

            var alertas =
                    alertaRepo.findByProcessoCodigo(subprocessoRevisao.getProcesso().getCodigo());
            assertThat(alertas).hasSize(1);
            var alerta = alertas.getFirst();
            assertThat(alerta.getDescricao())
                    .isEqualTo(
                            "Cadastro de atividades e conhecimentos da unidade SESEL"
                                    + " disponibilizado para análise");
            assertThat(alerta.getUnidadeDestino()).isEqualTo(unidadeSuperior);

            verify(subprocessoNotificacaoService)
                    .notificarDisponibilizacaoRevisaoCadastro(
                            org.mockito.ArgumentMatchers.any(Subprocesso.class),
                            org.mockito.ArgumentMatchers.any(Unidade.class));
        }

        @Test
        @DisplayName("Não deve disponibilizar se houver atividade sem conhecimento associado")
        void naoDeveDisponibilizarComAtividadeSemConhecimento() throws Exception {
            Atividade atividade = new Atividade(subprocessoRevisao.getMapa(), "Atividade Vazia");
            atividadeRepo.save(atividade);

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/disponibilizar-revisao",
                                    subprocessoRevisao.getCodigo()))
                    .andExpect(status().isUnprocessableContent());

            Subprocesso subprocessoNaoAlterado =
                    subprocessoRepo.findById(subprocessoRevisao.getCodigo()).orElseThrow();
            assertThat(subprocessoNaoAlterado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        }
    }

    @Nested
    @DisplayName("Testes de Segurança")
    class Seguranca {
        @Test
        @WithMockChefe("999999999999")
        @DisplayName("Não deve permitir que um CHEFE de outra unidade disponibilize a revisão")
        void naoDevePermitirChefeDeOutraUnidadeDisponibilizar() throws Exception {
            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/disponibilizar-revisao",
                                    subprocessoRevisao.getCodigo()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Cenário 4: Histórico de análise deve ser excluído após nova disponibilização")
    class HistoricoAnaliseExcluido {

        @Test
        @DisplayName(
                "Deve excluir histórico de análises anteriores quando disponibilizar revisão"
                        + " novamente - Fluxo completo")
        void deveExcluirHistoricoAposNovaDisponibilizacao() throws Exception {
            // Preparar: criar atividade com conhecimento para permitir disponibilização
            var competencia =
                    competenciaRepo.save(
                            new Competencia("Competência de Teste", subprocessoRevisao.getMapa()));
            var atividade = new Atividade(subprocessoRevisao.getMapa(), "Atividade de Teste");
            atividade.getCompetencias().add(competencia);
            atividadeRepo.save(atividade);
            competencia.getAtividades().add(atividade);
            competenciaRepo.save(competencia);
            conhecimentoRepo.save(new Conhecimento("Conhecimento de Teste", atividade));

            Long subprocessoId = subprocessoRevisao.getCodigo();

            // 1. Primeira disponibilização (como CHEFE)
            mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId))
                    .andExpect(status().isOk());

            // Verificar situação
            Subprocesso sp = subprocessoRepo.findById(subprocessoId).orElseThrow();
            assertThat(sp.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

            // Verificar que não há análises ainda
            List<Analise> analisesAposDisp1 =
                    analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoId);
            assertThat(analisesAposDisp1).isEmpty();

            // 2. Criar análise de "Aceite" manualmente (simulando GESTOR/ADMIN aceitou)
            Analise analiseAceite = new Analise();
            analiseAceite.setSubprocesso(sp);
            analiseAceite.setUnidadeSigla(unidadeSuperior.getSigla());
            analiseAceite.setAcao(TipoAcaoAnalise.ACEITE_REVISAO);
            analiseAceite.setDataHora(LocalDateTime.now());
            analiseRepo.saveAndFlush(analiseAceite);

            // 3. Criar análise de "Devolução" manualmente (simulando segunda devolução)
            Analise analiseDevolucao = new Analise();
            analiseDevolucao.setSubprocesso(sp);
            analiseDevolucao.setUnidadeSigla(unidadeSuperior.getSigla());
            analiseDevolucao.setAcao(TipoAcaoAnalise.DEVOLUCAO_REVISAO);
            analiseDevolucao.setObservacoes("Segunda devolução");
            analiseDevolucao.setDataHora(LocalDateTime.now());
            analiseRepo.saveAndFlush(analiseDevolucao);

            // Verificar que temos 2 análises
            List<Analise> analisesAntes =
                    analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoId);
            assertThat(analisesAntes).hasSize(2);

            // 4. Simular devolução: mudar situação para em andamento (como faria o endpoint de
            // devolução)
            sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            sp.setDataFimEtapa1(null);
            subprocessoRepo.saveAndFlush(sp);

            // 5. Nova disponibilização (como CHEFE) - deve excluir histórico anterior (CDU-10 passo
            // 15)
            mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId))
                    .andExpect(status().isOk());

            // 6. Verificar que histórico foi excluído
            List<Analise> analisesDepois =
                    analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoId);
            assertThat(analisesDepois)
                    .as(
                            "Histórico de análises deve estar vazio após nova disponibilização"
                                    + " (CDU-10 passo 15)")
                    .isEmpty();
        }
    }
}
