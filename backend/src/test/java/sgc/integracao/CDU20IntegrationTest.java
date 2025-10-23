package sgc.integracao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.analise.modelo.TipoAcaoAnalise;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockChefe;
import sgc.processo.SituacaoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.sgrh.Perfil;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.dto.DevolverValidacaoReq;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.subprocesso.SubprocessoNotificacaoService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-20: Analisar validação de mapa de competências")
public class CDU20IntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ProcessoRepo processoRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @MockitoSpyBean
    private SubprocessoNotificacaoService subprocessoNotificacaoService;


    private Subprocesso subprocesso;
    private Unidade unidadeSuperior;
    private Unidade unidadeSuperiorSuperior;

    @BeforeEach
    void setUp() {
        Unidade sedoc = unidadeRepo.save(new Unidade("SEC. DOCUMENTACAO", "SEDOC"));
        Usuario adminMock = new Usuario();
        adminMock.setTituloEleitoral(111111111111L);
        adminMock.setPerfis(java.util.Set.of(Perfil.ADMIN));
        adminMock.setUnidade(sedoc);
        usuarioRepo.save(adminMock);
        sedoc.setTitular(adminMock);
        unidadeRepo.save(sedoc);

        unidadeSuperiorSuperior = unidadeRepo.save(new Unidade("Unidade Superior Superior", "UNISUPSUP"));
        unidadeSuperior = new Unidade("Unidade Superior", "UNISUP");
        unidadeSuperior.setUnidadeSuperior(unidadeSuperiorSuperior);
        unidadeRepo.save(unidadeSuperior);

        Unidade unidade = new Unidade("Unidade Subprocesso", "UNISUB");
        unidade.setUnidadeSuperior(unidadeSuperior);
        unidadeRepo.save(unidade);

        // Criar usuários mockados para as unidades
        Usuario chefeMock = new Usuario();
        chefeMock.setTituloEleitoral(333333333333L);
        chefeMock.setPerfis(java.util.Set.of(Perfil.CHEFE));
        chefeMock.setUnidade(unidadeSuperior);
        usuarioRepo.save(chefeMock);
        unidadeSuperior.setTitular(chefeMock);
        unidadeRepo.save(unidadeSuperior);

        Usuario gestorMock = new Usuario();
        gestorMock.setTituloEleitoral(222222222222L);
        gestorMock.setPerfis(java.util.Set.of(Perfil.GESTOR));
        gestorMock.setUnidade(unidade);
        usuarioRepo.save(gestorMock);
        unidade.setTitular(gestorMock);
        unidadeRepo.save(unidade);

        Processo processo = processoRepo.save(new Processo("Processo de Teste", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDateTime.now()));
        subprocesso = subprocessoRepo.save(
                new Subprocesso(processo, unidade, null, SituacaoSubprocesso.MAPA_VALIDADO, LocalDateTime.now())
        );
        subprocessoRepo.flush();
    }

    @Test
    @DisplayName("Devolução e aceite da validação do mapa com verificação do histórico")
    @WithMockChefe()
    void devolucaoEaceiteComVerificacaoHistorico() throws Exception {
        // Devolução do mapa
        DevolverValidacaoReq devolverReq = new DevolverValidacaoReq("Justificativa da devolução");
        mockMvc.perform(post("/api/subprocessos/{id}/devolver-validacao", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(devolverReq)))
                .andExpect(status().isOk());

        // Verificação do histórico após devolução
        String responseDevolucao = mockMvc.perform(get("/api/subprocessos/{id}/historico-validacao", subprocesso.getCodigo())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<sgc.analise.dto.AnaliseValidacaoHistoricoDto> historicoDevolucao = objectMapper.readValue(responseDevolucao, new TypeReference<>() {
        });

        assertThat(historicoDevolucao).hasSize(1);
        assertThat(historicoDevolucao.getFirst().acao()).isEqualTo(TipoAcaoAnalise.DEVOLUCAO);
        assertThat(historicoDevolucao.getFirst().unidadeSigla()).isNotNull();
        assertThat(historicoDevolucao.getFirst().observacoes()).isEqualTo("Justificativa da devolução");

        // Adicionar verificação de Movimentacao e Alerta após devolução
        List<Movimentacao> movimentacoesDevolucao = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
        assertThat(movimentacoesDevolucao).hasSize(1);
        assertThat(movimentacoesDevolucao.getFirst().getDescricao()).isEqualTo("Devolução da validação do mapa de competências para ajustes");
        assertThat(movimentacoesDevolucao.getFirst().getUnidadeOrigem().getSigla()).isEqualTo(unidadeSuperior.getSigla());
        assertThat(movimentacoesDevolucao.getFirst().getUnidadeDestino().getSigla()).isEqualTo(subprocesso.getUnidade().getSigla());

        List<Alerta> alertasDevolucao = alertaRepo.findAll();
        assertThat(alertasDevolucao).hasSize(1);
        assertThat(alertasDevolucao.getFirst().getDescricao()).contains("Cadastro de atividades e conhecimentos da unidade UNISUB devolvido para ajustes");
        assertThat(alertasDevolucao.getFirst().getUnidadeDestino().getSigla()).isEqualTo(subprocesso.getUnidade().getSigla());

        // Unidade inferior valida o mapa novamente
        mockMvc.perform(post("/api/subprocessos/{id}/validar-mapa", subprocesso.getCodigo())
                        .with(csrf()))
                .andExpect(status().isOk());

        // Chefe da unidade superior aceita a validação
        mockMvc.perform(post("/api/subprocessos/{id}/aceitar-validacao", subprocesso.getCodigo())
                        .with(csrf()))
                .andExpect(status().isOk());

        // Verificação do histórico após aceite
        String responseAceite = mockMvc.perform(get("/api/subprocessos/{id}/historico-validacao", subprocesso.getCodigo())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<sgc.analise.dto.AnaliseValidacaoHistoricoDto> historicoAceite = objectMapper.readValue(responseAceite, new TypeReference<>() {
        });

        assertThat(historicoAceite).hasSize(2);
        assertThat(historicoAceite.getFirst().acao()).isEqualTo(TipoAcaoAnalise.ACEITE);
        assertThat(historicoAceite.getFirst().unidadeSigla()).isNotNull();

        // Adicionar verificação de Movimentacao e Alerta após aceite
        List<Movimentacao> movimentacoesAceite = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
        assertThat(movimentacoesAceite).hasSize(3); // Movimentação inicial + devolução + aceite
        assertThat(movimentacoesAceite.getFirst().getDescricao()).isEqualTo("Mapa de competências validado");
        assertThat(movimentacoesAceite.getFirst().getUnidadeOrigem().getSigla()).isEqualTo(unidadeSuperior.getSigla());
        assertThat(movimentacoesAceite.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidadeSuperiorSuperior.getSigla());

        List<Alerta> alertasAceite = alertaRepo.findAll();
        assertThat(alertasAceite).hasSize(3); // devolução + validação + aceite

        // Verifica o alerta de aceite para a unidade hierarquicamente superior
        Alerta alertaDeAceite = alertasAceite.stream()
            .filter(a -> a.getUnidadeDestino().getSigla().equals(unidadeSuperiorSuperior.getSigla()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Alerta de aceite para unidade superior não encontrado"));

        assertThat(alertaDeAceite.getDescricao()).contains("Validação do mapa de competências da UNISUB submetida para análise");
    }

    @Test
    @DisplayName("ADMIN deve homologar validação do mapa, alterando status para MAPA_HOMOLOGADO e registrando movimentação e alerta")
    @WithMockAdmin
    void testHomologarValidacao_Sucesso() throws Exception {
        // Cenário: Subprocesso já validado e pronto para homologação
        subprocesso.setSituacao(SituacaoSubprocesso.MAPA_VALIDADO);
        subprocessoRepo.save(subprocesso);
        subprocessoRepo.flush();

        // Ação
        mockMvc.perform(post("/api/subprocessos/{id}/homologar-validacao", subprocesso.getCodigo())
                        .with(csrf()))
                .andExpect(status().isOk());

        // Verificações
        Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(spAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_HOMOLOGADO);

        List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
        assertThat(movimentacoes).hasSize(1); // Apenas a movimentação de homologação
        assertThat(movimentacoes.getFirst().getDescricao()).isEqualTo("Mapa de competências homologado");
        assertThat(movimentacoes.getFirst().getUnidadeOrigem().getSigla()).isEqualTo("SEDOC");
        assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla()).isEqualTo("SEDOC");

        List<Alerta> alertas = alertaRepo.findAll();
        assertThat(alertas).hasSize(1);
        assertThat(alertas.getFirst().getDescricao()).contains("Mapa de competências do processo Processo de Teste homologado");
        assertThat(alertas.getFirst().getUnidadeDestino().getSigla()).isEqualTo("SEDOC");

        verify(subprocessoNotificacaoService, times(1)).notificarHomologacaoMapa(spAtualizado);
    }
}
