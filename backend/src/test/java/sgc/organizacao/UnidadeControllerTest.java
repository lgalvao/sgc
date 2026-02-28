package sgc.organizacao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.comum.erros.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.processo.*;
import sgc.seguranca.*;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UnidadeController.class)
@Import(RestExceptionHandler.class)
@Tag("integration")
@DisplayName("Testes do Controller de Unidade")
class UnidadeControllerTest {

    @MockitoBean
    private OrganizacaoFacade unidadeService;

    @MockitoBean
    private ProcessoFacade processoFacade;

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    @Test
    @DisplayName("Deve retornar 201 ao criar atribuição temporária")
    @WithMockUser(roles = "ADMIN")
    void deveRetornar201AoCriarAtribuicaoTemporaria() throws Exception {
        String conteudo = """
                {
                    "tituloEleitoralUsuario":"123",
                    "dataTermino":"2025-12-31",
                    "justificativa":"teste"
                }""";

        mockMvc.perform(post("/api/unidades/1/atribuicoes-temporarias")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conteudo))
                .andExpect(status().isCreated());

        verify(unidadeService)
                .criarAtribuicaoTemporaria(eq(1L), any(CriarAtribuicaoRequest.class));
    }

    @Test
    @DisplayName("Deve retornar lista ao buscar todas as unidades")
    @WithMockUser
    void deveRetornarListaAoBuscarTodasUnidades() throws Exception {

        when(unidadeService.buscarTodasUnidades()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/unidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Deve retornar árvore de elegibilidade")
    @WithMockUser
    void deveRetornarArvoreDeElegibilidade() throws Exception {

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
    @DisplayName("Deve verificar mapa vigente e retornar boolean")
    @WithMockUser
    void deveVerificarMapaVigente() throws Exception {

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

        Usuario usuario = Usuario.builder()
                .tituloEleitoral("123")
                .nome("Teste")
                .unidadeLotacao(Unidade.builder().codigo(1L).build())
                .build();
        when(unidadeService.usuariosPorCodigoUnidade(1L)).thenReturn(List.of(usuario));

        // Act & Assert
        mockMvc.perform(get("/api/unidades/1/usuarios")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar unidade por sigla")
    @WithMockUser
    void deveRetornarUnidadePorSigla() throws Exception {

        when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(UnidadeDto.builder().build());

        // Act & Assert
        mockMvc.perform(get("/api/unidades/sigla/SIGLA")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar unidade por ID")
    @WithMockUser
    void deveRetornarUnidadePorId() throws Exception {

        when(unidadeService.dtoPorCodigo(1L)).thenReturn(UnidadeDto.builder().build());

        // Act & Assert
        mockMvc.perform(get("/api/unidades/1")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar árvore de unidade quando existente")
    @WithMockUser
    void deveRetornarArvoreDeUnidadeExistente() throws Exception {

        when(unidadeService.buscarArvore(1L)).thenReturn(UnidadeDto.builder().build());

        // Act & Assert
        mockMvc.perform(get("/api/unidades/1/arvore"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar siglas subordinadas")
    @WithMockUser
    void deveRetornarSiglasSubordinadas() throws Exception {

        when(unidadeService.buscarSiglasSubordinadas("SIGLA")).thenReturn(List.of("SIGLA", "FILHA"));

        // Act & Assert
        mockMvc.perform(get("/api/unidades/sigla/SIGLA/subordinadas"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar sigla superior")
    @WithMockUser
    void deveRetornarSiglaSuperior() throws Exception {

        when(unidadeService.buscarSiglaSuperior("FILHA")).thenReturn(Optional.of("SIGLA"));

        // Act & Assert
        mockMvc.perform(get("/api/unidades/sigla/FILHA/superior"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 204 ao buscar sigla superior de raiz")
    @WithMockUser
    void deveRetornar204AoBuscarSiglaSuperiorDeRaiz() throws Exception {

        when(unidadeService.buscarSiglaSuperior("RAIZ")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/unidades/sigla/RAIZ/superior"))
                .andExpect(status().isNoContent());
    }
}
