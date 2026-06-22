package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.erros.RestExceptionHandler;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UsuarioConsultaLeitura;
import sgc.organizacao.service.ResponsavelUnidadeService;
import sgc.organizacao.service.UnidadeHierarquiaService;
import sgc.organizacao.service.UnidadeService;
import sgc.organizacao.service.UsuarioService;
import sgc.processo.service.ProcessoService;
import sgc.seguranca.SgcPermissionEvaluator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UnidadeController.class)
@EnableMethodSecurity
@Import({RestExceptionHandler.class, OrganizacaoDtoMapper.class})
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

    @MockitoBean
    private ValidadorDadosOrganizacionais validadorDadosOrganizacionais;

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
                    "dataInicio":"2025-01-01",
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
    @DisplayName("Deve retornar atribuições da unidade")
    @WithMockUser(roles = "ADMIN")
    void deveRetornarAtribuicoesDaUnidade() throws Exception {
        when(responsavelService.buscarAtribuicoesPorUnidade(1L)).thenReturn(List.of(AtribuicaoDto.builder().codigo(10L).build()));

        mockMvc.perform(get("/api/unidades/1/atribuicoes-temporarias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(10));
    }

    @Test
    @DisplayName("Deve atualizar atribuição temporária")
    @WithMockUser(roles = "ADMIN")
    void deveAtualizarAtribuicaoTemporaria() throws Exception {
        String conteudo = """
                {
                    "tituloEleitoralUsuario":"123",
                    "dataInicio":"2025-01-01",
                    "dataTermino":"2025-12-31",
                    "justificativa":"teste"
                }""";

        mockMvc.perform(post("/api/unidades/1/atribuicoes-temporarias/9/atualizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conteudo))
                .andExpect(status().isOk());

        verify(responsavelService)
                .atualizarAtribuicaoTemporaria(eq(1L), eq(9L), any(CriarAtribuicaoRequest.class));
    }

    @Test
    @DisplayName("Deve remover atribuição temporária")
    @WithMockUser(roles = "ADMIN")
    void deveRemoverAtribuicaoTemporaria() throws Exception {
        mockMvc.perform(post("/api/unidades/1/atribuicoes-temporarias/9/excluir")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(responsavelService).removerAtribuicaoTemporaria(1L, 9L);
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
    @DisplayName("Deve retornar diagnostico organizacional para ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deveRetornarDiagnosticoOrganizacional() throws Exception {
        when(validadorDadosOrganizacionais.diagnosticar()).thenReturn(DiagnosticoOrganizacionalDto.semViolacoes());

        mockMvc.perform(get("/api/unidades/diagnostico-organizacional"))
                .andExpect(status().isOk());

        verify(validadorDadosOrganizacionais).diagnosticar();
    }

    @Test
    @DisplayName("Deve retornar codigos das unidades com mapa vigente")
    @WithMockUser
    void deveRetornarCodigosUnidadesComMapaVigente() throws Exception {
        when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(10L, 20L));

        mockMvc.perform(get("/api/unidades/com-mapa-vigente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(10))
                .andExpect(jsonPath("$[1]").value(20));
    }

    @Test
    @DisplayName("Deve retornar codigos das unidades sem mapa vigente")
    @WithMockUser(roles = "ADMIN")
    void deveRetornarCodigosUnidadesSemMapaVigente() throws Exception {
        when(unidadeService.buscarCodigosUnidadesSemMapaVigente()).thenReturn(List.of(30L, 40L));

        mockMvc.perform(get("/api/unidades/sem-mapa-vigente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(30))
                .andExpect(jsonPath("$[1]").value(40));
    }

    @Test
    @DisplayName("Deve proibir perfil não admin ao buscar codigos das unidades sem mapa vigente")
    @WithMockUser
    void deveProibirBuscaCodigosUnidadesSemMapaVigenteParaNaoAdmin() throws Exception {
        mockMvc.perform(get("/api/unidades/sem-mapa-vigente"))
                .andExpect(status().isForbidden());
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
    @DisplayName("Deve exigir mapa vigente para revisão ao buscar árvore de elegibilidade")
    @WithMockUser
    void deveExigirMapaVigenteParaRevisaoAoBuscarArvoreDeElegibilidade() throws Exception {
        when(processoService.buscarIdsUnidadesComProcessosAtivos(10L))
                .thenReturn(Set.of(1L, 2L));
        when(hierarquiaService.buscarArvoreComElegibilidade(true, Set.of(1L, 2L)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/unidades/arvore-com-elegibilidade")
                        .param("tipoProcesso", "REVISAO")
                        .param("codProcesso", "10"))
                .andExpect(status().isOk());

        verify(hierarquiaService).buscarArvoreComElegibilidade(true, Set.of(1L, 2L));
    }

    @Test
    @DisplayName("Deve exigir mapa vigente para diagnóstico ao buscar árvore de elegibilidade")
    @WithMockUser
    void deveExigirMapaVigenteParaDiagnosticoAoBuscarArvoreDeElegibilidade() throws Exception {
        when(processoService.buscarIdsUnidadesComProcessosAtivos(any()))
                .thenReturn(Set.of());
        when(hierarquiaService.buscarArvoreComElegibilidade(true, Set.of()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/unidades/arvore-com-elegibilidade")
                        .param("tipoProcesso", "DIAGNOSTICO"))
                .andExpect(status().isOk());

        verify(hierarquiaService).buscarArvoreComElegibilidade(true, Set.of());
    }


    @Test
    @DisplayName("Deve retornar referência do mapa vigente quando existente")
    @WithMockUser
    void deveRetornarReferenciaMapaVigente() throws Exception {
        when(unidadeService.buscarReferenciaMapaVigente(1L))
                .thenReturn(Optional.of(new MapaVigenteReferenciaDto(10L, 20L, true)));

        mockMvc.perform(get("/api/unidades/1/mapa-vigente/referencia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codProcesso").value(10))
                .andExpect(jsonPath("$.codSubprocesso").value(20))
                .andExpect(jsonPath("$.podeExportar").value(true));
    }

    @Test
    @DisplayName("Deve retornar 204 quando não houver referência de mapa vigente")
    @WithMockUser
    void deveRetornar204SemReferenciaMapaVigente() throws Exception {
        when(unidadeService.buscarReferenciaMapaVigente(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/unidades/1/mapa-vigente/referencia"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar lista de usuários por unidade")
    @WithMockUser(roles = "CHEFE")
    void deveRetornarListaDeUsuariosPorUnidade() throws Exception {
        UsuarioConsultaLeitura usuario = UsuarioConsultaLeitura.builder()
                .tituloEleitoral("123")
                .matricula("111")
                .nome("Teste")
                .email("teste@x.com")
                .ramal("1234")
                .unidadeCodigo(1L)
                .unidadeNome("Unidade 1")
                .unidadeSigla("U1")
                .unidadeTipo(TipoUnidade.OPERACIONAL)
                .build();
        when(usuarioServiceBean.buscarConsultasPorUnidadeLotacao(1L)).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/unidades/1/usuarios")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar unidade por sigla")
    @WithMockUser
    void deveRetornarUnidadePorSigla() throws Exception {

        Unidade u = new Unidade();
        u.setCodigo(1L);
        u.setNome("Unidade SIGLA");
        u.setSigla("SIGLA");
        u.setTipo(TipoUnidade.OPERACIONAL);
        when(unidadeService.buscarPorSiglaComResponsavel("SIGLA")).thenReturn(u);

        mockMvc.perform(get("/api/unidades/sigla/SIGLA")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar unidade por código")
    @WithMockUser
    void deveRetornarUnidadePorCodigo() throws Exception {
        Unidade un = new Unidade();
        un.setCodigo(1L);
        un.setNome("Unidade 1");
        un.setSigla("U1");
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
