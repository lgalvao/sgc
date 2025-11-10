package sgc.integracao;

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
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.alerta.model.AlertaRepo;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockChefe;
import sgc.integracao.mocks.WithMockChefeSecurityContextFactory;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.model.*;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockChefe
@Import({TestSecurityConfig.class, WithMockChefeSecurityContextFactory.class, TestThymeleafConfig.class})
@Transactional
@DisplayName("CDU-09: Disponibilizar Cadastro de Atividades e Conhecimentos")
class CDU09IntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProcessoRepo processoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private MapaRepo mapaRepo;
    @Autowired
    private AtividadeRepo atividadeRepo;
    @Autowired
    private ConhecimentoRepo conhecimentoRepo;
    @Autowired
    private CompetenciaRepo competenciaRepo;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private AlertaRepo alertaRepo;

    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean
    private sgc.subprocesso.service.SubprocessoNotificacaoService subprocessoNotificacaoService;

    private Unidade unidadeChefe;
    private Unidade unidadeSuperior;
    private Subprocesso subprocessoMapeamento;

    @BeforeEach
    void setUp() {
        unidadeSuperior = unidadeRepo.findById(6L).orElseThrow();
        unidadeChefe = unidadeRepo.findById(8L).orElseThrow();
        var chefe = usuarioRepo.findById("333333333333").orElseThrow();

        Processo processoMapeamento = new Processo("Processo de Mapeamento", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDateTime.now().plusDays(30));
        processoRepo.save(processoMapeamento);

        var mapa = mapaRepo.save(new sgc.mapa.model.Mapa());
        subprocessoMapeamento = new Subprocesso(processoMapeamento, unidadeChefe, mapa, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, processoMapeamento.getDataLimite());
        subprocessoRepo.save(subprocessoMapeamento);
    }

    @Nested
    @DisplayName("Testes para Disponibilizar Cadastro")
    class DisponibilizarCadastro {
        @Test
        @DisplayName("Deve disponibilizar o cadastro quando todas as condições são atendidas")
        void deveDisponibilizarCadastroComSucesso() throws Exception {
            var competencia = new Competencia("Competência de Teste", subprocessoMapeamento.getMapa());
            competenciaRepo.save(competencia);

            Atividade atividade = new Atividade(subprocessoMapeamento.getMapa(), "Atividade de Teste");
            atividade.setCompetencias(new java.util.HashSet<>(Set.of(competencia)));
            atividade = atividadeRepo.save(atividade);

            competencia.setAtividades(new java.util.HashSet<>(Set.of(atividade)));
            competenciaRepo.save(competencia);
            conhecimentoRepo.save(new Conhecimento("Conhecimento de Teste", atividade));

            mockMvc.perform(post("/api/subprocessos/{id}/cadastro/disponibilizar", subprocessoMapeamento.getCodigo()).with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Cadastro de atividades disponibilizado")));

            Subprocesso subprocessoAtualizado = subprocessoRepo.findById(subprocessoMapeamento.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
            assertThat(subprocessoAtualizado.getDataFimEtapa1()).isNotNull();

            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoAtualizado.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            Movimentacao movimentacao = movimentacoes.getFirst();
            assertThat(movimentacao.getDescricao()).isEqualTo("Disponibilização do cadastro de atividades");
            assertThat(movimentacao.getUnidadeOrigem().getCodigo()).isEqualTo(unidadeChefe.getCodigo());
            assertThat(movimentacao.getUnidadeDestino().getCodigo()).isEqualTo(unidadeSuperior.getCodigo());

            var alertas = alertaRepo.findByProcessoCodigo(subprocessoMapeamento.getProcesso().getCodigo());
            assertThat(alertas).hasSize(1);
            var alerta = alertas.getFirst();
            assertThat(alerta.getDescricao()).isEqualTo("Cadastro de atividades e conhecimentos da unidade SEDESENV submetido para análise");
            assertThat(alerta.getUnidadeDestino()).isEqualTo(unidadeSuperior);

            verify(subprocessoNotificacaoService).notificarAceiteCadastro(
                    org.mockito.ArgumentMatchers.any(Subprocesso.class),
                    org.mockito.ArgumentMatchers.any(Unidade.class)
            );
        }

        @Test
        @WithMockChefe
        @DisplayName("Não deve disponibilizar se houver atividade sem conhecimento associado")
        void naoDeveDisponibilizarComAtividadeSemConhecimento() throws Exception {
            Atividade atividade = new Atividade(subprocessoMapeamento.getMapa(), "Atividade Vazia");
            atividadeRepo.save(atividade);

            mockMvc.perform(post("/api/subprocessos/{id}/cadastro/disponibilizar", subprocessoMapeamento.getCodigo()).with(csrf()))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.message", is("Existem atividades sem conhecimentos associados.")));

            Subprocesso subprocessoNaoAlterado = subprocessoRepo.findById(subprocessoMapeamento.getCodigo()).orElseThrow();
            assertThat(subprocessoNaoAlterado.getSituacao()).isEqualTo(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
        }
    }

    @Nested
    @DisplayName("Testes de Segurança")
    class Seguranca {
        @Test
        @WithMockChefe("999999999999")
        @DisplayName("Não deve permitir que um CHEFE de outra unidade disponibilize o cadastro")
        void naoDevePermitirChefeDeOutraUnidadeDisponibilizar() throws Exception {
            mockMvc.perform(post("/api/subprocessos/{id}/cadastro/disponibilizar", subprocessoMapeamento.getCodigo()).with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
