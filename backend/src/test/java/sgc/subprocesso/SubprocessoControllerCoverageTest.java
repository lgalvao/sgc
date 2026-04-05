package sgc.subprocesso;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.comum.ComumDtos.*;
import sgc.comum.erros.*;
import sgc.mapa.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubprocessoController.class)
@Import(RestExceptionHandler.class)
@DisplayName("SubprocessoController - Cobertura")
class SubprocessoControllerCoverageTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    @MockitoBean
    private SubprocessoService subprocessoService;
    @MockitoBean private SubprocessoConsultaService consultaService;
    @MockitoBean
    private SubprocessoTransicaoService transicaoService;
    @MockitoBean
    private UnidadeService unidadeService;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("listar - deve retornar lista e 200")
    @WithMockUser(roles = "ADMIN")
    void listar() throws Exception {
        Processo processo = new Processo();
        processo.setDescricao("Processo teste");

        Unidade unidade = new Unidade();
        unidade.setCodigo(2L);
        unidade.setSigla("UND");
        unidade.setNome("Unidade teste");
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setProcesso(processo);
        sp.setUnidade(unidade);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        when(consultaService.listarTodos()).thenReturn(List.of(sp));

        mockMvc.perform(get("/api/subprocessos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(1L));

        verify(consultaService).listarTodos();
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("obterStatus - deve retornar status e 200")
    @WithMockUser
    void obterStatus() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(consultaService.obterStatus(1L)).thenReturn(SubprocessoSituacaoDto.builder().situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO).build());

        mockMvc.perform(get("/api/subprocessos/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("MAPEAMENTO_MAPA_CRIADO"));

        verify(consultaService).obterStatus(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("buscarPorProcessoEUnidade - deve retornar subprocesso")
    @WithMockUser
    void buscarPorProcessoEUnidade() throws Exception {
        Unidade un = new Unidade(); un.setCodigo(2L);
        when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(un);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        when(consultaService.obterEntidadePorProcessoEUnidade(1L, 2L)).thenReturn(sp);
        when(permissionEvaluator.hasPermission(any(), any(Subprocesso.class), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);

        mockMvc.perform(get("/api/subprocessos/buscar")
                        .param("codProcesso", "1")
                        .param("siglaUnidade", "SIGLA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1L));

        verify(unidadeService).buscarPorSigla("SIGLA");
        verify(consultaService).obterEntidadePorProcessoEUnidade(1L, 2L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("criar - deve chamar servico e retornar 201")
    @WithMockUser(roles = "ADMIN")
    void criar() throws Exception {
        String req = criarJsonSubprocesso(LocalDateTime.now(), LocalDateTime.now());
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        Processo processo = new Processo();
        processo.setDescricao("Processo criado");
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("UND");
        unidade.setNome("Unidade teste");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        sp.setProcesso(processo);
        sp.setUnidade(unidade);

        when(subprocessoService.criarEntidade(any())).thenReturn(sp);
        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);

        mockMvc.perform(post("/api/subprocessos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/subprocessos/100"))
                .andExpect(jsonPath("$.codigo").value(100L));

        verify(subprocessoService).criarEntidade(any());
        verify(consultaService).buscarSubprocesso(100L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("atualizar - deve chamar servico e retornar 200")
    @WithMockUser(roles = "ADMIN")
    void atualizar() throws Exception {
        AtualizarSubprocessoRequest req = AtualizarSubprocessoRequest.builder()
                .codUnidade(10L)
                .codMapa(20L)
                .build();
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("UND");
        unidade.setNome("Unidade teste");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        Processo processo = new Processo();
        processo.setDescricao("Processo atualizado");
        Subprocesso atualizado = new Subprocesso();
        atualizado.setCodigo(1L);
        atualizado.setUnidade(unidade);
        atualizado.setProcesso(processo);

        when(subprocessoService.atualizarEntidade(eq(1L), any())).thenReturn(atualizado);
        when(consultaService.buscarSubprocesso(1L)).thenReturn(atualizado);

        mockMvc.perform(post("/api/subprocessos/1/atualizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1L));

        verify(subprocessoService).atualizarEntidade(1L, req.paraCommand());
        verify(consultaService).buscarSubprocesso(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("excluir - deve chamar servico e retornar 204")
    @WithMockUser(roles = "ADMIN")
    void excluir() throws Exception {
        mockMvc.perform(post("/api/subprocessos/1/excluir")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(subprocessoService).excluir(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("alterarDataLimite - deve chamar servico e retornar 200")
    @WithMockUser(roles = "ADMIN")
    void alterarDataLimite() throws Exception {
        DataRequest req = new DataRequest(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/subprocessos/1/data-limite")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(transicaoService).alterarDataLimite(eq(1L), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("obterHistoricoCadastro - deve retornar lista e 200")
    @WithMockUser
    void obterHistoricoCadastro() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(consultaService.listarHistoricoCadastro(1L)).thenReturn(List.of(
            new AnaliseHistoricoDto(sgc.subprocesso.model.TipoAnalise.CADASTRO, sgc.subprocesso.model.TipoAcaoAnalise.ACEITE_MAPEAMENTO, "obs", "nome", "justificativa", LocalDateTime.now(), "sigla", "unidade")
        ));

        mockMvc.perform(get("/api/subprocessos/1/historico-cadastro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(consultaService).listarHistoricoCadastro(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("obterContextoEdicao - deve retornar contexto e 200")
    @WithMockUser
    void obterContextoEdicao() throws Exception {
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("UND");
        unidade.setNome("Unidade teste");
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Processo processo = new Processo();
        processo.setDescricao("Processo teste");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        subprocesso.setUnidade(unidade);
        subprocesso.setProcesso(processo);

        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(consultaService.obterContextoEdicao(1L)).thenReturn(
            new ContextoEdicaoResponse(
                    unidade,
                    SubprocessoResumoDto.fromEntity(subprocesso),
                    SubprocessoDetalheResponse.builder()
                            .subprocesso(SubprocessoResumoDto.fromEntity(subprocesso))
                            .responsavel(sgc.organizacao.dto.ResponsavelDto.builder().tipo("RESPONSAVEL").build())
                            .movimentacoes(List.of())
                            .localizacaoAtual("UND")
                            .permissoes(PermissoesSubprocessoDto.builder().build())
                            .build(),
                    new MapaCompletoDto(10L, 1L, null, List.of(), null),
                    List.of())
        );

        mockMvc.perform(get("/api/subprocessos/1/contexto-edicao"))
                .andExpect(status().isOk());

        verify(consultaService).obterContextoEdicao(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("reabrirCadastro - deve chamar servico e retornar 200")
    @WithMockUser(roles = "ADMIN")
    void reabrirCadastro() throws Exception {
        JustificativaRequest req = new JustificativaRequest("Justificativa");

        mockMvc.perform(post("/api/subprocessos/1/reabrir-cadastro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(transicaoService).reabrirCadastro(1L, "Justificativa");
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("reabrirRevisaoCadastro - deve chamar servico e retornar 200")
    @WithMockUser(roles = "ADMIN")
    void reabrirRevisaoCadastro() throws Exception {
        JustificativaRequest req = new JustificativaRequest("Justificativa");

        mockMvc.perform(post("/api/subprocessos/1/reabrir-revisao-cadastro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(transicaoService).reabrirRevisaoCadastro(1L, "Justificativa");
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("validarCadastro - deve chamar servico e retornar 200")
    @WithMockUser
    void validarCadastro() throws Exception {
        when(consultaService.validarCadastro(1L)).thenReturn(new ValidacaoCadastroDto(true, List.of()));

        mockMvc.perform(get("/api/subprocessos/1/validar-cadastro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.erros").isArray())
                .andExpect(jsonPath("$.erros").isEmpty());

        verify(consultaService).validarCadastro(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("importarAtividades - deve aplicar payload e retornar mensagem de sucesso")
    @WithMockUser
    void importarAtividades() throws Exception {
        ImportarAtividadesRequest req = new ImportarAtividadesRequest(2L, List.of(30L, 31L));

        mockMvc.perform(post("/api/subprocessos/1/importar-atividades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Atividades importadas."));

        verify(subprocessoService).importarAtividades(1L, 2L, List.of(30L, 31L));
        verifyNoMoreInteractions(subprocessoService);
        verifyNoMoreInteractions(transicaoService, unidadeService);
    }



    @Test
    @DisplayName("importarAtividades - deve retornar aviso quando houver atividades duplicadas")
    @WithMockUser
    void importarAtividadesComDuplicatas() throws Exception {
        ImportarAtividadesRequest req = new ImportarAtividadesRequest(2L, List.of(30L, 31L));
        when(subprocessoService.importarAtividades(1L, 2L, List.of(30L, 31L))).thenReturn(true);

        mockMvc.perform(post("/api/subprocessos/1/importar-atividades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Atividades importadas."))
                .andExpect(jsonPath("$.aviso").isNotEmpty());

        verify(subprocessoService).importarAtividades(1L, 2L, List.of(30L, 31L));
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("aceitarCadastroEmBloco - deve chamar servico e retornar 200")
    @WithMockUser
    void aceitarCadastroEmBloco() throws Exception {
        String req = criarJsonProcessamentoEmBloco("ACEITAR", List.of(1L, 2L));

        mockMvc.perform(post("/api/subprocessos/1/aceitar-cadastro-bloco")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isOk());

        verify(transicaoService).aceitarCadastroEmBloco(anyList(), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("salvarMapa - deve chamar servico e retornar 200")
    @WithMockUser
    void salvarMapa() throws Exception {
        SalvarMapaRequest req = new SalvarMapaRequest("Obs", List.of());
        when(consultaService.mapaCompletoDtoPorSubprocesso(1L))
                .thenReturn(new MapaCompletoDto(1L, 1L, "Obs", List.of(), null));

        mockMvc.perform(post("/api/subprocessos/1/mapa")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1L))
                .andExpect(jsonPath("$.subprocessoCodigo").value(1L))
                .andExpect(jsonPath("$.observacoes").value("Obs"));

        verify(subprocessoService).salvarMapa(1L, req);
        verify(consultaService).mapaCompletoDtoPorSubprocesso(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("adicionarCompetencia - deve chamar servico e retornar 200")
    @WithMockUser
    void adicionarCompetencia() throws Exception {
        CompetenciaRequest req = new CompetenciaRequest("Comp", List.of(1L));
        when(consultaService.mapaCompletoDtoPorSubprocesso(1L))
                .thenReturn(new MapaCompletoDto(1L, 1L, null, List.of(), null));

        mockMvc.perform(post("/api/subprocessos/1/competencia")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).adicionarCompetencia(1L, req);
        verify(consultaService).mapaCompletoDtoPorSubprocesso(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("atualizarCompetencia - deve chamar servico e retornar 200")
    @WithMockUser
    void atualizarCompetencia() throws Exception {
        CompetenciaRequest req = new CompetenciaRequest("Comp", List.of(1L));
        when(consultaService.mapaCompletoDtoPorSubprocesso(1L))
                .thenReturn(new MapaCompletoDto(1L, 1L, null, List.of(), null));

        mockMvc.perform(post("/api/subprocessos/1/competencia/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).atualizarCompetencia(1L, 10L, req);
        verify(consultaService).mapaCompletoDtoPorSubprocesso(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("removerCompetencia - deve chamar servico e retornar 200")
    @WithMockUser
    void removerCompetencia() throws Exception {
        when(consultaService.mapaCompletoDtoPorSubprocesso(1L))
                .thenReturn(new MapaCompletoDto(1L, 1L, null, List.of(), null));

        mockMvc.perform(post("/api/subprocessos/1/competencia/10/remover")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(subprocessoService).removerCompetencia(1L, 10L);
        verify(consultaService).mapaCompletoDtoPorSubprocesso(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("disponibilizarMapa - deve chamar servico e retornar 200")
    @WithMockUser
    void disponibilizarMapa() throws Exception {
        DisponibilizarMapaRequest req = new DisponibilizarMapaRequest(java.time.LocalDate.now().plusDays(10), "Obs");

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar-mapa")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(transicaoService).disponibilizarMapa(eq(1L), any(), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("salvarMapaCompleto - deve chamar servico e retornar 200")
    @WithMockUser
    void salvarMapaCompleto() throws Exception {
        SalvarMapaRequest req = new SalvarMapaRequest("Obs", List.of());
        when(consultaService.mapaCompletoDtoPorSubprocesso(1L))
                .thenReturn(new MapaCompletoDto(1L, 1L, "Obs", List.of(), null));

        mockMvc.perform(post("/api/subprocessos/1/mapa-completo")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).salvarMapa(1L, req);
        verify(consultaService).mapaCompletoDtoPorSubprocesso(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("validarMapa - deve chamar servico e retornar 200")
    @WithMockUser
    void validarMapa() throws Exception {
        mockMvc.perform(post("/api/subprocessos/1/validar-mapa")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(transicaoService).validarMapa(eq(1L), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("devolverValidacao - deve chamar servico e retornar 200")
    @WithMockUser
    void devolverValidacao() throws Exception {
        JustificativaRequest req = new JustificativaRequest("Just");

        mockMvc.perform(post("/api/subprocessos/1/devolver-validacao")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(transicaoService).devolverValidacao(eq(1L), any(), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("aceitarValidacao - deve chamar servico e retornar 200")
    @WithMockUser
    void aceitarValidacao() throws Exception {
        TextoOpcionalRequest req = new TextoOpcionalRequest("Obs");

        mockMvc.perform(post("/api/subprocessos/1/aceitar-validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(transicaoService).aceitarValidacao(eq(1L), eq("Obs"), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("homologarValidacao - deve chamar servico e retornar 200")
    @WithMockUser
    void homologarValidacao() throws Exception {
        TextoOpcionalRequest req = new TextoOpcionalRequest("Obs");

        mockMvc.perform(post("/api/subprocessos/1/homologar-validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(transicaoService).homologarValidacao(eq(1L), eq("Obs"), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("submeterMapaAjustado - deve chamar servico e retornar 200")
    @WithMockUser
    void submeterMapaAjustado() throws Exception {
        SubmeterMapaAjustadoRequest req = new SubmeterMapaAjustadoRequest("Just", null, List.of());

        mockMvc.perform(post("/api/subprocessos/1/submeter-mapa-ajustado")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(transicaoService).submeterMapaAjustado(eq(1L), any(), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("aceitarValidacaoEmBloco - deve chamar servico e retornar 200")
    @WithMockUser
    void aceitarValidacaoEmBloco() throws Exception {
        String req = criarJsonProcessamentoEmBloco("ACEITAR", List.of(1L));

        mockMvc.perform(post("/api/subprocessos/1/aceitar-validacao-bloco")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isOk());

        verify(transicaoService).aceitarValidacaoEmBloco(anyList(), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("homologarValidacaoEmBloco - deve chamar servico e retornar 200")
    @WithMockUser
    void homologarValidacaoEmBloco() throws Exception {
        String req = criarJsonProcessamentoEmBloco("HOMOLOGAR", List.of(1L));

        mockMvc.perform(post("/api/subprocessos/1/homologar-validacao-bloco")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isOk());

        verify(transicaoService).homologarValidacaoEmBloco(anyList(), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("listarAtividadesParaImportacao - deve retornar lista e 200")
    @WithMockUser
    void listarAtividadesParaImportacao() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("CONSULTAR_PARA_IMPORTACAO"))).thenReturn(true);
        when(consultaService.listarAtividadesParaImportacao(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/subprocessos/1/atividades-importacao"))
                .andExpect(status().isOk());

        verify(consultaService).listarAtividadesParaImportacao(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("obterMapa - deve retornar mapa e 200")
    @WithMockUser
    void obterMapa() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(consultaService.mapaCompletoDtoPorSubprocesso(1L))
                .thenReturn(new MapaCompletoDto(1L, 1L, null, List.of(), null));

        mockMvc.perform(get("/api/subprocessos/1/mapa"))
                .andExpect(status().isOk());

        verify(consultaService).mapaCompletoDtoPorSubprocesso(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("obterMapaCompleto - deve retornar mapa e 200")
    @WithMockUser
    void obterMapaCompleto() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(consultaService.mapaCompletoDtoPorSubprocesso(1L))
                .thenReturn(new MapaCompletoDto(1L, 1L, null, List.of(), null));

        mockMvc.perform(get("/api/subprocessos/1/mapa-completo"))
                .andExpect(status().isOk());

        verify(consultaService).mapaCompletoDtoPorSubprocesso(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("validarCadastro - deve retornar inconsistências reais quando cadastro estiver inválido")
    @WithMockUser
    void validarCadastroInvalido() throws Exception {
        List<ValidacaoCadastroDto.Erro> erros = List.of(
                new ValidacaoCadastroDto.Erro("SEM_MAPA", null, null, "Subprocesso sem mapa"),
                new ValidacaoCadastroDto.Erro("SEM_ATIVIDADES", null, null, "Mapa sem atividades"));
        when(consultaService.validarCadastro(1L)).thenReturn(new ValidacaoCadastroDto(false, erros));

        mockMvc.perform(get("/api/subprocessos/1/validar-cadastro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.erros").isArray())
                .andExpect(jsonPath("$.erros[0].tipo").value("SEM_MAPA"))
                .andExpect(jsonPath("$.erros[1].tipo").value("SEM_ATIVIDADES"));

        verify(consultaService).validarCadastro(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("validarCadastro - deve retornar 500 quando serviço lançar erro inesperado")
    @WithMockUser
    void validarCadastroErroInterno() throws Exception {
        when(consultaService.validarCadastro(1L)).thenThrow(new RuntimeException("falha inesperada"));

        mockMvc.perform(get("/api/subprocessos/1/validar-cadastro"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ERRO INESPERADO")));

        verify(consultaService).validarCadastro(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("importarAtividades - deve retornar 400 quando código de origem não for informado")
    @WithMockUser
    void importarAtividadesSemOrigem() throws Exception {
        String jsonInvalido = """
                {
                  "codigosAtividades": [30, 31]
                }
                """;

        mockMvc.perform(post("/api/subprocessos/1/importar-atividades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").exists());

        verify(subprocessoService, never()).importarAtividades(anyLong(), anyLong(), anyList());
    }

    @Test
    @DisplayName("importarAtividades - deve propagar erro de validação de negócio")
    @WithMockUser
    void importarAtividadesErroValidacao() throws Exception {
        ImportarAtividadesRequest req = new ImportarAtividadesRequest(2L, List.of(30L));
        doThrow(new ErroValidacao("Não é possível importar atividades do mesmo subprocesso."))
                .when(subprocessoService).importarAtividades(1L, 2L, List.of(30L));

        mockMvc.perform(post("/api/subprocessos/1/importar-atividades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Não é possível importar atividades do mesmo subprocesso."));

        verify(subprocessoService).importarAtividades(1L, 2L, List.of(30L));
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("criar - deve retornar 400 quando payload estiver inválido")
    @WithMockUser(roles = "ADMIN")
    void criarPayloadInvalido() throws Exception {
        String jsonInvalido = """
                {
                  "codProcesso": 1,
                  "codUnidade": 10,
                  "dataLimiteEtapa1": null
                }
                """;

        mockMvc.perform(post("/api/subprocessos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").exists());

        verify(subprocessoService, never()).criarEntidade(any());
    }

    @Test
    @DisplayName("alterarDataLimite - deve retornar 400 quando data estiver no passado")
    @WithMockUser(roles = "ADMIN")
    void alterarDataLimiteInvalida() throws Exception {
        DataRequest req = new DataRequest(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/subprocessos/1/data-limite")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").exists());

        verify(transicaoService, never()).alterarDataLimite(anyLong(), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("devolverValidacao - deve retornar 400 quando justificativa estiver em branco")
    @WithMockUser
    void devolverValidacaoSemJustificativa() throws Exception {
        JustificativaRequest req = new JustificativaRequest("   ");

        mockMvc.perform(post("/api/subprocessos/1/devolver-validacao")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").exists());

        verify(transicaoService, never()).devolverValidacao(anyLong(), any(), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("aceitarCadastroEmBloco - deve retornar 400 quando lista de subprocessos estiver vazia")
    @WithMockUser
    void aceitarCadastroEmBlocoSemSubprocessos() throws Exception {
        String req = criarJsonProcessamentoEmBloco("ACEITAR", List.of());

        mockMvc.perform(post("/api/subprocessos/1/aceitar-cadastro-bloco")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isBadRequest());

        verify(transicaoService, never()).aceitarCadastroEmBloco(anyList(), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("disponibilizarMapa - deve encaminhar request completo ao serviço")
    @WithMockUser
    void disponibilizarMapaEncaminhaRequest() throws Exception {
        DisponibilizarMapaRequest req = new DisponibilizarMapaRequest(java.time.LocalDate.now().plusDays(15), "Observação de teste");

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar-mapa")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Mapa de competências disponibilizado."));

        verify(transicaoService).disponibilizarMapa(eq(1L), eq(req), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("salvarMapa - deve retornar 500 quando não conseguir montar resposta")
    @WithMockUser
    void salvarMapaErroAoGerarResposta() throws Exception {
        SalvarMapaRequest req = new SalvarMapaRequest("Obs", List.of());
        when(consultaService.mapaCompletoDtoPorSubprocesso(1L)).thenThrow(new RuntimeException("falha ao montar dto"));

        mockMvc.perform(post("/api/subprocessos/1/mapa")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());

        verify(subprocessoService).salvarMapa(1L, req);
        verify(consultaService).mapaCompletoDtoPorSubprocesso(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("obterHistoricoCadastro - deve retornar lista preenchida com campos de rastreabilidade")
    @WithMockUser
    void obterHistoricoCadastroComDados() throws Exception {
        AnaliseHistoricoDto historico = new AnaliseHistoricoDto(
                sgc.subprocesso.model.TipoAnalise.CADASTRO,
                sgc.subprocesso.model.TipoAcaoAnalise.ACEITE_MAPEAMENTO,
                "123456789012",
                "SESEL",
                "Secretaria",
                LocalDateTime.now(),
                "RN-001",
                "Observação técnica");
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(consultaService.listarHistoricoCadastro(1L)).thenReturn(List.of(historico));

        mockMvc.perform(get("/api/subprocessos/1/historico-cadastro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].motivo").value("RN-001"))
                .andExpect(jsonPath("$[0].unidadeSigla").value("SESEL"));

        verify(consultaService).listarHistoricoCadastro(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("listarAtividadesParaImportacao - deve retornar 500 quando serviço falhar")
    @WithMockUser
    void listarAtividadesParaImportacaoComErro() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("CONSULTAR_PARA_IMPORTACAO"))).thenReturn(true);
        when(consultaService.listarAtividadesParaImportacao(1L)).thenThrow(new RuntimeException("falha ao listar"));

        mockMvc.perform(get("/api/subprocessos/1/atividades-importacao"))
                .andExpect(status().isInternalServerError());

        verify(consultaService).listarAtividadesParaImportacao(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("obterMapa - deve retornar payload com código do subprocesso")
    @WithMockUser
    void obterMapaComPayload() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(consultaService.mapaCompletoDtoPorSubprocesso(1L))
                .thenReturn(new MapaCompletoDto(15L, 1L, "Mapa criado", List.of(), null));

        mockMvc.perform(get("/api/subprocessos/1/mapa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(15L))
                .andExpect(jsonPath("$.subprocessoCodigo").value(1L));

        verify(consultaService).mapaCompletoDtoPorSubprocesso(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("obterMapaCompleto - deve retornar payload com observações do mapa")
    @WithMockUser
    void obterMapaCompletoComObservacoes() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(consultaService.mapaCompletoDtoPorSubprocesso(1L))
                .thenReturn(new MapaCompletoDto(15L, 1L, "Observação detalhada", List.of(), null));

        mockMvc.perform(get("/api/subprocessos/1/mapa-completo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.observacoes").value("Observação detalhada"));

        verify(consultaService).mapaCompletoDtoPorSubprocesso(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("aceitarValidacao - deve permitir texto opcional nulo")
    @WithMockUser
    void aceitarValidacaoSemTexto() throws Exception {
        TextoOpcionalRequest req = new TextoOpcionalRequest(null);

        mockMvc.perform(post("/api/subprocessos/1/aceitar-validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(transicaoService).aceitarValidacao(eq(1L), isNull(), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    private String criarJsonSubprocesso(LocalDateTime dataLimiteEtapa1, LocalDateTime dataLimiteEtapa2) throws Exception {
        var json = objectMapper.createObjectNode();
        json.put("codProcesso", 1L);
        json.put("codUnidade", 10L);
        json.put("dataLimiteEtapa1", dataLimiteEtapa1.toString());
        json.put("dataLimiteEtapa2", dataLimiteEtapa2.toString());
        return objectMapper.writeValueAsString(json);
    }

    private String criarJsonProcessamentoEmBloco(String acao, List<Long> subprocessos) throws Exception {
        var json = objectMapper.createObjectNode();
        json.put("acao", acao);
        json.putPOJO("subprocessos", subprocessos);
        return objectMapper.writeValueAsString(json);
    }

}
