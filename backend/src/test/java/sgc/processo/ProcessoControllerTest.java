package sgc.processo;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.RestExceptionHandler;
import sgc.processo.dto.AtualizarProcessoReq;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.dto.IniciarProcessoReq;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProcessoController.class)
@Import(RestExceptionHandler.class)
public class ProcessoControllerTest {
    private static final String NOVO_PROCESSO = "Novo Processo";
    private static final String API_PROCESSOS = "/api/processos";
    private static final String API_PROCESSOS_1 = "/api/processos/1";
    private static final String API_PROCESSOS_999 = "/api/processos/999";
    private static final String CODIGO_JSON_PATH = "$.codigo";
    private static final String DESCRICAO_JSON_PATH = "$.descricao";
    private static final String PROCESSO_ATUALIZADO = "Processo Atualizado";
    private static final String PROCESSO_NAO_ENCONTRADO = "Processo não encontrado";

    @MockitoBean
    private ProcessoService processoService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @org.mockito.Captor
    private org.mockito.ArgumentCaptor<CriarProcessoReq> criarCaptor;

    @org.mockito.Captor
    private org.mockito.ArgumentCaptor<AtualizarProcessoReq> atualizarCaptor;

    @BeforeEach
    void setUp() {
        org.mockito.MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser
    void criar_ProcessoValido_RetornaCreatedComUri() throws Exception {
        var req = new CriarProcessoReq(NOVO_PROCESSO, TipoProcesso.MAPEAMENTO, LocalDateTime.now().plusDays(30),
                List.of(1L));
        var dto = ProcessoDto.builder()
                .codigo(1L)
                .dataCriacao(LocalDateTime.now())
                .descricao(NOVO_PROCESSO)
                .situacao(SituacaoProcesso.CRIADO)
                .tipo(TipoProcesso.MAPEAMENTO.name())
                .build();

        when(processoService.criar(any(CriarProcessoReq.class))).thenReturn(dto);

        mockMvc.perform(post(API_PROCESSOS)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", API_PROCESSOS_1))
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L))
                .andExpect(jsonPath(DESCRICAO_JSON_PATH).value(NOVO_PROCESSO));

        verify(processoService).criar(criarCaptor.capture());
        CriarProcessoReq capturado = criarCaptor.getValue();
        assertEquals(NOVO_PROCESSO, capturado.getDescricao());
    }

