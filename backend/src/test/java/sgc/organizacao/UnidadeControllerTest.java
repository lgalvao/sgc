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
import sgc.organizacao.service.*;
import sgc.processo.service.*;
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
    private UnidadeService unidadeService;

    @MockitoBean
    private UnidadeHierarquiaService hierarquiaService;

    @MockitoBean
    private ResponsavelUnidadeService responsavelService;

    @MockitoBean
    private UsuarioService usuarioServiceBean;

    @MockitoBean
    private ProcessoService processoService;

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

        verify(responsavelService)
                .criarAtribuicaoTemporaria(eq(1L), any(CriarAtribuicaoRequest.class));
    }

    @Test
    @DisplayName("Deve retornar lista ao buscar todas as unidades")
    @WithMockUser
    void deveRetornarListaAoBuscarTodasUnidades() throws Exception {

        when(hierarquiaService.buscarArvoreHierarquica()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/unidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Deve retornar árvore de elegibilidade")
    @WithMockUser
    void deveRetornarArvoreDeElegibilidade() throws Exception {

        when(processoService.buscarIdsUnidadesComProcessosAtivos(any()))
                .thenReturn(Collections.emptySet());
        when(hierarquiaService.buscarArvoreComElegibilidade(anyBoolean(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/unidades/arvore-com-elegibilidade")
                        .param("tipoProcesso", "MAPEAMENTO"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve verificar mapa vigente e retornar boolean")
    @WithMockUser
    void deveVerificarMapaVigente() throws Exception {

        when(unidadeService.verificarMapaVigente(1L)).thenReturn(true);

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
        when(usuarioServiceBean.buscarPorUnidadeLotacao(1L)).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/unidades/1/usuarios")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar unidade por sigla")
    @WithMockUser
    void deveRetornarUnidadePorSigla() throws Exception {

        Unidade u = new Unidade();
        u.setSigla("SIGLA");
        u.setTipo(TipoUnidade.OPERACIONAL);
        when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(u);

        mockMvc.perform(get("/api/unidades/sigla/SIGLA")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar unidade por ID")
    @WithMockUser
    void deveRetornarUnidadePorId() throws Exception {

        Unidade un = new Unidade();
        un.setCodigo(1L);
        un.setTipo(TipoUnidade.OPERACIONAL);
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(un);

        mockMvc.perform(get("/api/unidades/1")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar árvore de unidade quando existente")
    @WithMockUser
    void deveRetornarArvoreDeUnidadeExistente() throws Exception {

        when(hierarquiaService.buscarArvore(1L)).thenReturn(UnidadeDto.builder().build());

        mockMvc.perform(get("/api/unidades/1/arvore"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar siglas subordinadas")
    @WithMockUser
    void deveRetornarSiglasSubordinadas() throws Exception {

        when(hierarquiaService.buscarSiglasSubordinadas("SIGLA")).thenReturn(List.of("SIGLA", "FILHA"));

        mockMvc.perform(get("/api/unidades/sigla/SIGLA/subordinadas"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar sigla superior")
    @WithMockUser
    void deveRetornarSiglaSuperior() throws Exception {

        when(hierarquiaService.buscarSiglaSuperior("FILHA")).thenReturn(Optional.of("SIGLA"));

        mockMvc.perform(get("/api/unidades/sigla/FILHA/superior"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 204 ao buscar sigla superior de raiz")
    @WithMockUser
    void deveRetornar204AoBuscarSiglaSuperiorDeRaiz() throws Exception {

        when(hierarquiaService.buscarSiglaSuperior("RAIZ")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/unidades/sigla/RAIZ/superior"))
                .andExpect(status().isNoContent());
    }
}
