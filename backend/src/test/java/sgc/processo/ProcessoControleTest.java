package sgc.processo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.RestExceptionHandler;
import sgc.processo.dto.AtualizarProcessoReq;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.modelo.ErroProcesso;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProcessoControle.class)
public class ProcessoControleTest {
    private static final String NOVO_PROCESSO = "Novo Processo";
    private static final String MAPEAMENTO = "MAPEAMENTO";
    private static final String API_PROCESSOS = "/api/processos";
    private static final String API_PROCESSOS_1 = "/api/processos/1";
    private static final String API_PROCESSOS_999 = "/api/processos/999";
    private static final String CODIGO_JSON_PATH = "$.codigo";
    private static final String DESCRICAO_JSON_PATH = "$.descricao";
    private static final String PROCESSO_ATUALIZADO = "Processo Atualizado";
    private static final String PROCESSO_NAO_ENCONTRADO = "Processo não encontrado";
    private static final String REVISAO = "REVISAO";
    @MockitoBean
    private ProcessoService processoService;

    @MockitoBean
    private ProcessoIniciacaoService processoIniciacaoService;

    @MockitoBean
    private ProcessoFinalizacaoService processoFinalizacaoService;

    @Captor
    private ArgumentCaptor<CriarProcessoReq> criarCaptor;

    @Captor
    private ArgumentCaptor<AtualizarProcessoReq> atualizarCaptor;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ProcessoControle controle = new ProcessoControle(processoService, processoIniciacaoService, processoFinalizacaoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controle)
                .setControllerAdvice(new RestExceptionHandler()) // Register the global exception handler
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void criar_ProcessoValido_RetornaCreatedComUri() throws Exception {
        var req = new CriarProcessoReq(NOVO_PROCESSO, MAPEAMENTO, LocalDateTime.now().plusDays(30), List.of(1L));
        var dto = ProcessoDto.builder()
                .codigo(1L)
                .dataCriacao(LocalDateTime.now())
                .descricao(NOVO_PROCESSO)
                .situacao(SituacaoProcesso.CRIADO)
                .tipo(MAPEAMENTO)
                .build();

        when(processoService.criar(any(CriarProcessoReq.class))).thenReturn(dto);

        mockMvc.perform(post(API_PROCESSOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", API_PROCESSOS_1))
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L))
                .andExpect(jsonPath(DESCRICAO_JSON_PATH).value(NOVO_PROCESSO));

        verify(processoService).criar(criarCaptor.capture());
        CriarProcessoReq capturado = criarCaptor.getValue();
        assertEquals(NOVO_PROCESSO, capturado.descricao());
    }