    @Test
    @WithMockUser
    void criar_ProcessoInvalido_RetornaBadRequest() throws Exception {
        var req = new CriarProcessoReq("", TipoProcesso.MAPEAMENTO, LocalDateTime.now().plusDays(30),
                List.of(1L));

        mockMvc.perform(post(API_PROCESSOS)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void obterPorId_ProcessoExiste_RetornaOk() throws Exception {
        var dto = ProcessoDto.builder()
                .codigo(1L)
                .dataCriacao(LocalDateTime.now())
                .descricao("Processo Teste")
                .situacao(SituacaoProcesso.CRIADO)
                .tipo(TipoProcesso.MAPEAMENTO.name())
                .build();

        when(processoService.obterPorId(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get(API_PROCESSOS_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L))
                .andExpect(jsonPath(DESCRICAO_JSON_PATH).value("Processo Teste"));

        verify(processoService).obterPorId(1L);
    }

    @Test
    @WithMockUser
    void obterPorId_ProcessoNaoExiste_RetornaNotFound() throws Exception {
        when(processoService.obterPorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get(API_PROCESSOS_999))
                .andExpect(status().isNotFound());

        verify(processoService).obterPorId(999L);
    }

    @Test
    @WithMockUser
    void atualizar_ProcessoExiste_RetornaOk() throws Exception {
        var req = new AtualizarProcessoReq(1L, PROCESSO_ATUALIZADO, TipoProcesso.REVISAO,
                LocalDateTime.now().plusDays(45), List.of(1L));
        var dto = ProcessoDto.builder()
                .codigo(1L)
                .dataCriacao(LocalDateTime.now())
                .descricao(PROCESSO_ATUALIZADO)
                .situacao(SituacaoProcesso.CRIADO)
                .tipo(TipoProcesso.REVISAO.name())
                .build();

        when(processoService.atualizar(eq(1L), any(AtualizarProcessoReq.class))).thenReturn(dto);

        mockMvc.perform(post(API_PROCESSOS + "/1/atualizar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L))
                .andExpect(jsonPath(DESCRICAO_JSON_PATH).value(PROCESSO_ATUALIZADO));

        verify(processoService).atualizar(eq(1L), atualizarCaptor.capture());
        AtualizarProcessoReq capturado = atualizarCaptor.getValue();
        assertEquals(PROCESSO_ATUALIZADO, capturado.getDescricao());
    }

    @Test
    @WithMockUser
    void atualizar_ProcessoNaoEncontrado_RetornaNotFound() throws Exception {
        var req = new AtualizarProcessoReq(999L, "Teste", TipoProcesso.MAPEAMENTO, null, List.of(1L));

        doThrow(new ErroEntidadeNaoEncontrada(PROCESSO_NAO_ENCONTRADO)).when(processoService)
                .atualizar(eq(999L), any(AtualizarProcessoReq.class));

        mockMvc.perform(post(API_PROCESSOS + "/999/atualizar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void atualizar_ProcessoEstadoInvalido_RetornaBadRequest() throws Exception {
        var req = new AtualizarProcessoReq(1L, "Teste", TipoProcesso.MAPEAMENTO, null, List.of(1L));

        doThrow(new IllegalStateException()).when(processoService).atualizar(eq(1L),
                any(AtualizarProcessoReq.class));

        mockMvc.perform(post(API_PROCESSOS + "/1/atualizar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void excluir_ProcessoExiste_RetornaNoContent() throws Exception {
        mockMvc.perform(post(API_PROCESSOS + "/1/excluir")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(processoService).apagar(1L);
    }

    @Test
    @WithMockUser
    void excluir_ProcessoNaoEncontrado_RetornaNotFound() throws Exception {
        doThrow(new ErroEntidadeNaoEncontrada(PROCESSO_NAO_ENCONTRADO)).when(processoService).apagar(999L);

        mockMvc.perform(post(API_PROCESSOS + "/999/excluir")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void excluir_ProcessoEstadoInvalido_RetornaBadRequest() throws Exception {
        doThrow(new IllegalStateException()).when(processoService).apagar(eq(1L));

        mockMvc.perform(post(API_PROCESSOS + "/1/excluir")
                .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void obterDetalhes_ProcessoExiste_RetornaOk() throws Exception {
        var dto = ProcessoDetalheDto.builder()
                .codigo(1L)
                .descricao("Processo Detalhado")
                .tipo(TipoProcesso.MAPEAMENTO.name())
                .situacao(SituacaoProcesso.CRIADO)
                .dataCriacao(LocalDateTime.now())
                .build();

        when(processoService.obterDetalhes(eq(1L))).thenReturn(dto);

        mockMvc.perform(get("/api/processos/1/detalhes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L))
                .andExpect(jsonPath("$.descricao").value("Processo Detalhado"));

        verify(processoService).obterDetalhes(eq(1L));
    }

    @Test
    @WithMockUser
    void obterDetalhes_ProcessoNaoEncontrado_RetornaNotFound() throws Exception {
        doThrow(new ErroEntidadeNaoEncontrada(PROCESSO_NAO_ENCONTRADO)).when(processoService)
                .obterDetalhes(eq(999L));

        mockMvc.perform(get("/api/processos/999/detalhes"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void obterDetalhes_AcessoNegado_RetornaForbidden() throws Exception {
        doThrow(new ErroAccessoNegado("Acesso negado")).when(processoService).obterDetalhes(eq(1L));

        mockMvc.perform(get("/api/processos/1/detalhes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void iniciarProcessoMapeamento_Valido_RetornaOk() throws Exception {
        var req = new IniciarProcessoReq(TipoProcesso.MAPEAMENTO, List.of(1L));
        var processo = ProcessoDto.builder().codigo(1L).descricao("Processo Teste").build();

        when(processoService.obterPorId(1L)).thenReturn(Optional.of(processo));
        when(processoService.iniciarProcessoMapeamento(eq(1L), anyList())).thenReturn(List.of());

        mockMvc.perform(post("/api/processos/1/iniciar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1L))
                .andExpect(jsonPath("$.descricao").value("Processo Teste"));

        verify(processoService).iniciarProcessoMapeamento(eq(1L), eq(List.of(1L)));
    }

    @Test
    @WithMockUser
    void iniciarProcessoRevisao_Valido_RetornaOk() throws Exception {
        var req = new IniciarProcessoReq(TipoProcesso.REVISAO, List.of(1L));
        var processo = ProcessoDto.builder().codigo(1L).descricao("Processo Teste").build();

        when(processoService.obterPorId(1L)).thenReturn(Optional.of(processo));
        when(processoService.iniciarProcessoRevisao(eq(1L), anyList())).thenReturn(List.of());

        mockMvc.perform(post(API_PROCESSOS_1 + "/iniciar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1L))
                .andExpect(jsonPath("$.descricao").value("Processo Teste"));

        verify(processoService).iniciarProcessoRevisao(eq(1L), eq(List.of(1L)));
    }

    @Test
    @WithMockUser
    void iniciarProcesso_Invalido_RetornaBadRequest() throws Exception {
        var req = new IniciarProcessoReq(null, List.of(1L));

        mockMvc.perform(post("/api/processos/999/iniciar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void finalizar_ProcessoValido_RetornaOk() throws Exception {
        doNothing().when(processoService).finalizar(1L);

        mockMvc.perform(post("/api/processos/1/finalizar")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(processoService).finalizar(1L);
    }

    @Test
    @WithMockUser
    void finalizar_ProcessoNaoEncontrado_RetornaNotFound() throws Exception {
        doThrow(new ErroEntidadeNaoEncontrada(PROCESSO_NAO_ENCONTRADO)).when(processoService).finalizar(999L);

        mockMvc.perform(post(API_PROCESSOS_999 + "/finalizar")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void finalizar_ProcessoEstadoInvalido_RetornaBadRequest() throws Exception {
        doThrow(new IllegalStateException("Processo em estado inválido")).when(processoService).finalizar(1L);

        mockMvc.perform(post("/api/processos/1/finalizar")
                .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "A operação não pode ser executada no estado atual do recurso."));
    }

    @Test
    @WithMockUser
    void finalizar_ValidacaoFalhou_RetornaConflict() throws Exception {
        doThrow(new ErroProcesso("Subprocessos não homologados")).when(processoService).finalizar(1L);

        mockMvc.perform(post("/api/processos/1/finalizar")
                .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Subprocessos não homologados"));
    }

    @Test
    @WithMockUser
    void listarFinalizados_RetornaLista() throws Exception {
        when(processoService.listarFinalizados()).thenReturn(List.of(ProcessoDto.builder().codigo(1L).build()));

        mockMvc.perform(get("/api/processos/finalizados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].codigo").value(1L));
    }

    @Test
    @WithMockUser
    void listarAtivos_RetornaLista() throws Exception {
        when(processoService.listarAtivos()).thenReturn(List.of(ProcessoDto.builder().codigo(1L).build()));

        mockMvc.perform(get("/api/processos/ativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].codigo").value(1L));
    }

    @Test
    @WithMockUser
    void obterStatusUnidades_RetornaMap() throws Exception {
        when(processoService.listarUnidadesBloqueadasPorTipo("MAPEAMENTO")).thenReturn(List.of(100L));

        mockMvc.perform(get("/api/processos/status-unidades").param("tipo", "MAPEAMENTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidadesDesabilitadas").isArray())
                .andExpect(jsonPath("$.unidadesDesabilitadas[0]").value(100L));
    }

    @Test
    @WithMockUser
    void listarUnidadesBloqueadas_RetornaLista() throws Exception {
        when(processoService.listarUnidadesBloqueadasPorTipo("MAPEAMENTO")).thenReturn(List.of(100L));

        mockMvc.perform(get("/api/processos/unidades-bloqueadas").param("tipo", "MAPEAMENTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value(100L));
    }

    @Test
    @WithMockUser
    void listarSubprocessosElegiveis_RetornaLista() throws Exception {
        when(processoService.listarSubprocessosElegiveis(1L)).thenReturn(
                List.of(sgc.processo.dto.SubprocessoElegivelDto.builder().codSubprocesso(10L).build()));

        mockMvc.perform(get("/api/processos/1/subprocessos-elegiveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].codSubprocesso").value(10L));
    }

    @Test
    @WithMockUser
    void listarSubprocessos_RetornaLista() throws Exception {
        when(processoService.listarTodosSubprocessos(1L))
                .thenReturn(List.of(sgc.subprocesso.dto.SubprocessoDto.builder().codigo(10L).build()));

        mockMvc.perform(get("/api/processos/1/subprocessos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].codigo").value(10L));
    }
}
