package sgc.subprocesso;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import sgc.comum.erros.RestExceptionHandler;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaService;
import sgc.mapa.service.MapaVisualizacaoService;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.dto.CompetenciaReq;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.dto.SalvarAjustesReq;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoConsultaService;
import sgc.subprocesso.service.SubprocessoDtoService;
import sgc.subprocesso.service.SubprocessoMapaService;
import sgc.subprocesso.service.SubprocessoMapaWorkflowService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SubprocessoMapaControllerTest {

    @Mock private SubprocessoMapaService subprocessoMapaService;
    @Mock private MapaService mapaService;
    @Mock private MapaVisualizacaoService mapaVisualizacaoService;
    @Mock private ImpactoMapaService impactoMapaService;
    @Mock private SubprocessoDtoService subprocessoDtoService;
    @Mock private SubprocessoMapaWorkflowService subprocessoMapaWorkflowService;
    @Mock private SubprocessoConsultaService subprocessoConsultaService;

    @InjectMocks private SubprocessoMapaController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RestExceptionHandler())
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterAnnotation(AuthenticationPrincipal.class) != null;
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
                        Usuario usuario = new Usuario();
                        usuario.setTituloEleitoral("123");
                        return usuario.getTituloEleitoral();
                    }
                })
                .build();
    }

    @Test
    @DisplayName("verificarImpactos")
    void verificarImpactos() throws Exception {
        when(impactoMapaService.verificarImpactos(eq(1L), any())).thenReturn(ImpactoMapaDto.semImpacto());

        mockMvc.perform(get("/api/subprocessos/1/impactos-mapa"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("obterMapa")
    void obterMapa() throws Exception {
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        when(subprocessoConsultaService.getSubprocessoComMapa(1L)).thenReturn(sp);
        when(mapaService.obterMapaCompleto(10L, 1L)).thenReturn(new MapaCompletoDto());

        mockMvc.perform(get("/api/subprocessos/1/mapa"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("obterMapaVisualizacao")
    void obterMapaVisualizacao() throws Exception {
        when(mapaVisualizacaoService.obterMapaParaVisualizacao(1L)).thenReturn(new MapaVisualizacaoDto());

        mockMvc.perform(get("/api/subprocessos/1/mapa-visualizacao"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("salvarMapa")
    void salvarMapa() throws Exception {
        SalvarMapaRequest req = new SalvarMapaRequest();
        req.setObservacoes("obs");
        req.setCompetencias(List.of()); // Valid if empty list allowed by annotation, check DTO

        when(subprocessoMapaWorkflowService.salvarMapaSubprocesso(eq(1L), any(), any())).thenReturn(new MapaCompletoDto());

        mockMvc.perform(post("/api/subprocessos/1/mapa/atualizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("obterMapaParaAjuste")
    void obterMapaParaAjuste() throws Exception {
        when(subprocessoDtoService.obterMapaParaAjuste(1L)).thenReturn(MapaAjusteDto.builder().build());

        mockMvc.perform(get("/api/subprocessos/1/mapa-ajuste"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("salvarAjustesMapa")
    void salvarAjustesMapa() throws Exception {
        SalvarAjustesReq req = new SalvarAjustesReq();
        req.setCompetencias(List.of());

        mockMvc.perform(post("/api/subprocessos/1/mapa-ajuste/atualizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("obterMapaCompleto")
    void obterMapaCompleto() throws Exception {
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        when(subprocessoConsultaService.getSubprocessoComMapa(1L)).thenReturn(sp);
        when(mapaService.obterMapaCompleto(10L, 1L)).thenReturn(new MapaCompletoDto());

        mockMvc.perform(get("/api/subprocessos/1/mapa-completo"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("salvarMapaCompleto")
    void salvarMapaCompleto() throws Exception {
        SalvarMapaRequest req = new SalvarMapaRequest();
        req.setObservacoes("obs");
        req.setCompetencias(List.of());

        when(subprocessoMapaWorkflowService.salvarMapaSubprocesso(eq(1L), any(), any())).thenReturn(new MapaCompletoDto());

        mockMvc.perform(post("/api/subprocessos/1/mapa-completo/atualizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("adicionarCompetencia")
    void adicionarCompetencia() throws Exception {
        CompetenciaReq req = new CompetenciaReq();
        req.setDescricao("Comp");
        req.setAtividadesIds(List.of());

        when(subprocessoMapaWorkflowService.adicionarCompetencia(eq(1L), any(), any())).thenReturn(new MapaCompletoDto());

        mockMvc.perform(post("/api/subprocessos/1/competencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("atualizarCompetencia")
    void atualizarCompetencia() throws Exception {
        CompetenciaReq req = new CompetenciaReq();
        req.setDescricao("Comp");
        req.setAtividadesIds(List.of());

        when(subprocessoMapaWorkflowService.atualizarCompetencia(eq(1L), eq(10L), any(), any())).thenReturn(new MapaCompletoDto());

        mockMvc.perform(put("/api/subprocessos/1/competencias/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("removerCompetencia")
    void removerCompetencia() throws Exception {
        when(subprocessoMapaWorkflowService.removerCompetencia(eq(1L), eq(10L), any())).thenReturn(new MapaCompletoDto());

        mockMvc.perform(delete("/api/subprocessos/1/competencias/10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("disponibilizarMapa")
    void disponibilizarMapa() throws Exception {
        DisponibilizarMapaRequest req = new DisponibilizarMapaRequest();
        req.setObservacoes("Obs");
        req.setDataLimite(java.time.LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
