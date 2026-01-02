package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockChefe;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.DevolverValidacaoReq;
import sgc.subprocesso.model.*;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-20: Analisar validação de mapa de competências")
@Import(TestThymeleafConfig.class)
public class CDU20IntegrationTest extends BaseIntegrationTest {
    @Autowired
    ProcessoRepo processoRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    private Subprocesso subprocesso;
    private Unidade unidade;
    private Unidade unidadeSuperior;
    private Unidade unidadeSuperiorSuperior;

    @BeforeEach
    void setUp() {
        // Criar hierarquia de 3 níveis de unidades via Fixture
        // Nível 1: Unidade superior superior (interoperacional)
        unidadeSuperiorSuperior = UnidadeFixture.unidadePadrao();
        unidadeSuperiorSuperior.setCodigo(null);
        unidadeSuperiorSuperior.setNome("Secretaria CDU-20");
        unidadeSuperiorSuperior.setSigla("SEC20");
        unidadeSuperiorSuperior.setUnidadeSuperior(null);
        unidadeSuperiorSuperior = unidadeRepo.save(unidadeSuperiorSuperior);

        // Nível 2: Unidade superior (intermediária)
        unidadeSuperior = UnidadeFixture.unidadePadrao();
        unidadeSuperior.setCodigo(null);
        unidadeSuperior.setNome("Coordenadoria CDU-20");
        unidadeSuperior.setSigla("COORD20");
        unidadeSuperior.setUnidadeSuperior(unidadeSuperiorSuperior);
        unidadeSuperior = unidadeRepo.save(unidadeSuperior);

        // Nível 3: Unidade operacional
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setNome("Seção CDU-20");
        unidade.setSigla("SECAO20");
        unidade.setUnidadeSuperior(unidadeSuperior);
        unidade = unidadeRepo.save(unidade);

        // Criar Processo via Fixture
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setDescricao("Processo de Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);

        // Criar Subprocesso via Fixture
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocesso = subprocessoRepo.save(subprocesso);
        subprocessoRepo.flush();
    }

    @Test
    @DisplayName("Devolução e aceite da validação do mapa com verificação do histórico")
    @WithMockChefe()
    void devolucaoEaceiteComVerificacaoHistorico() throws Exception {
        // Devolução do mapa
        DevolverValidacaoReq devolverReq = new DevolverValidacaoReq("Justificativa da devolução");
        mockMvc.perform(                        post("/api/subprocessos/{id}/devolver-validacao", subprocesso.getCodigo())
                                .with(csrf())
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(devolverReq)))
                .andExpect(status().isOk());

        // Verificação do histórico após devolução
        String responseDevolucao =
                mockMvc.perform(get("/api/subprocessos/{id}/historico-validacao", subprocesso.getCodigo())
                        .with(csrf()))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        List<sgc.analise.dto.AnaliseValidacaoHistoricoDto> historicoDevolucao =
                objectMapper.readValue(responseDevolucao, new TypeReference<>() {
                });

        assertThat(historicoDevolucao).hasSize(1);
        assertThat(historicoDevolucao.getFirst().getAcao())
                .isEqualTo(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO);
        assertThat(historicoDevolucao.getFirst().getUnidadeSigla()).isNotNull();
        assertThat(historicoDevolucao.getFirst().getObservacoes())
                .isEqualTo("Justificativa da devolução");

        // Adicionar verificação de Movimentacao e Alerta após devolução
        List<Movimentacao> movimentacoesDevolucao =
                movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                        subprocesso.getCodigo());
        assertThat(movimentacoesDevolucao).hasSize(1);
        assertThat(movimentacoesDevolucao.getFirst().getDescricao())
                .isEqualTo("Devolução da validação do mapa de competências para ajustes");
        assertThat(movimentacoesDevolucao.getFirst().getUnidadeOrigem().getSigla())
                .isEqualTo(unidadeSuperior.getSigla());
        assertThat(movimentacoesDevolucao.getFirst().getUnidadeDestino().getSigla())
                .isEqualTo(subprocesso.getUnidade().getSigla());

