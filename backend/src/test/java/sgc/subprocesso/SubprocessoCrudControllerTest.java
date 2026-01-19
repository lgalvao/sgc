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
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.dto.SubprocessoDto;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubprocessoCrudController.class)
@Import(RestExceptionHandler.class)
class SubprocessoCrudControllerTest {
    @MockitoBean
    private sgc.subprocesso.service.SubprocessoFacade subprocessoFacade;

    @MockitoBean
    private UnidadeFacade unidadeService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("listar deve retornar lista de subprocessos")
    @WithMockUser(roles = "ADMIN")
    void listar() throws Exception {
        when(subprocessoFacade.listar()).thenReturn(List.of(new SubprocessoDto()));

        mockMvc.perform(get("/api/subprocessos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    @DisplayName("obterPorCodigo deve retornar detalhe")
    @WithMockUser
    void obterPorCodigo() throws Exception {
        when(subprocessoFacade.obterDetalhes(eq(1L), any()))
                .thenReturn(SubprocessoDetalheDto.builder().build());

        mockMvc.perform(get("/api/subprocessos/1")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("buscarPorProcessoEUnidade deve retornar DTO")
    @WithMockUser
    void buscarPorProcessoEUnidade() throws Exception {
        UnidadeDto uDto = UnidadeDto.builder().codigo(10L).build();

        when(unidadeService.buscarPorSigla("U1")).thenReturn(uDto);
        when(subprocessoFacade.obterPorProcessoEUnidade(1L, 10L))
                .thenReturn(new SubprocessoDto());

        mockMvc.perform(
                        get("/api/subprocessos/buscar")
                                .param("codProcesso", "1")
                                .param("siglaUnidade", "U1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("criar deve retornar Created")
    @WithMockUser(roles = "ADMIN")
    void criar() throws Exception {
        SubprocessoDto dto = new SubprocessoDto();
        dto.setCodigo(1L);

        when(subprocessoFacade.criar(any())).thenReturn(dto);

        mockMvc.perform(post("/api/subprocessos").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codProcesso\": 1, \"codUnidade\": 10}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/subprocessos/1"));
    }

    @Test
    @DisplayName("atualizar deve retornar Ok")
    @WithMockUser(roles = "ADMIN")
    void atualizar() throws Exception {
        SubprocessoDto dto = new SubprocessoDto();
        dto.setCodigo(1L);

        when(subprocessoFacade.atualizar(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(post("/api/subprocessos/1/atualizar").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codProcesso\": 1}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("excluir deve retornar NoContent")
    @WithMockUser(roles = "ADMIN")
    void excluir() throws Exception {
        doNothing().when(subprocessoFacade).excluir(1L);

        mockMvc.perform(post("/api/subprocessos/1/excluir").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("excluir retorna NotFound se erro")
    @WithMockUser(roles = "ADMIN")
    void excluirNotFound() throws Exception {
        doThrow(new ErroEntidadeNaoEncontrada("Subprocesso", 1L))
                .when(subprocessoFacade)
                .excluir(1L);

        mockMvc.perform(post("/api/subprocessos/1/excluir").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("obterPermissoes deve retornar OK")
    @WithMockUser
    void obterPermissoes() throws Exception {
        mockMvc.perform(get("/api/subprocessos/1/permissoes"))
                .andExpect(status().isOk());
        verify(subprocessoFacade).obterPermissoes(1L);
    }

    @Test
    @DisplayName("validarCadastro deve retornar OK")
    @WithMockUser
    void validarCadastro() throws Exception {
        mockMvc.perform(get("/api/subprocessos/1/validar-cadastro")).andExpect(status().isOk());
        verify(subprocessoFacade).validarCadastro(1L);
    }

    @Test
    @DisplayName("obterStatus deve retornar OK")
    @WithMockUser
    void obterStatus() throws Exception {
        mockMvc.perform(get("/api/subprocessos/1/status")).andExpect(status().isOk());
        verify(subprocessoFacade).obterSituacao(1L);
    }

    @Test
    @DisplayName("alterarDataLimite deve retornar OK")
    @WithMockUser(roles = "ADMIN")
    void alterarDataLimite() throws Exception {
        mockMvc.perform(post("/api/subprocessos/1/data-limite")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novaDataLimite\": \"2030-01-01\"}"))
                .andExpect(status().isOk());
        verify(subprocessoFacade).alterarDataLimite(eq(1L), any());
    }

    @Test
    @DisplayName("reabrirCadastro deve retornar OK")
    @WithMockUser(roles = "ADMIN")
    void reabrirCadastro() throws Exception {
        mockMvc.perform(post("/api/subprocessos/1/reabrir-cadastro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"justificativa\": \"Erro\"}"))
                .andExpect(status().isOk());
        verify(subprocessoFacade).reabrirCadastro(1L, "Erro");
    }

    @Test
    @DisplayName("reabrirRevisaoCadastro deve retornar OK")
    @WithMockUser(roles = "ADMIN")
    void reabrirRevisaoCadastro() throws Exception {
        mockMvc.perform(post("/api/subprocessos/1/reabrir-revisao-cadastro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"justificativa\": \"Erro\"}"))
                .andExpect(status().isOk());
        verify(subprocessoFacade).reabrirRevisaoCadastro(1L, "Erro");
    }
}
