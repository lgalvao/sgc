package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;
import sgc.comum.ComumDtos.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.mapa.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import tools.jackson.core.type.*;
import tools.jackson.databind.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-20: Analisar validação de mapa de competências")
class CDU20IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private UsuarioFacade usuarioService;

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
        usuarioGestor.setPerfilAtivo(Perfil.GESTOR);
        usuarioGestor.setUnidadeAtivaCodigo(6L);
        usuarioGestor.setAuthorities(Set.of(Perfil.GESTOR.toGrantedAuthority()));

        usuarioChefe = usuarioService.buscarPorLogin("333333333333");
        usuarioChefe.setPerfilAtivo(Perfil.CHEFE);
        usuarioChefe.setUnidadeAtivaCodigo(9L);
        usuarioChefe.setAuthorities(Set.of(Perfil.CHEFE.toGrantedAuthority()));

        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setDescricao("Processo de Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);

        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocesso = subprocessoRepo.save(subprocesso);

        // Simular que o mapa foi validado e enviado para a unidade superior (6)
        Movimentacao m = Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidade)
                .unidadeDestino(unidadeSuperior)
                .descricao("Mapa validado e submetido para análise")
                .usuario(usuarioChefe)
                .build();
        movimentacaoRepo.save(m);

        subprocessoRepo.flush();
    }

    @Test
    @DisplayName("Devolução e aceitação da validação do mapa com verificação do histórico")
    void devolucaoEAceitacaoComVerificacaoHistorico() throws Exception {
        // Devolução do mapa (GESTOR of unit 6 devolves to subordinate unit 9)
        JustificativaRequest devolverReq = new JustificativaRequest("Justificativa da devolução");
        mockMvc.perform(post("/api/subprocessos/{codigo}/devolver-validacao", subprocesso.getCodigo())
                        .with(user(usuarioGestor))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(devolverReq)))
                .andExpect(status().isOk());

        // Verificação do histórico após devolução
        String responseDevolucao =
                mockMvc.perform(get("/api/subprocessos/{codigo}/historico-validacao", subprocesso.getCodigo())
                                .with(user(usuarioGestor))
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        List<AnaliseHistoricoDto> historicoDevolucao =
                objectMapper.readValue(responseDevolucao, new TypeReference<>() {
                });

        assertThat(historicoDevolucao).hasSize(1);
        assertThat(historicoDevolucao.getFirst().acao())
                .isEqualTo(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO);
        assertThat(historicoDevolucao.getFirst().unidadeSigla()).isNotNull();
        assertThat(historicoDevolucao.getFirst().observacoes())
                .isEqualTo("Justificativa da devolução");

        // Adicionar verificação de Movimentacao e Alerta após devolução
        List<Movimentacao> movimentacoesDevolucao =
                movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                        subprocesso.getCodigo());
        assertThat(movimentacoesDevolucao).hasSize(2); // Setup + Devolução
        assertThat(movimentacoesDevolucao.getFirst().getDescricao())
                .isEqualTo("Devolução da validação do mapa de competências para ajustes");
        assertThat(movimentacoesDevolucao.getFirst().getUnidadeOrigem().getSigla())
                .isEqualTo(unidadeSuperior.getSigla());
        assertThat(movimentacoesDevolucao.getFirst().getUnidadeDestino().getSigla())
                .isEqualTo(subprocesso.getUnidade().getSigla());

        List<Alerta> alertasDevolucao =
                alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo());
        // No setUp não gera alerta manual, mas a devolução gera. 
        // Se houver algum alerta de início de processo, considerar. 
        // No BaseIntegrationTest o BD é limpo? Sim, @Transactional.
        assertThat(alertasDevolucao).isNotEmpty();
        assertThat(alertasDevolucao.getFirst().getDescricao())
                .contains(
                        "Validação do mapa da unidade " + unidade.getSigla() + " devolvida para"
                                + " ajustes");
        assertThat(alertasDevolucao.getFirst().getUnidadeDestino().getSigla())
                .isEqualTo(subprocesso.getUnidade().getSigla());

        // Unidade inferior valida o mapa novamente (CHEFE of unit 9)
        mockMvc.perform(post("/api/subprocessos/{codigo}/validar-mapa", subprocesso.getCodigo())
                        .with(user(usuarioChefe))
                        .with(csrf()))
                .andExpect(status().isOk());

        // GESTOR da unidade superior aceita a validação
        mockMvc.perform(post("/api/subprocessos/{codigo}/aceitar-validacao", subprocesso.getCodigo())
                        .with(user(usuarioGestor))
                        .with(csrf()))
                .andExpect(status().isOk());

        // Verificação do histórico após aceite
        String responseAceite =
                mockMvc.perform(get("/api/subprocessos/{codigo}/historico-validacao", subprocesso.getCodigo())
                                .with(user(usuarioGestor))
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        List<AnaliseHistoricoDto> historicoAceite =
                objectMapper.readValue(responseAceite, new TypeReference<>() {
                });

        assertThat(historicoAceite).hasSize(2);
        assertThat(historicoAceite.getFirst().acao())
                .isEqualTo(TipoAcaoAnalise.ACEITE_MAPEAMENTO);
        assertThat(historicoAceite.getFirst().unidadeSigla()).isNotNull();

        // Adicionar verificação de Movimentacao e Alerta após aceite
        List<Movimentacao> movimentacoesAceite =
                movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                        subprocesso.getCodigo());
        assertThat(movimentacoesAceite).hasSize(4); // Setup + devolução + validação + aceite
        assertThat(movimentacoesAceite.getFirst().getDescricao())
                .isEqualTo("Mapa de competências validado");
        assertThat(movimentacoesAceite.getFirst().getUnidadeOrigem().getSigla())
                .isEqualTo(unidadeSuperior.getSigla());
        assertThat(movimentacoesAceite.getFirst().getUnidadeDestino().getSigla())
                .isEqualTo(unidadeSuperiorSuperior.getSigla());

        List<Alerta> alertasAceite =
                alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo());
        assertThat(alertasAceite).isNotEmpty(); // devolução + validação + aceite (pode variar se houver de processo)

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
    @DisplayName("Aceite de mapa com sugestões deve manter a situação e subir a localização")
    void aceiteComSugestoesMantemSituacaoEEncaminha() throws Exception {
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);
        subprocessoRepo.saveAndFlush(subprocesso);

        mockMvc.perform(post("/api/subprocessos/{codigo}/aceitar-validacao", subprocesso.getCodigo())
                        .with(user(usuarioGestor))
                        .with(csrf()))
                .andExpect(status().isOk());

        Subprocesso atualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);
        assertThat(atualizado.getLocalizacaoAtual().getSigla()).isEqualTo(unidadeSuperiorSuperior.getSigla());

        List<Movimentacao> movimentacoes =
                movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
        assertThat(movimentacoes.getFirst().getDescricao()).isEqualTo("Mapa de competências validado");
        assertThat(movimentacoes.getFirst().getUnidadeOrigem().getSigla()).isEqualTo(unidadeSuperior.getSigla());
        assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidadeSuperiorSuperior.getSigla());

        Alerta alerta =
                alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo()).stream()
                        .filter(a -> a.getUnidadeDestino().getSigla().equals(unidadeSuperiorSuperior.getSigla()))
                        .findFirst()
                        .orElseThrow();
        assertThat(alerta.getDescricao())
                .contains("Validação do mapa da unidade " + unidade.getSigla() + " submetida para análise");
    }

    @Test
    @DisplayName(
            "ADMIN deve homologar validação do mapa, alterando status para MAPA_HOMOLOGADO e"
                    + " registrando movimentação e alerta")
    @WithMockAdmin
    void testHomologarValidacao_Sucesso() throws Exception {
        // Cenário: Subprocesso já validado e localizado na unidade (9)
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        subprocessoRepo.save(subprocesso);
        subprocessoRepo.flush();

        // Garante que o subprocesso esteja na unidade do ADMIN (1)
        Unidade adminUnit = unidadeRepo.findById(1L).orElseThrow();
        Movimentacao movAdmin = Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidadeSuperior)
                .unidadeDestino(adminUnit)
                .descricao("Enviado para Admin para Homologação")
                .dataHora(LocalDateTime.now())
                .build();
        movimentacaoRepo.save(movAdmin);

        // Ação
        mockMvc.perform(
                        post("/api/subprocessos/{codigo}/homologar-validacao", subprocesso.getCodigo())
                                .with(csrf()))
                .andExpect(status().isOk());

        // Verificações
        Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(spAtualizado.getSituacao())
                .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);

        List<Movimentacao> movimentacoes =
                movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                        subprocesso.getCodigo());
        // Deve ter o setup + homologação
        assertThat(movimentacoes).hasSizeGreaterThanOrEqualTo(2);
        assertThat(movimentacoes.getFirst().getDescricao())
                .isEqualTo("Mapa de competências homologado");
        assertThat(movimentacoes.getFirst().getUnidadeOrigem().getSigla()).isEqualTo("ADMIN");
        assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla()).isEqualTo("ADMIN");

        // Homologação não gera alerta (por design)
        List<Alerta> alertas =
                alertaRepo.findByProcessoCodigo(subprocesso.getProcesso().getCodigo());
        assertThat(alertas).isEmpty(); // MAPA_HOMOLOGADO não gera alerta
    }

    @Test
    @DisplayName("GESTOR deve ter podeVerSugestoes=true quando situação é MAPA_COM_SUGESTOES e está na unidade correta")
    void testPermissaoVerSugestoes_GestorComMapaComSugestoes() throws Exception {
        // Configura subprocesso com sugestões apresentadas (localizado na unidade superior - 6)
        Mapa mapa = MapaFixture.mapaPadrao(subprocesso);
        mapa.setCodigo(null);
        mapa.setSugestoes("Sugestão de ajuste no mapa");
        mapa = mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);
        subprocessoRepo.saveAndFlush(subprocesso);

        // Verifica permissões: GESTOR da unidade superior deve ter podeVerSugestoes=true
        String response = mockMvc.perform(
                        get("/api/subprocessos/{codigo}", subprocesso.getCodigo())
                                .with(user(usuarioGestor)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        assertThat(json.at("/permissoes/podeVerSugestoes").asBoolean()).isTrue();
        assertThat(json.at("/permissoes/podeDevolverMapa").asBoolean()).isTrue();
        assertThat(json.at("/permissoes/podeAceitarMapa").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("CHEFE não deve ter podeVerSugestoes quando situação é MAPA_COM_SUGESTOES")
    void testPermissaoVerSugestoes_ChefeNaoTemAcesso() throws Exception {
        // Configura subprocesso com sugestões apresentadas
        Mapa mapa = MapaFixture.mapaPadrao(subprocesso);
        mapa.setCodigo(null);
        mapa.setSugestoes("Sugestão de ajuste no mapa");
        mapa = mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);
        subprocessoRepo.saveAndFlush(subprocesso);

        // Verifica permissões: CHEFE não deve ter podeVerSugestoes
        String response = mockMvc.perform(
                        get("/api/subprocessos/{codigo}", subprocesso.getCodigo())
                                .with(user(usuarioChefe)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        assertThat(json.at("/permissoes/podeVerSugestoes").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("GESTOR deve obter sugestões registradas no endpoint dedicado")
    void obterSugestoes_deveRetornarTextoRegistrado() throws Exception {
        Mapa mapa = MapaFixture.mapaPadrao(subprocesso);
        mapa.setCodigo(null);
        mapa.setSugestoes("Sugestão de ajuste no mapa");
        mapa = mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);
        subprocessoRepo.saveAndFlush(subprocesso);

        mockMvc.perform(
                        get("/api/subprocessos/{codigo}/sugestoes", subprocesso.getCodigo())
                                .with(user(usuarioGestor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sugestoes").value("Sugestão de ajuste no mapa"));
    }
}
