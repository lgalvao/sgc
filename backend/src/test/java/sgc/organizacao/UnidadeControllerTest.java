package sgc.organizacao;

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
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.dto.CriarAtribuicaoTemporariaReq;

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
@Import(RestExceptionHandler.class)
@DisplayName("Testes do Controller de Unidade")
class UnidadeControllerTest {

    @MockitoBean
    private UnidadeService unidadeService;

    @MockitoBean
    private sgc.seguranca.GerenciadorJwt gerenciadorJwt;

    @MockitoBean
    private sgc.organizacao.model.UsuarioRepo usuarioRepo;

    @MockitoBean
    private sgc.organizacao.model.UsuarioPerfilRepo usuarioPerfilRepo;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar 201 ao criar atribuição temporária")
    @WithMockUser
    void deveRetornar201AoCriarAtribuicaoTemporaria() throws Exception {
        // Act & Assert
        mockMvc.perform(
                        post("/api/unidades/1/atribuicoes-temporarias")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                                {
                                                    "tituloEleitoralUsuario":"123",
                                                    "dataTermino":"2025-12-31",
                                                    "justificativa":"teste"
                                                }
                                                """))
                .andExpect(status().isCreated());

        verify(unidadeService)
                .criarAtribuicaoTemporaria(eq(1L), any(CriarAtribuicaoTemporariaReq.class));
    }

    @Test
    @DisplayName("Deve retornar lista ao buscar todas as unidades")
    @WithMockUser
    void deveRetornarListaAoBuscarTodasUnidades() throws Exception {
        // Arrange
        when(unidadeService.buscarTodasUnidades()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/unidades")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar árvore de elegibilidade")
    @WithMockUser
    void deveRetornarArvoreDeElegibilidade() throws Exception {
        // Arrange
        when(unidadeService.buscarArvoreComElegibilidade(TipoProcesso.MAPEAMENTO, null))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(
                        get("/api/unidades/arvore-com-elegibilidade")
                                .param("tipoProcesso", "MAPEAMENTO"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve verificar mapa vigente e retornar boolean")
    @WithMockUser
    void deveVerificarMapaVigente() throws Exception {
        // Arrange
        when(unidadeService.verificarMapaVigente(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/unidades/1/mapa-vigente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temMapaVigente").value(true));
    }

    @Test
    @DisplayName("Deve retornar lista de usuários por unidade")
    @WithMockUser
    void deveRetornarListaDeUsuariosPorUnidade() throws Exception {
        // Arrange
        when(unidadeService.buscarUsuariosPorUnidade(1L)).thenReturn(List.of(UsuarioDto.builder().build()));

        // Act & Assert
        mockMvc.perform(get("/api/unidades/1/usuarios")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar unidade por sigla")
    @WithMockUser
    void deveRetornarUnidadePorSigla() throws Exception {
        // Arrange
        when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(UnidadeDto.builder().build());

        // Act & Assert
        mockMvc.perform(get("/api/unidades/sigla/SIGLA")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar unidade por ID")
    @WithMockUser
    void deveRetornarUnidadePorId() throws Exception {
        // Arrange
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(UnidadeDto.builder().build());

        // Act & Assert
        mockMvc.perform(get("/api/unidades/1")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar árvore de unidade inexistente")
    @WithMockUser
    void deveRetornar404AoBuscarArvoreDeUnidadeInexistente() throws Exception {
        // Arrange
        when(unidadeService.buscarArvore(99L)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/unidades/99/arvore"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar árvore de unidade quando existente")
    @WithMockUser
    void deveRetornarArvoreDeUnidadeExistente() throws Exception {
        // Arrange
        when(unidadeService.buscarArvore(1L)).thenReturn(UnidadeDto.builder().build());

        // Act & Assert
        mockMvc.perform(get("/api/unidades/1/arvore"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar siglas subordinadas")
    @WithMockUser
    void deveRetornarSiglasSubordinadas() throws Exception {
        // Arrange
        when(unidadeService.buscarSiglasSubordinadas("SIGLA")).thenReturn(List.of("SIGLA", "FILHA"));

        // Act & Assert
        mockMvc.perform(get("/api/unidades/sigla/SIGLA/subordinadas"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar sigla superior")
    @WithMockUser
    void deveRetornarSiglaSuperior() throws Exception {
        // Arrange
        when(unidadeService.buscarSiglaSuperior("FILHA")).thenReturn("SIGLA");

        // Act & Assert
        mockMvc.perform(get("/api/unidades/sigla/FILHA/superior"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 204 ao buscar sigla superior de raiz")
    @WithMockUser
    void deveRetornar204AoBuscarSiglaSuperiorDeRaiz() throws Exception {
        // Arrange
        when(unidadeService.buscarSiglaSuperior("RAIZ")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/unidades/sigla/RAIZ/superior"))
                .andExpect(status().isNoContent());
    }
}
