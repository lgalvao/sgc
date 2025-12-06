package sgc.unidade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.RestExceptionHandler;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.dto.ServidorDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.unidade.dto.CriarAtribuicaoTemporariaRequest;
import sgc.unidade.service.UnidadeService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UnidadeController.class)
@Import(RestExceptionHandler.class) // Explicitly import if not automatically picked up by slicing
class UnidadeControllerTest {

    @MockitoBean
    private UnidadeService unidadeService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("criarAtribuicaoTemporaria deve retornar 201")
    @WithMockUser
    void criarAtribuicaoTemporaria() throws Exception {
        mockMvc.perform(post("/api/unidades/1/atribuicoes-temporarias")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tituloEleitoralServidor\":\"123\",\"dataTermino\":\"2025-12-31\",\"justificativa\":\"teste\"}"))
                .andExpect(status().isCreated());

        verify(unidadeService).criarAtribuicaoTemporaria(eq(1L), any(CriarAtribuicaoTemporariaRequest.class));
    }

    @Test
    @DisplayName("buscarTodasUnidades deve retornar lista")
    @WithMockUser
    void buscarTodasUnidades() throws Exception {
        when(unidadeService.buscarTodasUnidades()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/unidades"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("buscarArvoreComElegibilidade deve retornar arvore")
    @WithMockUser
    void buscarArvoreComElegibilidade() throws Exception {
        when(unidadeService.buscarArvoreComElegibilidade(TipoProcesso.MAPEAMENTO, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/unidades/arvore-com-elegibilidade")
                        .param("tipoProcesso", "MAPEAMENTO"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("verificarMapaVigente deve retornar boolean")
    @WithMockUser
    void verificarMapaVigente() throws Exception {
        when(unidadeService.verificarMapaVigente(1L)).thenReturn(true);

        mockMvc.perform(get("/api/unidades/1/mapa-vigente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temMapaVigente").value(true));
    }

    @Test
    @DisplayName("buscarServidoresPorUnidade deve retornar lista")
    @WithMockUser
    void buscarServidoresPorUnidade() throws Exception {
        when(unidadeService.buscarServidoresPorUnidade(1L)).thenReturn(List.of(new ServidorDto()));

        mockMvc.perform(get("/api/unidades/1/servidores"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/unidades/1/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("buscarUnidadePorSigla deve retornar unidade")
    @WithMockUser
    void buscarUnidadePorSigla() throws Exception {
        when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(new UnidadeDto());

        mockMvc.perform(get("/api/unidades/sigla/SIGLA"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("buscarUnidadePorId deve retornar unidade")
    @WithMockUser
    void buscarUnidadePorId() throws Exception {
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(new UnidadeDto());

        mockMvc.perform(get("/api/unidades/1"))
                .andExpect(status().isOk());
    }
}
