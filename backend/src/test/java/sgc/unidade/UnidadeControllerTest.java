package sgc.unidade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sgc.comum.erros.RestExceptionHandler;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.dto.ServidorDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.unidade.dto.CriarAtribuicaoTemporariaRequest;
import sgc.unidade.service.UnidadeService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UnidadeControllerTest {

    @Mock
    private UnidadeService unidadeService;

    @InjectMocks
    private UnidadeController unidadeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(unidadeController)
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("criarAtribuicaoTemporaria deve retornar 201")
    void criarAtribuicaoTemporaria() throws Exception {
        mockMvc.perform(post("/api/unidades/1/atribuicoes-temporarias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tituloEleitoralServidor\":\"123\",\"dataTermino\":\"2025-12-31\",\"justificativa\":\"teste\"}"))
                .andExpect(status().isCreated());

        verify(unidadeService).criarAtribuicaoTemporaria(eq(1L), any(CriarAtribuicaoTemporariaRequest.class));
    }

    @Test
    @DisplayName("buscarTodasUnidades deve retornar lista")
    void buscarTodasUnidades() throws Exception {
        when(unidadeService.buscarTodasUnidades()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/unidades"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("buscarArvoreComElegibilidade deve retornar arvore")
    void buscarArvoreComElegibilidade() throws Exception {
        when(unidadeService.buscarArvoreComElegibilidade(TipoProcesso.MAPEAMENTO, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/unidades/arvore-com-elegibilidade")
                        .param("tipoProcesso", "MAPEAMENTO"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("verificarMapaVigente deve retornar boolean")
    void verificarMapaVigente() throws Exception {
        when(unidadeService.verificarMapaVigente(1L)).thenReturn(true);

        mockMvc.perform(get("/api/unidades/1/mapa-vigente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temMapaVigente").value(true));
    }

    @Test
    @DisplayName("buscarServidoresPorUnidade deve retornar lista")
    void buscarServidoresPorUnidade() throws Exception {
        when(unidadeService.buscarServidoresPorUnidade(1L)).thenReturn(List.of(new ServidorDto()));

        mockMvc.perform(get("/api/unidades/1/servidores"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/unidades/1/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("buscarUnidadePorSigla deve retornar unidade")
    void buscarUnidadePorSigla() throws Exception {
        when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(new UnidadeDto());

        mockMvc.perform(get("/api/unidades/sigla/SIGLA"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("buscarUnidadePorId deve retornar unidade")
    void buscarUnidadePorId() throws Exception {
        when(unidadeService.buscarPorId(1L)).thenReturn(new UnidadeDto());

        mockMvc.perform(get("/api/unidades/1"))
                .andExpect(status().isOk());
    }
}
