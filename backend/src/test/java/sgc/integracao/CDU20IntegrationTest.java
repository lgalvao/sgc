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
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.DevolverValidacaoReq;
import sgc.subprocesso.model.*;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
    private UsuarioService usuarioService;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    private Subprocesso subprocesso;
    private Unidade unidade;
    private Unidade unidadeSuperior;
    private Unidade unidadeSuperiorSuperior;
    private Usuario usuarioGestor;
    private Usuario usuarioChefe;

    @BeforeEach
    void setUp() {
        // Use existing 3-level hierarchy from data.sql:
        // Unit 2 (STIC - INTEROPERACIONAL) - top level
        // Unit 6 (COSIS - INTERMEDIARIA) - subordinate to 2
        // Unit 9 (SEDIA - OPERACIONAL) - subordinate to 6
        // User '666666666666' is GESTOR of unit 6
        // User '333333333333' is CHEFE of unit 9
        unidadeSuperiorSuperior = unidadeRepo.findById(2L)
                .orElseThrow(() -> new RuntimeException("Unit 2 not found in data.sql"));
        
        unidadeSuperior = unidadeRepo.findById(6L)
                .orElseThrow(() -> new RuntimeException("Unit 6 not found in data.sql"));
        
        unidade = unidadeRepo.findById(9L)
                .orElseThrow(() -> new RuntimeException("Unit 9 not found in data.sql"));

        // Load users from database with their profiles
        usuarioGestor = usuarioService.buscarPorLogin("666666666666");
        usuarioChefe = usuarioService.buscarPorLogin("333333333333");

        // Create test process and subprocess
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setDescricao("Processo de Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);

        // Create subprocess in MAPEAMENTO_MAPA_VALIDADO state for unit 9
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocesso = subprocessoRepo.save(subprocesso);
        subprocessoRepo.flush();
    }

    @Test
    @DisplayName("Devolução e aceitação da validação do mapa com verificação do histórico")
    void devolucaoEAceitacaoComVerificacaoHistorico() throws Exception {
        // Devolução do mapa (GESTOR of unit 6 devolves to subordinate unit 9)
        DevolverValidacaoReq devolverReq = new DevolverValidacaoReq("Justificativa da devolução");
        mockMvc.perform(post("/api/subprocessos/{id}/devolver-validacao", subprocesso.getCodigo())
                                .with(user(usuarioGestor))
                                .with(csrf())
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(devolverReq)))
                .andExpect(status().isOk());

        // Verificação do histórico após devolução
        String responseDevolucao =
                mockMvc.perform(get("/api/subprocessos/{id}/historico-validacao", subprocesso.getCodigo())
                        .with(user(usuarioGestor))
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

        // Unidade inferior valida o mapa novamente (CHEFE of unit 9)
        mockMvc.perform(post("/api/subprocessos/{id}/validar-mapa", subprocesso.getCodigo())
                                .with(user(usuarioChefe))
                                .with(csrf()))
                .andExpect(status().isOk());

        // GESTOR da unidade superior aceita a validação
        mockMvc.perform(post("/api/subprocessos/{id}/aceitar-validacao", subprocesso.getCodigo())
                                .with(user(usuarioGestor))
                                .with(csrf()))
                .andExpect(status().isOk());

        // Verificação do histórico após aceite
        String responseAceite =
                mockMvc.perform(get("/api/subprocessos/{id}/historico-validacao", subprocesso.getCodigo())
                                        .with(user(usuarioGestor))
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