        List<Alerta> alertasDevolucao =
                alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo());
        assertThat(alertasDevolucao).hasSize(1);
        assertThat(alertasDevolucao.getFirst().getDescricao())
                .contains(
                        "Validação do mapa da unidade " + unidade.getSigla() + " devolvida para"
                                + " ajustes");
        assertThat(alertasDevolucao.getFirst().getUnidadeDestino().getSigla())
                .isEqualTo(subprocesso.getUnidade().getSigla());

        // Unidade inferior valida o mapa novamente
        mockMvc.perform(
                        post("/api/subprocessos/{id}/validar-mapa", subprocesso.getCodigo())
                                .with(csrf()))
                .andExpect(status().isOk());

        // Chefe da unidade superior aceita a validação
        mockMvc.perform(
                        post("/api/subprocessos/{id}/aceitar-validacao", subprocesso.getCodigo())
                                .with(csrf()))
                .andExpect(status().isOk());

        // Verificação do histórico após aceite
        String responseAceite =
                mockMvc.perform(
                                get(
                                        "/api/subprocessos/{id}/historico-validacao",
                                        subprocesso.getCodigo())
                                        .with(csrf()))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        List<sgc.analise.dto.AnaliseValidacaoHistoricoDto> historicoAceite =
                objectMapper.readValue(responseAceite, new TypeReference<>() {
                });

        assertThat(historicoAceite).hasSize(2);
        assertThat(historicoAceite.getFirst().getAcao())
                .isEqualTo(TipoAcaoAnalise.ACEITE_MAPEAMENTO);
        assertThat(historicoAceite.getFirst().getUnidadeSigla()).isNotNull();

        // Adicionar verificação de Movimentacao e Alerta após aceite
        List<Movimentacao> movimentacoesAceite =
                movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                        subprocesso.getCodigo());
        assertThat(movimentacoesAceite).hasSize(3); // Movimentação inicial + devolução + aceite
        assertThat(movimentacoesAceite.getFirst().getDescricao())
                .isEqualTo("Validação do mapa aceita");
        assertThat(movimentacoesAceite.getFirst().getUnidadeOrigem().getSigla())
                .isEqualTo(unidadeSuperior.getSigla());
        assertThat(movimentacoesAceite.getFirst().getUnidadeDestino().getSigla())
                .isEqualTo(unidadeSuperiorSuperior.getSigla());

        List<Alerta> alertasAceite =
                alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo());
        assertThat(alertasAceite).hasSize(3); // devolução + validação + aceite

        // Verifica o alerta de aceite para a unidade hierarquicamente superior
        Alerta alertaDeAceite =
                alertasAceite.stream()
                        .filter(
                                a ->
                                        a.getUnidadeDestino()
                                                .getSigla()
                                                .equals(unidadeSuperiorSuperior.getSigla()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new AssertionError(
                                                "Alerta de aceite para unidade superior não"
                                                        + " encontrado"));

        assertThat(alertaDeAceite.getDescricao())
                .contains("Validação do mapa da unidade " + unidade.getSigla() + " submetida para análise");
    }

    @Test
    @DisplayName(
            "ADMIN deve homologar validação do mapa, alterando status para MAPA_HOMOLOGADO e"
                    + " registrando movimentação e alerta")
    @WithMockAdmin
    void testHomologarValidacao_Sucesso() throws Exception {
        // Cenário: Subprocesso já validado e pronto para homologação
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocessoRepo.save(subprocesso);
        subprocessoRepo.flush();

        // Ação
        mockMvc.perform(
                        post("/api/subprocessos/{id}/homologar-validacao", subprocesso.getCodigo())
                                .with(csrf()))
                .andExpect(status().isOk());

        // Verificações
        Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(spAtualizado.getSituacao())
                .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);

        List<Movimentacao> movimentacoes =
                movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                        subprocesso.getCodigo());
        assertThat(movimentacoes).hasSize(1); // Apenas a movimentação de homologação
        assertThat(movimentacoes.getFirst().getDescricao())
                .isEqualTo("Mapa de competências homologado");
        assertThat(movimentacoes.getFirst().getUnidadeOrigem().getSigla()).isEqualTo("SEDOC");
        assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla()).isEqualTo("SEDOC");

        // Homologação não gera alerta (por design)
        List<Alerta> alertas =
                alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo());
        assertThat(alertas).isEmpty(); // MAPA_HOMOLOGADO não gera alerta
    }
}
