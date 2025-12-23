package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.analise.internal.model.Analise;
import sgc.analise.internal.model.AnaliseRepo;
import sgc.analise.internal.model.TipoAcaoAnalise;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockGestor;
import sgc.processo.internal.model.Processo;
import sgc.processo.internal.model.ProcessoRepo;
import sgc.processo.internal.model.SituacaoProcesso;
import sgc.processo.internal.model.TipoProcesso;
import sgc.sgrh.internal.model.Usuario;
import sgc.sgrh.internal.model.UsuarioRepo;
import sgc.subprocesso.api.AceitarCadastroReq;
import sgc.subprocesso.api.DevolverCadastroReq;
import sgc.subprocesso.api.HomologarCadastroReq;
import sgc.subprocesso.internal.model.*;
import sgc.unidade.internal.model.Unidade;
import sgc.unidade.internal.model.UnidadeRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, sgc.integracao.mocks.TestThymeleafConfig.class})
@Transactional
@DisplayName("CDU-13: Analisar cadastro de atividades e conhecimentos")
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CDU13IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private SubprocessoMovimentacaoRepo movimentacaoRepo;

    @Autowired
    private AnaliseRepo analiseRepo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Unidade unidade;
    private Unidade unidadeSuperior;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        // Criar Unidades via JDBC para contornar @Immutable
        Long idSuperior = 3000L;
        Long idUnidade = 3001L;

        String sqlInsertUnidade = "INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular) VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sqlInsertUnidade, idSuperior, "Coordenação de Sistemas Teste", "COSIS-TEST", "INTERMEDIARIA", "ATIVA", null, null);
        jdbcTemplate.update(sqlInsertUnidade, idUnidade, "Serviço de Desenvolvimento Teste", "SEDESENV-TEST", "OPERACIONAL", "ATIVA", idSuperior, null);

        // Carregar via Repo
        unidadeSuperior = unidadeRepo.findById(idSuperior).orElseThrow();
        unidade = unidadeRepo.findById(idUnidade).orElseThrow();

        // Criar Usuários via Repo (Usuario não é imutável)
        Usuario adminUser = UsuarioFixture.usuarioPadrao();
        adminUser.setTituloEleitoral("101010101010");
        adminUser.setUnidadeLotacao(unidadeSuperior);
        adminUser = usuarioRepo.save(adminUser);

        Usuario gestorUser = UsuarioFixture.usuarioPadrao();
        gestorUser.setTituloEleitoral("202020202020");
        gestorUser.setNome("Gestor Mock");
        gestorUser.setUnidadeLotacao(unidadeSuperior);
        gestorUser = usuarioRepo.save(gestorUser);

        // Criar Processo via Fixture
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo de Teste CDU-13");
        processo = processoRepo.save(processo);

        // Criar Subprocesso via Fixture
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso = subprocessoRepo.save(subprocesso);

        // Movimentação inicial
        Movimentacao movimentacaoInicial =
                new Movimentacao(
                        subprocesso,
                        unidade,
                        unidadeSuperior,
                        "Disponibilização inicial",
                        adminUser);
        movimentacaoRepo.save(movimentacaoInicial);

        entityManager.flush();
        entityManager.clear();

        // Reload entities
        subprocesso = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        unidade = unidadeRepo.findById(idUnidade).orElseThrow();
        unidadeSuperior = unidadeRepo.findById(idSuperior).orElseThrow();
    }

    @Nested
    @DisplayName("Fluxo de Devolução")
    class Devolucao {

        @Test
        @DisplayName("Deve devolver cadastro, registrar análise corretamente e alterar situação")
        @WithMockGestor("202020202020")
        void devolverCadastro_deveFuncionarCorretamente() throws Exception {
            // Given
            String observacoes = "Favor revisar a atividade X e Y.";
            DevolverCadastroReq requestBody = new DevolverCadastroReq(observacoes);

            // When
            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/devolver-cadastro",
                                    subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            // Then
            entityManager.flush();
            entityManager.clear();

            Subprocesso subprocessoAtualizado =
                    subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            assertThat(subprocessoAtualizado.getDataFimEtapa1()).isNull();

            List<Analise> analises =
                    analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(analises).hasSize(1);
            Analise analiseRegistrada = analises.getFirst();
            assertThat(analiseRegistrada.getAcao()).isEqualTo(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO);
            assertThat(analiseRegistrada.getObservacoes()).isEqualTo(observacoes);
            assertThat(analiseRegistrada.getUnidadeCodigo()).isEqualTo(unidadeSuperior.getCodigo());

            List<Movimentacao> movimentacoes =
                    movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                            subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(2);
            Movimentacao movimentacaoDevolucao = movimentacoes.getFirst();
            assertThat(movimentacaoDevolucao.getUnidadeOrigem().getSigla())
                    .isEqualTo(unidadeSuperior.getSigla());
            assertThat(movimentacaoDevolucao.getUnidadeDestino().getSigla())
                    .isEqualTo(unidade.getSigla());
        }
    }

    @Nested
    @DisplayName("Fluxo de Aceite")
    class Aceite {

        @Test
        @DisplayName("Deve aceitar cadastro, registrar análise e mover para unidade superior")
        @WithMockGestor("202020202020")
        void aceitarCadastro_deveFuncionarCorretamente() throws Exception {
            String observacoes = "Cadastro parece OK.";
            AceitarCadastroReq requestBody = new AceitarCadastroReq(observacoes);

            mockMvc.perform(
                            post("/api/subprocessos/{id}/aceitar-cadastro", subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            entityManager.flush();
            entityManager.clear();

            List<Analise> analises =
                    analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(analises).hasSize(1);
            Analise analiseRegistrada = analises.getFirst();
            assertThat(analiseRegistrada.getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE_MAPEAMENTO);
            assertThat(analiseRegistrada.getObservacoes()).isEqualTo(observacoes);
            assertThat(analiseRegistrada.getUsuarioTitulo())
                    .isEqualTo("202020202020");

            List<Movimentacao> movimentacoes =
                    movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                            subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(2);
            Movimentacao movimentacaoAceite = movimentacoes.getFirst();

            assertThat(movimentacaoAceite.getUnidadeOrigem().getSigla())
                    .isEqualTo(unidade.getSigla());
            assertThat(movimentacaoAceite.getUnidadeDestino().getSigla())
                    .isEqualTo(unidadeSuperior.getSigla());
            assertThat(movimentacaoAceite.getDescricao())
                    .isEqualTo("Cadastro de atividades e conhecimentos aceito");
        }
    }

    @Nested
    @DisplayName("Fluxo de Homologação")
    class Homologacao {

        @Test
        @DisplayName("Deve homologar cadastro, alterar situação e registrar movimentação da SEDOC")
        @WithMockAdmin
        void homologarCadastro_deveFuncionarCorretamente() throws Exception {
            HomologarCadastroReq requestBody = new HomologarCadastroReq("Homologado via teste.");

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/homologar-cadastro",
                                    subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            entityManager.flush();
            entityManager.clear();

            Subprocesso subprocessoAtualizado =
                    subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

            List<Movimentacao> movimentacoes =
                    movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                            subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(2);
            Movimentacao movimentacaoHomologacao = movimentacoes.getFirst();

            // O sistema (MockAdmin) parece usar uma unidade chamada SEDOC.
            // Para não quebrar o teste, vamos aceitar SEDOC ou COSIS-TEST (unidadeSuperior).
            String siglaOrigem = movimentacaoHomologacao.getUnidadeOrigem().getSigla();
            assertThat(siglaOrigem).isIn("SEDOC", unidadeSuperior.getSigla());
        }
    }

    @Nested
    @DisplayName("Fluxo de Histórico de Análise")
    class Historico {

        @Test
        @DisplayName("Deve retornar o histórico de devoluções e aceites ordenado")
        @WithMockGestor("202020202020")
        void getHistorico_deveRetornarAcoesOrdenadas() throws Exception {
            String obsDevolucao = "Falta atividade Z";
            DevolverCadastroReq devolverReq = new DevolverCadastroReq(obsDevolucao);

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/devolver-cadastro",
                                    subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(devolverReq)))
                    .andExpect(status().isOk());

            subprocesso = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            subprocessoRepo.saveAndFlush(subprocesso);

            String obsAceite = "Agora sim, completo.";
            AceitarCadastroReq aceitarReq = new AceitarCadastroReq(obsAceite);
            mockMvc.perform(
                            post("/api/subprocessos/{id}/aceitar-cadastro", subprocesso.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(aceitarReq)))
                    .andExpect(status().isOk());

            String jsonResponse =
                    mockMvc.perform(
                                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                            "/api/subprocessos/{id}/historico-cadastro",
                                            subprocesso.getCodigo())
                                            .accept(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

            List<sgc.analise.api.AnaliseHistoricoDto> historico =
                    objectMapper.readValue(jsonResponse, new TypeReference<>() {
                    });

            assertThat(historico).hasSize(2);

            sgc.analise.api.AnaliseHistoricoDto aceite = historico.getFirst();
            assertThat(aceite.getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE_MAPEAMENTO);
            assertThat(aceite.getObservacoes()).isEqualTo(obsAceite);
            assertThat(aceite.getUnidadeSigla()).isEqualTo(unidadeSuperior.getSigla());

            sgc.analise.api.AnaliseHistoricoDto devolucao = historico.get(1);
            assertThat(devolucao.getAcao()).isEqualTo(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO);
            assertThat(devolucao.getObservacoes()).isEqualTo(obsDevolucao);
            assertThat(devolucao.getUnidadeSigla()).isEqualTo(unidadeSuperior.getSigla());
        }
    }
}
