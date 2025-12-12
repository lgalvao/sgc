package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;
import sgc.alerta.model.AlertaRepo;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
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
import sgc.subprocesso.service.SubprocessoNotificacaoService;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;
import org.springframework.jdbc.core.JdbcTemplate;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@WithMockChefe("333333333333")
@Import({
        TestSecurityConfig.class,
        WithMockChefeSecurityContextFactory.class,
        TestThymeleafConfig.class
})
@Transactional
@DisplayName("CDU-09: Disponibilizar Cadastro de Atividades e Conhecimentos")
class CDU09IntegrationTest extends BaseIntegrationTest {
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
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private EntityManager entityManager;

    @MockitoSpyBean
    private SubprocessoNotificacaoService subprocessoNotificacaoService;

    @MockitoBean
    private JavaMailSender javaMailSender;

    private Unidade unidadeChefe;
    private Unidade unidadeSuperior;
    private Subprocesso subprocessoMapeamento;

    @BeforeEach
    void setUp() {
        when(javaMailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        unidadeSuperior = unidadeRepo.findById(6L).orElseThrow();
        unidadeChefe = unidadeRepo.findById(10L).orElseThrow();

        Processo processoMapeamento =
                new Processo(
                        "Processo de Mapeamento",
                        TipoProcesso.MAPEAMENTO,
                        SituacaoProcesso.EM_ANDAMENTO,
                        LocalDateTime.now().plusDays(30));

        processoRepo.save(processoMapeamento);

        var mapa = mapaRepo.save(new sgc.mapa.model.Mapa());
        subprocessoMapeamento =
                new Subprocesso(
                        processoMapeamento,
                        unidadeChefe,
                        mapa,
                        SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                        processoMapeamento.getDataLimite());
        subprocessoRepo.save(subprocessoMapeamento);

        // Configurar perfil de CHEFE para o usuário do teste (333333333333) na unidade correta (SESEL - 10L)
        // O usuário 333333333333 já existe em data.sql como "Chefe Teste", unidade 8.
        // Precisamos atualizar para unidade 10 ou adicionar perfil.
        Usuario chefe = usuarioRepo.findById("333333333333").orElseThrow();
        // Insert na VIEW_USUARIO_PERFIL_UNIDADE
        jdbcTemplate.update("INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)",
                chefe.getTituloEleitoral(), unidadeChefe.getCodigo(), Perfil.CHEFE.name());

        // Definir o usuário como TITULAR da unidade na VIEW VW_UNIDADE
        jdbcTemplate.update("UPDATE SGC.VW_UNIDADE SET titulo_titular = ? WHERE codigo = ?",
                chefe.getTituloEleitoral(), unidadeChefe.getCodigo());

        // Refresh na entidade Unidade para refletir a mudança no banco (já que é @Immutable, hibernate não sabe da mudança via JDBC)
        entityManager.refresh(unidadeChefe);

        // Definir titular da unidade superior (COSIS - 6) para que o envio de e-mail não falhe
        jdbcTemplate.update("UPDATE SGC.VW_UNIDADE SET titulo_titular = ? WHERE codigo = ?",
                "666666666666", unidadeSuperior.getCodigo());
        entityManager.refresh(unidadeSuperior);
    }

    @Nested
    @DisplayName("Testes para Disponibilizar Cadastro")
    class DisponibilizarCadastro {
        @Test
        @DisplayName("Deve disponibilizar o cadastro quando todas as condições são atendidas")
        void deveDisponibilizarCadastroComSucesso() throws Exception {
            var competencia =
                    competenciaRepo.save(
                            new Competencia(
                                    "Competência de Teste", subprocessoMapeamento.getMapa()));
            var atividade = new Atividade(subprocessoMapeamento.getMapa(), "Atividade de Teste");
            atividade.getCompetencias().add(competencia);
            atividadeRepo.save(atividade);
            competencia.getAtividades().add(atividade);
            competenciaRepo.save(competencia);
            conhecimentoRepo.save(new Conhecimento("Conhecimento de Teste", atividade));

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/cadastro/disponibilizar",
                                    subprocessoMapeamento.getCodigo())
                                    .with(csrf()))
                    .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Cadastro de atividades disponibilizado")));

            Subprocesso subprocessoAtualizado =
                    subprocessoRepo.findById(subprocessoMapeamento.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            assertThat(subprocessoAtualizado.getDataFimEtapa1()).isNotNull();

            List<Movimentacao> movimentacoes =
                    movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                            subprocessoAtualizado.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            Movimentacao movimentacao = movimentacoes.getFirst();
            assertThat(movimentacao.getDescricao())
                    .isEqualTo("Disponibilização do cadastro de atividades");
            assertThat(movimentacao.getUnidadeOrigem().getCodigo())
                    .isEqualTo(unidadeChefe.getCodigo());
            assertThat(movimentacao.getUnidadeDestino().getCodigo())
                    .isEqualTo(unidadeSuperior.getCodigo());

            var alertas =
                    alertaRepo.findByProcessoCodigo(
                            subprocessoMapeamento.getProcesso().getCodigo());
            assertThat(alertas).hasSize(1);
            var alerta = alertas.getFirst();
            assertThat(alerta.getDescricao())
                    .isEqualTo(
                            "Cadastro de atividades/conhecimentos da unidade SESEL disponibilizado"
                                    + " para análise");
            assertThat(alerta.getUnidadeDestino()).isEqualTo(unidadeSuperior);

            verify(subprocessoNotificacaoService)
                    .notificarDisponibilizacaoCadastro(
                            org.mockito.ArgumentMatchers.any(Subprocesso.class),
                            org.mockito.ArgumentMatchers.any(Unidade.class));
        }

        @Test
        @DisplayName("Não deve disponibilizar se houver atividade sem conhecimento associado")
        void naoDeveDisponibilizarComAtividadeSemConhecimento() throws Exception {
            Atividade atividade = new Atividade(subprocessoMapeamento.getMapa(), "Atividade Vazia");
            atividadeRepo.save(atividade);

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/cadastro/disponibilizar",
                                    subprocessoMapeamento.getCodigo())
                                    .with(csrf()))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(
                            jsonPath(
                                    "$.message",
                                    is("Existem atividades sem conhecimentos associados.")))
                    .andExpect(
                            jsonPath(
                                    "$.details.atividadesSemConhecimento[0].descricao",
                                    is("Atividade Vazia")));

            Subprocesso subprocessoNaoAlterado =
                    subprocessoRepo.findById(subprocessoMapeamento.getCodigo()).orElseThrow();
            assertThat(subprocessoNaoAlterado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        }
    }

    @Nested
    @DisplayName("Testes de Segurança")
    class Seguranca {
        @Test
        @WithMockChefe("999999999999")
        @DisplayName("Não deve permitir que um CHEFE de outra unidade disponibilize o cadastro")
        void naoDevePermitirChefeDeOutraUnidadeDisponibilizar() throws Exception {
            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/cadastro/disponibilizar",
                                    subprocessoMapeamento.getCodigo())
                                    .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
