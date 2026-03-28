package sgc.subprocesso;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.comum.ComumDtos.*;
import sgc.comum.erros.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.organizacao.service.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubprocessoController.class)
@Import(RestExceptionHandler.class)
@DisplayName("SubprocessoController - Cobertura extra")
class SubprocessoControllerCoverageExtraTest {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private SubprocessoService subprocessoService;
    
    @MockitoBean
    private SubprocessoTransicaoService transicaoService;

    @MockitoBean
    private UnidadeService unidadeService;

    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("disponibilizarCadastro - erro validacao")
    @WithMockUser
    void disponibilizarCadastroErro() throws Exception {
        doThrow(new ErroValidacao("Existem atividades sem conhecimentos associados."))
                .when(transicaoService).disponibilizarCadastro(eq(1L), any());
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("DISPONIBILIZAR_CADASTRO"))).thenReturn(true);

        mockMvc.perform(post("/api/subprocessos/1/cadastro/disponibilizar").with(csrf()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Existem atividades sem conhecimentos associados."));

        verify(transicaoService).disponibilizarCadastro(eq(1L), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("disponibilizarRevisao - erro validacao")
    @WithMockUser
    void disponibilizarRevisaoErro() throws Exception {
        doThrow(new ErroValidacao("Existem atividades sem conhecimentos associados."))
                .when(transicaoService).disponibilizarRevisao(eq(1L), any());
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("DISPONIBILIZAR_REVISAO_CADASTRO"))).thenReturn(true);

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar-revisao").with(csrf()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Existem atividades sem conhecimentos associados."));

        verify(transicaoService).disponibilizarRevisao(eq(1L), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("obterMapaCompleto - erro generico")
    @WithMockUser
    void obterMapaCompletoErro() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(subprocessoService.mapaCompletoDtoPorSubprocesso(1L)).thenThrow(new RuntimeException("erro"));

        mockMvc.perform(get("/api/subprocessos/1/mapa-completo"))
                .andExpect(status().isInternalServerError());

        verify(subprocessoService).mapaCompletoDtoPorSubprocesso(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("salvarMapaCompleto - deve persistir mapa completo")
    @WithMockUser
    void salvarMapaCompletoOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("EDITAR_MAPA"))).thenReturn(true);
        SalvarMapaRequest req = new SalvarMapaRequest(null, List.of());
        when(subprocessoService.salvarMapa(eq(1L), any())).thenReturn(new Mapa());

        mockMvc.perform(post("/api/subprocessos/1/mapa-completo")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).salvarMapa(eq(1L), any());
        verify(subprocessoService).mapaCompletoDtoPorSubprocesso(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("disponibilizarMapaEmBloco - deve processar lista de subprocessos")
    @WithMockUser
    void disponibilizarMapaEmBlocoOk() throws Exception {
        ProcessarEmBlocoRequest req = new ProcessarEmBlocoRequest("DISPONIBILIZAR", List.of(1L), LocalDate.now());
        when(permissionEvaluator.hasPermission(any(), any(), eq("Subprocesso"), eq("DISPONIBILIZAR_MAPA"))).thenReturn(true);

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar-mapa-bloco")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(transicaoService).disponibilizarMapaEmBloco(eq(List.of(1L)), any(), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("obterMapaParaAjuste - deve retornar payload de ajuste")
    @WithMockUser
    void obterMapaParaAjusteOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("AJUSTAR_MAPA"))).thenReturn(true);
        when(subprocessoService.obterMapaParaAjuste(1L)).thenReturn(MapaAjusteDto.builder().build());

        mockMvc.perform(get("/api/subprocessos/1/mapa-ajuste"))
                .andExpect(status().isOk());

        verify(subprocessoService).obterMapaParaAjuste(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("obterSugestoes - deve retornar sugestões consolidadas")
    @WithMockUser
    void obterSugestoesOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(subprocessoService.obterSugestoes(1L)).thenReturn(Map.of("total", 2));

        mockMvc.perform(get("/api/subprocessos/1/sugestoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));

        verify(subprocessoService).obterSugestoes(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("criarAnaliseValidacao - deve registrar análise de validação")
    @WithMockUser(roles = {"GESTOR"})
    void criarAnaliseValidacaoOk() throws Exception {
        CriarAnaliseRequest req = new CriarAnaliseRequest("obs", "mot", sgc.subprocesso.model.TipoAcaoAnalise.ACEITE_MAPEAMENTO);
        when(subprocessoService.buscarSubprocesso(1L)).thenReturn(new Subprocesso());
        Analise a = new Analise();
        when(transicaoService.criarAnalise(any(), any(), eq(sgc.subprocesso.model.TipoAnalise.VALIDACAO), any())).thenReturn(a);
        when(subprocessoService.paraHistoricoDto(a)).thenReturn(new AnaliseHistoricoDto(
                TipoAnalise.VALIDACAO,
                TipoAcaoAnalise.ACEITE_MAPEAMENTO,
                "123456789012",
                "SIGLA",
                "Unidade teste",
                LocalDateTime.now(),
                "mot",
                "obs"));

        mockMvc.perform(post("/api/subprocessos/1/analises-validacao")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("VALIDACAO"));

        verify(subprocessoService).buscarSubprocesso(1L);
        verify(transicaoService).criarAnalise(any(), any(), eq(sgc.subprocesso.model.TipoAnalise.VALIDACAO), any());
        verify(subprocessoService).paraHistoricoDto(a);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }
    @Test
    @DisplayName("criarAnaliseCadastro - deve registrar análise de cadastro")
    @WithMockUser(roles = {"GESTOR"})
    void criarAnaliseCadastroOk() throws Exception {
        CriarAnaliseRequest req = new CriarAnaliseRequest("obs", "mot", sgc.subprocesso.model.TipoAcaoAnalise.ACEITE_MAPEAMENTO);
        when(subprocessoService.buscarSubprocesso(1L)).thenReturn(new Subprocesso());
        Analise a = new Analise();
        when(transicaoService.criarAnalise(any(), any(), eq(sgc.subprocesso.model.TipoAnalise.CADASTRO), any())).thenReturn(a);
        when(subprocessoService.paraHistoricoDto(a)).thenReturn(new AnaliseHistoricoDto(
                TipoAnalise.CADASTRO,
                TipoAcaoAnalise.ACEITE_MAPEAMENTO,
                "123456789012",
                "SIGLA",
                "Unidade teste",
                LocalDateTime.now(),
                "mot",
                "obs"));

        mockMvc.perform(post("/api/subprocessos/1/analises-cadastro")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("CADASTRO"));

        verify(subprocessoService).buscarSubprocesso(1L);
        verify(transicaoService).criarAnalise(any(), any(), eq(sgc.subprocesso.model.TipoAnalise.CADASTRO), any());
        verify(subprocessoService).paraHistoricoDto(a);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("obterMapaParaVisualizacao - deve retornar visão de leitura")
    @WithMockUser
    void obterMapaParaVisualizacaoOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(subprocessoService.mapaParaVisualizacao(1L)).thenReturn(MapaVisualizacaoResponse.builder().build());

        mockMvc.perform(get("/api/subprocessos/1/mapa-visualizacao"))
                .andExpect(status().isOk());

        verify(subprocessoService).mapaParaVisualizacao(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("salvarAjustesMapa - deve aplicar ajustes recebidos")
    @WithMockUser
    void salvarAjustesMapaOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("EDITAR_MAPA"))).thenReturn(true);
        SalvarAjustesRequest req = new SalvarAjustesRequest(List.of(new CompetenciaAjusteDto(1L, "desc", List.of())));

        mockMvc.perform(post("/api/subprocessos/1/mapa-ajuste/atualizar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(subprocessoService).salvarAjustesMapa(eq(1L), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("apresentarSugestoes - deve encaminhar justificativa ao fluxo")
    @WithMockUser
    void apresentarSugestoesOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("APRESENTAR_SUGESTOES"))).thenReturn(true);
        TextoRequest req = new TextoRequest("Sugestao");

        mockMvc.perform(post("/api/subprocessos/1/apresentar-sugestoes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(transicaoService).apresentarSugestoes(eq(1L), eq("Sugestao"), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("obterHistoricoValidacao - deve retornar histórico de validação")
    @WithMockUser
    void obterHistoricoValidacaoOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(subprocessoService.listarHistoricoValidacao(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/subprocessos/1/historico-validacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(subprocessoService).listarHistoricoValidacao(1L);
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }

    @Test
    @DisplayName("verificarImpactos - deve retornar impactos calculados")
    @WithMockUser
    void verificarImpactosOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VERIFICAR_IMPACTOS"))).thenReturn(true);
        when(subprocessoService.verificarImpactos(eq(1L), any())).thenReturn(ImpactoMapaResponse.builder().build());

        mockMvc.perform(get("/api/subprocessos/1/impactos-mapa"))
                .andExpect(status().isOk());

        verify(subprocessoService).verificarImpactos(eq(1L), any());
        verifyNoMoreInteractions(subprocessoService, transicaoService, unidadeService);
    }
}
