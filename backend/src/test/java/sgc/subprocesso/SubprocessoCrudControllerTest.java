package sgc.subprocesso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.RestExceptionHandler;
import sgc.sgrh.dto.UnidadeDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.service.SubprocessoDtoService;
import sgc.subprocesso.service.SubprocessoService;
import sgc.unidade.service.UnidadeService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubprocessoCrudController.class)
@Import(RestExceptionHandler.class)
class SubprocessoCrudControllerTest {

    @MockitoBean private SubprocessoService subprocessoService;
    @MockitoBean private SubprocessoDtoService subprocessoDtoService;
    @MockitoBean private UnidadeService unidadeService;

    @Autowired private MockMvc mockMvc;

    @Test
    @DisplayName("listar deve retornar lista de subprocessos")
    @WithMockUser
    void listar() throws Exception {
        when(subprocessoDtoService.listar()).thenReturn(List.of(new SubprocessoDto()));

        mockMvc.perform(get("/api/subprocessos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    @DisplayName("obterPorCodigo deve retornar detalhe")
    @WithMockUser
    void obterPorCodigo() throws Exception {
        when(subprocessoDtoService.obterDetalhes(eq(1L), any(), any()))
                .thenReturn(SubprocessoDetalheDto.builder().build());

        mockMvc.perform(get("/api/subprocessos/1")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("buscarPorProcessoEUnidade deve retornar DTO")
    @WithMockUser
    void buscarPorProcessoEUnidade() throws Exception {
        UnidadeDto uDto = new UnidadeDto();
        uDto.setCodigo(10L);

        when(unidadeService.buscarPorSigla("U1")).thenReturn(uDto);
        when(subprocessoDtoService.obterPorProcessoEUnidade(1L, 10L))
                .thenReturn(new SubprocessoDto());

        mockMvc.perform(
                        get("/api/subprocessos/buscar")
                                .param("codProcesso", "1")
                                .param("siglaUnidade", "U1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("criar deve retornar Created")
    @WithMockUser
    void criar() throws Exception {
        SubprocessoDto dto = new SubprocessoDto();
        dto.setCodigo(1L);

        when(subprocessoService.criar(any())).thenReturn(dto);

        mockMvc.perform(
                        post("/api/subprocessos")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"codProcesso\": 1}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/subprocessos/1"));
    }

    @Test
    @DisplayName("atualizar deve retornar Ok")
    @WithMockUser
    void atualizar() throws Exception {
        SubprocessoDto dto = new SubprocessoDto();
        dto.setCodigo(1L);

        when(subprocessoService.atualizar(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(
                        post("/api/subprocessos/1/atualizar")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"codProcesso\": 1}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("excluir deve retornar NoContent")
    @WithMockUser
    void excluir() throws Exception {
        doNothing().when(subprocessoService).excluir(1L);

        mockMvc.perform(post("/api/subprocessos/1/excluir").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("excluir retorna NotFound se erro")
    @WithMockUser
    void excluirNotFound() throws Exception {
        doThrow(new ErroEntidadeNaoEncontrada("Subprocesso", 1L))
                .when(subprocessoService)
                .excluir(1L);

        mockMvc.perform(post("/api/subprocessos/1/excluir").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