    @Test
    void criar_ProcessoInvalido_RetornaBadRequest() throws Exception {
        var req = new CriarProcessoReq("", MAPEAMENTO, LocalDateTime.now().plusDays(30), List.of(1L));

        mockMvc.perform(post(API_PROCESSOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obterPorId_ProcessoExiste_RetornaOk() throws Exception {
        var dto = ProcessoDto.builder()
                .codigo(1L)
                .dataCriacao(LocalDateTime.now())
                .descricao("Processo Teste")
                .situacao(SituacaoProcesso.CRIADO)
                .tipo(MAPEAMENTO)
                .build();

        when(processoService.obterPorId(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get(API_PROCESSOS_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L))
                .andExpect(jsonPath(DESCRICAO_JSON_PATH).value("Processo Teste"));

        verify(processoService).obterPorId(1L);
    }

    @Test
    void obterPorId_ProcessoNaoExiste_RetornaNotFound() throws Exception {
        when(processoService.obterPorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get(API_PROCESSOS_999))
                .andExpect(status().isNotFound());

        verify(processoService).obterPorId(999L);
    }

    @Test
    void atualizar_ProcessoExiste_RetornaOk() throws Exception {
        var req = new AtualizarProcessoReq(1L, PROCESSO_ATUALIZADO, REVISAO, LocalDateTime.now().plusDays(45), List.of(1L));
        var dto = ProcessoDto.builder()
                .codigo(1L)
                .dataCriacao(LocalDateTime.now())
                .descricao(PROCESSO_ATUALIZADO)
                .situacao(SituacaoProcesso.CRIADO)
                .tipo(REVISAO)
                .build();

        when(processoService.atualizar(eq(1L), any(AtualizarProcessoReq.class))).thenReturn(dto);

        mockMvc.perform(put(API_PROCESSOS_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODIGO_JSON_PATH).value(1L))
                .andExpect(jsonPath(DESCRICAO_JSON_PATH).value(PROCESSO_ATUALIZADO));

        verify(processoService).atualizar(eq(1L), atualizarCaptor.capture());
        AtualizarProcessoReq capturado = atualizarCaptor.getValue();
        assertEquals(PROCESSO_ATUALIZADO, capturado.descricao());
    }

    @Test
    void atualizar_ProcessoNaoEncontrado_RetornaNotFound() throws Exception {
        var req = new AtualizarProcessoReq(999L, "Teste", MAPEAMENTO, null, List.of(1L));

        doThrow(new sgc.comum.erros.ErroDominioNaoEncontrado(PROCESSO_NAO_ENCONTRADO)).when(processoService).atualizar(eq(999L), any(AtualizarProcessoReq.class));

        mockMvc.perform(put(API_PROCESSOS_999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizar_ProcessoEstadoInvalido_RetornaBadRequest() throws Exception {
        var req = new AtualizarProcessoReq(1L, "Teste", MAPEAMENTO, null, List.of(1L));

        doThrow(new IllegalStateException()).when(processoService).atualizar(eq(1L), any(AtualizarProcessoReq.class));

        mockMvc.perform(put(API_PROCESSOS_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void excluir_ProcessoExiste_RetornaNoContent() throws Exception {
        mockMvc.perform(delete(API_PROCESSOS_1))
                .andExpect(status().isNoContent());

        verify(processoService).apagar(1L);
    }

    @Test
    void excluir_ProcessoNaoEncontrado_RetornaNotFound() throws Exception {
        doThrow(new sgc.comum.erros.ErroDominioNaoEncontrado(PROCESSO_NAO_ENCONTRADO)).when(processoService).apagar(999L);

        mockMvc.perform(delete(API_PROCESSOS_999))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluir_ProcessoEstadoInvalido_RetornaBadRequest() throws Exception {
        doThrow(new IllegalStateException()).when(processoService).apagar(eq(1L));

        mockMvc.perform(delete(API_PROCESSOS_1))
                .andExpect(status().isConflict());
    }

    @Test
    void obterDetalhes_ProcessoExiste_RetornaOk() throws Exception {
        var dto = ProcessoDetalheDto.builder()
                .codigo(1L)
                .descricao("Processo Detalhado")
                .tipo(MAPEAMENTO)
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
    void obterDetalhes_ProcessoNaoEncontrado_RetornaNotFound() throws Exception {
        doThrow(new sgc.comum.erros.ErroDominioNaoEncontrado(PROCESSO_NAO_ENCONTRADO)).when(processoService).obterDetalhes(eq(999L));

        mockMvc.perform(get("/api/processos/999/detalhes"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obterDetalhes_AcessoNegado_RetornaForbidden() throws Exception {
        doThrow(new ErroDominioAccessoNegado("Acesso negado")).when(processoService).obterDetalhes(eq(1L));

        mockMvc.perform(get("/api/processos/1/detalhes"))
                .andExpect(status().isForbidden());
    }

    @Test
    void iniciarProcessoMapeamento_Valido_RetornaOk() throws Exception {
        doNothing().when(processoIniciacaoService).iniciarProcessoMapeamento(eq(1L), anyList());

        mockMvc.perform(post("/api/processos/1/iniciar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(1L))))
                .andExpect(status().isOk());

        verify(processoIniciacaoService).iniciarProcessoMapeamento(eq(1L), eq(List.of(1L)));
    }

    @Test
    void iniciarProcessoRevisao_Valido_RetornaOk() throws Exception {
        doNothing().when(processoIniciacaoService).iniciarProcessoRevisao(eq(1L), anyList());

        mockMvc.perform(post(API_PROCESSOS_1 + "/iniciar?tipo=REVISAO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(1L))))
                .andExpect(status().isOk());

        verify(processoIniciacaoService).iniciarProcessoRevisao(eq(1L), eq(List.of(1L)));
    }

    @Test
    void iniciarProcesso_Invalido_RetornaBadRequest() throws Exception {
        doThrow(new IllegalArgumentException()).when(processoIniciacaoService).iniciarProcessoMapeamento(eq(999L), anyList());

        mockMvc.perform(post("/api/processos/999/iniciar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(1L))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void finalizar_ProcessoValido_RetornaOk() throws Exception {
        doNothing().when(processoFinalizacaoService).finalizar(1L);

        mockMvc.perform(post("/api/processos/1/finalizar")).andExpect(status().isOk());

        verify(processoFinalizacaoService).finalizar(1L);
    }

    @Test
    void finalizar_ProcessoNaoEncontrado_RetornaNotFound() throws Exception {
        doThrow(new sgc.comum.erros.ErroDominioNaoEncontrado(PROCESSO_NAO_ENCONTRADO)).when(processoFinalizacaoService).finalizar(999L);

        mockMvc.perform(post(API_PROCESSOS_999 + "/finalizar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void finalizar_ProcessoEstadoInvalido_RetornaBadRequest() throws Exception {
        doThrow(new IllegalStateException("Processo em estado inválido")).when(processoFinalizacaoService).finalizar(1L);

        mockMvc.perform(post("/api/processos/1/finalizar"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A operação não pode ser executada no estado atual do recurso."));
    }

    @Test
    void finalizar_ValidacaoFalhou_RetornaUnprocessableEntity() throws Exception {
        doThrow(new ErroProcesso("Subprocessos não homologados")).when(processoFinalizacaoService).finalizar(1L);

        mockMvc.perform(post("/api/processos/1/finalizar"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Subprocessos não homologados"));
    }
}