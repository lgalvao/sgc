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
import sgc.organizacao.*;
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
@DisplayName("SubprocessoController - Cobertura Extra")
class SubprocessoControllerCoverageExtraTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    @MockitoBean
    private SubprocessoService subprocessoService;
    @MockitoBean
    private OrganizacaoFacade organizacaoFacade;
    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("disponibilizarCadastro - erro validacao")
    @WithMockUser
    void disponibilizarCadastroErro() throws Exception {
        Atividade a = new Atividade();
        a.setCodigo(1L);
        a.setDescricao("desc");
        when(subprocessoService.obterAtividadesSemConhecimento(1L)).thenReturn(List.of(a));
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("DISPONIBILIZAR_CADASTRO"))).thenReturn(true);

        mockMvc.perform(post("/api/subprocessos/1/cadastro/disponibilizar").with(csrf()))
                .andExpect(status().isUnprocessableEntity());

    }

    @Test
    @DisplayName("disponibilizarRevisao - erro validacao")
    @WithMockUser
    void disponibilizarRevisaoErro() throws Exception {
        Atividade a = new Atividade();
        a.setCodigo(1L);
        a.setDescricao("desc");
        when(subprocessoService.obterAtividadesSemConhecimento(1L)).thenReturn(List.of(a));
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("DISPONIBILIZAR_REVISAO_CADASTRO"))).thenReturn(true);

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar-revisao").with(csrf()))
                .andExpect(status().isUnprocessableEntity());

    }

    @Test
    @DisplayName("obterMapaCompleto - erro generico")
    @WithMockUser
    void obterMapaCompletoErro() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(subprocessoService.mapaCompletoPorSubprocesso(1L)).thenThrow(new RuntimeException("erro"));

        mockMvc.perform(get("/api/subprocessos/1/mapa-completo"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("salvarMapaCompleto - ok")
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
    }

    @Test
    @DisplayName("disponibilizarMapaEmBloco - ok")
    @WithMockUser
    void disponibilizarMapaEmBlocoOk() throws Exception {
        ProcessarEmBlocoRequest req = new ProcessarEmBlocoRequest("DISPONIBILIZAR", List.of(1L), LocalDate.now());
        when(permissionEvaluator.hasPermission(any(), any(), eq("Subprocesso"), eq("DISPONIBILIZAR_MAPA"))).thenReturn(true);

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar-mapa-bloco")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("obterMapaParaAjuste - ok")
    @WithMockUser
    void obterMapaParaAjusteOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("AJUSTAR_MAPA"))).thenReturn(true);
        when(subprocessoService.obterMapaParaAjuste(1L)).thenReturn(MapaAjusteDto.builder().build());

        mockMvc.perform(get("/api/subprocessos/1/mapa-ajuste"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("obterSugestoes - ok")
    @WithMockUser
    void obterSugestoesOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(subprocessoService.obterSugestoes()).thenReturn(Map.of());

        mockMvc.perform(get("/api/subprocessos/1/sugestoes"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("criarAnaliseValidacao - ok")
    @WithMockUser(roles = {"GESTOR"})
    void criarAnaliseValidacaoOk() throws Exception {
        CriarAnaliseRequest req = new CriarAnaliseRequest("191919", "obs", "SGL", "mot", sgc.subprocesso.model.TipoAcaoAnalise.ACEITE_MAPEAMENTO);
        when(subprocessoService.buscarSubprocesso(1L)).thenReturn(new Subprocesso());
        Analise a = new Analise();
        when(subprocessoService.criarAnalise(any(), any(), eq(sgc.subprocesso.model.TipoAnalise.VALIDACAO))).thenReturn(a);
        when(subprocessoService.paraHistoricoDto(a)).thenReturn(new AnaliseHistoricoDto(null, null, null, null, null, null, null, null));

        mockMvc.perform(post("/api/subprocessos/1/analises-validacao")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
    @Test
    @DisplayName("criarAnaliseCadastro - ok")
    @WithMockUser(roles = {"GESTOR"})
    void criarAnaliseCadastroOk() throws Exception {
        CriarAnaliseRequest req = new CriarAnaliseRequest("191919", "obs", "SGL", "mot", sgc.subprocesso.model.TipoAcaoAnalise.ACEITE_MAPEAMENTO);
        when(subprocessoService.buscarSubprocesso(1L)).thenReturn(new Subprocesso());
        Analise a = new Analise();
        when(subprocessoService.criarAnalise(any(), any(), eq(sgc.subprocesso.model.TipoAnalise.CADASTRO))).thenReturn(a);
        when(subprocessoService.paraHistoricoDto(a)).thenReturn(new AnaliseHistoricoDto(null, null, null, null, null, null, null, null));

        mockMvc.perform(post("/api/subprocessos/1/analises-cadastro")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("obterMapaParaVisualizacao - ok")
    @WithMockUser
    void obterMapaParaVisualizacaoOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(subprocessoService.mapaParaVisualizacao(1L)).thenReturn(MapaVisualizacaoResponse.builder().build());

        mockMvc.perform(get("/api/subprocessos/1/mapa-visualizacao"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("salvarAjustesMapa - ok")
    @WithMockUser
    void salvarAjustesMapaOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("EDITAR_MAPA"))).thenReturn(true);
        SalvarAjustesRequest req = new SalvarAjustesRequest(List.of(new CompetenciaAjusteDto(1L, "desc", null)));

        mockMvc.perform(post("/api/subprocessos/1/mapa-ajuste/atualizar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("apresentarSugestoes - ok")
    @WithMockUser
    void apresentarSugestoesOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("APRESENTAR_SUGESTOES"))).thenReturn(true);
        TextoRequest req = new TextoRequest("Sugestao");

        mockMvc.perform(post("/api/subprocessos/1/apresentar-sugestoes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("obterHistoricoValidacao - ok")
    @WithMockUser
    void obterHistoricoValidacaoOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO"))).thenReturn(true);
        when(subprocessoService.listarHistoricoValidacao(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/subprocessos/1/historico-validacao"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("verificarImpactos - ok")
    @WithMockUser
    void verificarImpactosOk() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(1L), eq("Subprocesso"), eq("VERIFICAR_IMPACTOS"))).thenReturn(true);
        when(subprocessoService.verificarImpactos(eq(1L), any())).thenReturn(ImpactoMapaResponse.builder().build());

        mockMvc.perform(get("/api/subprocessos/1/impactos-mapa"))
                .andExpect(status().isOk());
    }
}
