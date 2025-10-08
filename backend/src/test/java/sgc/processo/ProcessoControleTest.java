package sgc.processo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.processo.dto.AtualizarProcessoReq;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.modelo.ErroProcesso;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ProcessoControleTest {

    @Mock
    private ProcessoService processoService;

    @Captor
    private ArgumentCaptor<CriarProcessoReq> criarCaptor;

    @Captor
    private ArgumentCaptor<AtualizarProcessoReq> atualizarCaptor;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ProcessoControle controle = new ProcessoControle(processoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controle).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void criar_ProcessoValido_RetornaCreatedComUri() throws Exception {
        CriarProcessoReq req = new CriarProcessoReq();
        req.setDescricao("Novo Processo");
        req.setTipo("MAPEAMENTO");
        req.setDataLimiteEtapa1(LocalDate.now().plusDays(30));
        req.setUnidades(List.of(1L));

        ProcessoDto dto = new ProcessoDto();
        dto.setCodigo(1L);
        dto.setDescricao("Novo Processo");

        when(processoService.criar(any(CriarProcessoReq.class))).thenReturn(dto);

        mockMvc.perform(post("/api/processos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/processos/1"))
                .andExpect(jsonPath("$.codigo").value(1L))
                .andExpect(jsonPath("$.descricao").value("Novo Processo"));

        verify(processoService).criar(criarCaptor.capture());
        CriarProcessoReq capturado = criarCaptor.getValue();
        assertEquals("Novo Processo", capturado.getDescricao());
    }

    @Test
    void criar_ProcessoInvalido_RetornaBadRequest() throws Exception {
        CriarProcessoReq req = new CriarProcessoReq();
        req.setDescricao(""); // inválido
        req.setTipo("MAPEAMENTO");
        req.setDataLimiteEtapa1(LocalDate.now().plusDays(30));
        req.setUnidades(List.of(1L));

        mockMvc.perform(post("/api/processos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obterPorId_ProcessoExiste_RetornaOk() throws Exception {
        ProcessoDto dto = new ProcessoDto();
        dto.setCodigo(1L);
        dto.setDescricao("Processo Teste");

        when(processoService.obterPorId(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/processos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1L))
                .andExpect(jsonPath("$.descricao").value("Processo Teste"));

        verify(processoService).obterPorId(1L);
    }

    @Test
    void obterPorId_ProcessoNaoExiste_RetornaNotFound() throws Exception {
        when(processoService.obterPorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/processos/999"))
                .andExpect(status().isNotFound());

        verify(processoService).obterPorId(999L);
    }

    @Test
    void atualizar_ProcessoExiste_RetornaOk() throws Exception {
        AtualizarProcessoReq req = new AtualizarProcessoReq();
        req.setDescricao("Processo Atualizado");
        req.setTipo("REVISAO");
        req.setDataLimiteEtapa1(LocalDate.now().plusDays(45));
        req.setUnidades(List.of(1L));

        ProcessoDto dto = new ProcessoDto();
        dto.setCodigo(1L);
        dto.setDescricao("Processo Atualizado");

        when(processoService.atualizar(eq(1L), any(AtualizarProcessoReq.class))).thenReturn(dto);

        mockMvc.perform(put("/api/processos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1L))
                .andExpect(jsonPath("$.descricao").value("Processo Atualizado"));

        verify(processoService).atualizar(eq(1L), atualizarCaptor.capture());
        AtualizarProcessoReq capturado = atualizarCaptor.getValue();
        assertEquals("Processo Atualizado", capturado.getDescricao());
    }

    @Test
    void atualizar_ProcessoNaoEncontrado_RetornaNotFound() throws Exception {
        AtualizarProcessoReq req = new AtualizarProcessoReq();
        req.setDescricao("Teste");
        req.setTipo("MAPEAMENTO");
        req.setUnidades(List.of(1L));
        
        doThrow(new IllegalArgumentException()).when(processoService).atualizar(eq(999L), any(AtualizarProcessoReq.class));

        mockMvc.perform(put("/api/processos/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizar_ProcessoEstadoInvalido_RetornaBadRequest() throws Exception {
        AtualizarProcessoReq req = new AtualizarProcessoReq();
        req.setDescricao("Teste");
        req.setTipo("MAPEAMENTO");
        req.setUnidades(List.of(1L));
        
        doThrow(new IllegalStateException()).when(processoService).atualizar(eq(1L), any(AtualizarProcessoReq.class));

        mockMvc.perform(put("/api/processos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void excluir_ProcessoExiste_RetornaNoContent() throws Exception {
        mockMvc.perform(delete("/api/processos/1"))
                .andExpect(status().isNoContent());

        verify(processoService).apagar(1L);
    }

    @Test
    void excluir_ProcessoNaoEncontrado_RetornaNotFound() throws Exception {
        doThrow(new IllegalArgumentException()).when(processoService).apagar(999L);

        mockMvc.perform(delete("/api/processos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluir_ProcessoEstadoInvalido_RetornaBadRequest() throws Exception {
        doThrow(new IllegalStateException()).when(processoService).apagar(eq(1L));

        mockMvc.perform(delete("/api/processos/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obterDetalhes_ProcessoExiste_RetornaOk() throws Exception {
        ProcessoDetalheDto dto = new ProcessoDetalheDto();
        dto.setCodigo(1L);
        dto.setDescricao("Processo Detalhado");

        when(processoService.obterDetalhes(eq(1L), eq("ADMIN"), isNull())).thenReturn(dto);

        mockMvc.perform(get("/api/processos/1/detalhes?perfil=ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1L))
                .andExpect(jsonPath("$.descricao").value("Processo Detalhado"));

        verify(processoService).obterDetalhes(eq(1L), eq("ADMIN"), isNull());
    }

    @Test
    void obterDetalhes_ProcessoNaoEncontrado_RetornaNotFound() throws Exception {
        doThrow(new IllegalArgumentException()).when(processoService).obterDetalhes(eq(999L), eq("ADMIN"), isNull());

        mockMvc.perform(get("/api/processos/999/detalhes?perfil=ADMIN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obterDetalhes_AcessoNegado_RetornaForbidden() throws Exception {
        doThrow(new ErroDominioAccessoNegado("Acesso negado")).when(processoService).obterDetalhes(eq(1L), eq("INVALIDO"), isNull());

        mockMvc.perform(get("/api/processos/1/detalhes?perfil=INVALIDO"))
                .andExpect(status().isForbidden());
    }

    @Test
    void iniciarProcessoMapeamento_Valido_RetornaOk() throws Exception {
        ProcessoDto dto = new ProcessoDto();
        dto.setCodigo(1L);
        dto.setDescricao("Processo Iniciado");

        when(processoService.iniciarProcessoMapeamento(eq(1L), any(List.class))).thenReturn(dto);

        mockMvc.perform(post("/api/processos/1/iniciar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1L));

        verify(processoService).iniciarProcessoMapeamento(eq(1L), eq(List.of(1L)));
    }

    @Test
    void iniciarProcessoRevisao_Valido_RetornaOk() throws Exception {
        ProcessoDto dto = new ProcessoDto();
        dto.setCodigo(1L);
        dto.setDescricao("Processo de Revisão Iniciado");

        when(processoService.iniciarProcessoRevisao(eq(1L), any(List.class))).thenReturn(dto);

        mockMvc.perform(post("/api/processos/1/iniciar?tipo=REVISAO")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1L));

        verify(processoService).iniciarProcessoRevisao(eq(1L), eq(List.of(1L)));
    }

    @Test
    void iniciarProcesso_Invalido_RetornaBadRequest() throws Exception {
        doThrow(new IllegalArgumentException()).when(processoService).iniciarProcessoMapeamento(eq(999L), any(List.class));

        mockMvc.perform(post("/api/processos/999/iniciar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(1L))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void finalizar_ProcessoValido_RetornaOk() throws Exception {
        ProcessoDto dto = new ProcessoDto();
        dto.setCodigo(1L);
        dto.setDescricao("Processo Finalizado");

        when(processoService.finalizar(1L)).thenReturn(dto);

        mockMvc.perform(post("/api/processos/1/finalizar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1L));

        verify(processoService).finalizar(1L);
    }

    @Test
    void finalizar_ProcessoNaoEncontrado_RetornaNotFound() throws Exception {
        doThrow(new IllegalArgumentException()).when(processoService).finalizar(999L);

        mockMvc.perform(post("/api/processos/999/finalizar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void finalizar_ProcessoEstadoInvalido_RetornaBadRequest() throws Exception {
        doThrow(new IllegalStateException("Processo em estado inválido")).when(processoService).finalizar(1L);

        mockMvc.perform(post("/api/processos/1/finalizar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Processo em estado inválido"));
    }

    @Test
    void finalizar_ValidacaoFalhou_RetornaUnprocessableEntity() throws Exception {
        doThrow(new ErroProcesso("Subprocessos não homologados")).when(processoService).finalizar(1L);

        mockMvc.perform(post("/api/processos/1/finalizar"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro").value("Subprocessos não homologados"));
    }
}