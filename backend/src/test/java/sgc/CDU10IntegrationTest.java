package sgc;

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
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.modelo.SituacaoProcesso;
import sgc.comum.modelo.SituacaoSubprocesso;
import sgc.processo.modelo.TipoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;
import sgc.mapa.modelo.MapaRepo;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.comum.modelo.UsuarioRepo;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.atividade.modelo.Atividade;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.alerta.modelo.AlertaRepo;
import sgc.subprocesso.modelo.Movimentacao;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import sgc.notificacao.NotificacaoServico;

import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;


import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockChefe
@Import({TestSecurityConfig.class, WithMockChefeSecurityContextFactory.class})
@Transactional
@DisplayName("CDU-10: Disponibilizar Revisão do Cadastro de Atividades e Conhecimentos")
class CDU10IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Repositories
    @Autowired private ProcessoRepo processoRepo;
    @Autowired private UnidadeRepo unidadeRepo;
    @Autowired private SubprocessoRepo subprocessoRepo;
    @Autowired private MapaRepo mapaRepo;
    @Autowired private AtividadeRepo atividadeRepo;
    @Autowired private ConhecimentoRepo conhecimentoRepo;
    @Autowired private UsuarioRepo usuarioRepo;
    @Autowired private MovimentacaoRepo movimentacaoRepo;
    @Autowired private AlertaRepo alertaRepo;

    @MockitoBean
    private NotificacaoServico notificacaoServico;

    // Test data
    private Unidade unidadeChefe;
    private Unidade unidadeSuperior;
    private Processo processoRevisao;
    private Subprocesso subprocessoRevisao;

    @BeforeEach
    void setUp() {
        unidadeSuperior = new Unidade("Unidade Superior", "US");
        unidadeRepo.save(unidadeSuperior);

        unidadeChefe = new Unidade("Unidade Teste", "UT");
        unidadeChefe.setUnidadeSuperior(unidadeSuperior);
        var chefe = new sgc.comum.modelo.Usuario();
        chefe.setTitulo("chefe");
        chefe = usuarioRepo.save(chefe);
        unidadeChefe.setTitular(chefe);
        unidadeRepo.save(unidadeChefe);

        processoRevisao = new Processo("Processo de Revisão", TipoProcesso.REVISAO, SituacaoProcesso.EM_ANDAMENTO, LocalDate.now().plusDays(30));
        processoRepo.save(processoRevisao);

        var mapa = mapaRepo.save(new sgc.mapa.modelo.Mapa());
        subprocessoRevisao = new Subprocesso(processoRevisao, unidadeChefe, mapa, SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, processoRevisao.getDataLimite());
        subprocessoRepo.save(subprocessoRevisao);
    }

    @Nested
    @DisplayName("Testes para Disponibilizar Revisão do Cadastro")
    class DisponibilizarRevisaoCadastro {

        @Test
        @DisplayName("Deve disponibilizar a revisão do cadastro com sucesso quando todas as condições são atendidas")
        void deveDisponibilizarRevisaoComSucesso() throws Exception {
            // Arrange: Create an activity with an associated knowledge
            Atividade atividade = new Atividade(subprocessoRevisao.getMapa(), "Atividade de Teste");
            atividadeRepo.save(atividade);
            conhecimentoRepo.save(new Conhecimento(atividade, "Conhecimento de Teste"));

            // Act & Assert
            mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoRevisao.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Revisão do cadastro de atividades disponibilizada")));

            // Assert database state
            Subprocesso subprocessoAtualizado = subprocessoRepo.findById(subprocessoRevisao.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
            assertThat(subprocessoAtualizado.getDataFimEtapa1()).isNotNull();

            // Assert Movimentacao
            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoAtualizado.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            Movimentacao movimentacao = movimentacoes.get(0);
            assertThat(movimentacao.getDescricao()).isEqualTo("Disponibilização da revisão do cadastro de atividades");
            assertThat(movimentacao.getUnidadeOrigem()).isEqualTo(unidadeChefe);
            assertThat(movimentacao.getUnidadeDestino()).isEqualTo(unidadeSuperior);

            // Assert Alerta
            var alertas = alertaRepo.findAll();
            assertThat(alertas).hasSize(1);
            var alerta = alertas.get(0);
            assertThat(alerta.getDescricao()).isEqualTo("Cadastro de atividades e conhecimentos da unidade UT disponibilizado para análise");
            assertThat(alerta.getUnidadeDestino()).isEqualTo(unidadeSuperior);

            // Assert Notificação
            String assuntoEsperado = "SGC: Revisão do cadastro de atividades e conhecimentos disponibilizada: UT";
            String corpoEsperado = "Prezado(a) responsável pela US,\n" +
                                   "A unidade UT concluiu a revisão e disponibilizou seu cadastro de atividades e conhecimentos do processo Processo de Revisão.\n" +
                                   "A análise desse cadastro já pode ser realizada no O sistema de Gestão de Competências ([URL_SISTEMA]).";
            verify(notificacaoServico).enviarEmail(eq(unidadeSuperior.getSigla()), eq(assuntoEsperado), eq(corpoEsperado));
        }

        @Test
        @DisplayName("Não deve disponibilizar se houver atividade sem conhecimento associado")
        void naoDeveDisponibilizarComAtividadeSemConhecimento() throws Exception {
            // Arrange: Create an activity without any knowledge
            Atividade atividade = new Atividade(subprocessoRevisao.getMapa(), "Atividade Vazia");
            atividadeRepo.save(atividade);

            // Act & Assert
            mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoRevisao.getCodigo()))
                .andExpect(status().isUnprocessableEntity());

            // Assert that the state has not changed
            Subprocesso subprocessoNaoAlterado = subprocessoRepo.findById(subprocessoRevisao.getCodigo()).orElseThrow();
            assertThat(subprocessoNaoAlterado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        }
    }

    @Nested
    @DisplayName("Testes de Segurança")
    class Seguranca {

        @Test
        @WithMockChefe("outro_chefe")
        @DisplayName("Não deve permitir que um CHEFE de outra unidade disponibilize a revisão")
        void naoDevePermitirChefeDeOutraUnidadeDisponibilizar() throws Exception {
            // Arrange: create another user
            var outroChefe = new sgc.comum.modelo.Usuario();
            outroChefe.setTitulo("outro_chefe");
            usuarioRepo.save(outroChefe);

            // Act & Assert
            mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoRevisao.getCodigo()))
                .andExpect(status().isForbidden());
        }
    }
}