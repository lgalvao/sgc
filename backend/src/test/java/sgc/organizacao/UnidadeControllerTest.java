package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.RestExceptionHandler;
import sgc.organizacao.dto.CriarAtribuicaoTemporariaRequest;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.UsuarioPerfilRepo;
import sgc.organizacao.model.UsuarioRepo;
import sgc.processo.service.ProcessoFacade;
import sgc.seguranca.login.GerenciadorJwt;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UnidadeController.class)
@Import(RestExceptionHandler.class)
@Tag("integration")
@DisplayName("Testes do Controller de Unidade")
class UnidadeControllerTest {

    @MockitoBean
    private UnidadeFacade unidadeService;

    @MockitoBean
    private ProcessoFacade processoFacade;

    @MockitoBean
    private GerenciadorJwt gerenciadorJwt;

    @MockitoBean
    private UsuarioRepo usuarioRepo;

    @MockitoBean
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar 201 ao criar atribuição temporária")
    @WithMockUser(roles = "ADMIN")
    void deveRetornar201AoCriarAtribuicaoTemporaria() throws Exception {
        // Act & Assert
        mockMvc.perform(
                        post("/api/unidades/1/atribuicoes-temporarias")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "tituloEleitoralUsuario":"123",
                                            "dataTermino":"2025-12-31",
                                            "justificativa":"teste"
                                        }
                                        """))
                .andExpect(status().isCreated());

        verify(unidadeService)
                .criarAtribuicaoTemporaria(eq(1L), any(CriarAtribuicaoTemporariaRequest.class));
    }

    @Test
    @DisplayName("Deve retornar 400 ao criar atribuição temporária com justificativa longa")
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoCriarAtribuicaoTemporariaComJustificativaLonga() throws Exception {
        // Act & Assert
        mockMvc.perform(
                        post("/api/unidades/1/atribuicoes-temporarias")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{ \"tituloEleitoralUsuario\":\"123\", \"dataTermino\":\"2025-12-31\", \"justificativa\":\"%s\" }"
                                                .formatted("a".repeat(501))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar lista ao buscar todas as unidades")
    @WithMockUser
    void deveRetornarListaAoBuscarTodasUnidades() throws Exception {
        // Arrange
        when(unidadeService.buscarTodasUnidades()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/unidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Deve retornar lista vazia ao buscar atribuições quando não há nenhuma")
    @WithMockUser(roles = "ADMIN")
    void deveRetornarListaVaziaAoBuscarAtribuicoes() throws Exception {
        // Arrange
        when(unidadeService.buscarTodasAtribuicoes()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/unidades/atribuicoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Deve retornar árvore de elegibilidade")
    @WithMockUser
    void deveRetornarArvoreDeElegibilidade() throws Exception {
        // Arrange
        when(processoFacade.buscarIdsUnidadesEmProcessosAtivos(any()))
                .thenReturn(Collections.emptySet());
        when(unidadeService.buscarArvoreComElegibilidade(anyBoolean(), any()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/unidades/arvore-com-elegibilidade")
                        .param("tipoProcesso", "MAPEAMENTO"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar árvore de elegibilidade para REVISAO")
    @WithMockUser
    void deveRetornarArvoreDeElegibilidadeRevisao() throws Exception {
        // Arrange
        when(processoFacade.buscarIdsUnidadesEmProcessosAtivos(any()))
                .thenReturn(Collections.emptySet());
        when(unidadeService.buscarArvoreComElegibilidade(eq(true), any()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/unidades/arvore-com-elegibilidade")
                        .param("tipoProcesso", "REVISAO"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar árvore de elegibilidade para DIAGNOSTICO")
    @WithMockUser
    void deveRetornarArvoreDeElegibilidadeDiagnostico() throws Exception {
        // Arrange
        when(processoFacade.buscarIdsUnidadesEmProcessosAtivos(any()))
                .thenReturn(Collections.emptySet());
        when(unidadeService.buscarArvoreComElegibilidade(eq(true), any()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/unidades/arvore-com-elegibilidade")
                        .param("tipoProcesso", "DIAGNOSTICO"))
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
    @WithMockUser(roles = "CHEFE")
    void deveRetornarListaDeUsuariosPorUnidade() throws Exception {
        // Arrange
        when(unidadeService.buscarUsuariosPorUnidade(1L)).thenReturn(List.of(UsuarioDto.builder().build()));

        // Act & Assert
        mockMvc.perform(get("/api/unidades/1/usuarios")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando unidade não tem usuários")
    @WithMockUser(roles = "CHEFE")
    void deveRetornarListaVaziaQuandoUnidadeNaoTemUsuarios() throws Exception {
        // Arrange
        when(unidadeService.buscarUsuariosPorUnidade(999L)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/unidades/999/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
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
    @DisplayName("Deve retornar lista vazia quando unidade não tem subordinadas")
    @WithMockUser
    void deveRetornarListaVaziaQuandoNaoTemSubordinadas() throws Exception {
        // Arrange
        when(unidadeService.buscarSiglasSubordinadas("FOLHA")).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/unidades/sigla/FOLHA/subordinadas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Deve retornar sigla superior")
    @WithMockUser
    void deveRetornarSiglaSuperior() throws Exception {
        // Arrange
        when(unidadeService.buscarSiglaSuperior("FILHA")).thenReturn(Optional.of("SIGLA"));

        // Act & Assert
        mockMvc.perform(get("/api/unidades/sigla/FILHA/superior"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 204 ao buscar sigla superior de raiz")
    @WithMockUser
    void deveRetornar204AoBuscarSiglaSuperiorDeRaiz() throws Exception {
        // Arrange
        when(unidadeService.buscarSiglaSuperior("RAIZ")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/unidades/sigla/RAIZ/superior"))
                .andExpect(status().isNoContent());
    }
}
